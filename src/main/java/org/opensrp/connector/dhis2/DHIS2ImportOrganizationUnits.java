package org.opensrp.connector.dhis2;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.opensrp.service.LocationTagService;
import org.opensrp.service.PhysicalLocationService;
import org.smartregister.domain.Geometry;
import org.smartregister.domain.LocationProperty;
import org.smartregister.domain.LocationTag;
import org.smartregister.domain.PhysicalLocation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.util.*;

import static org.opensrp.connector.dhis2.DHIS2Constants.*;
import static org.opensrp.connector.dhis2.DHISUtils.parseDhisDate;
import static org.opensrp.connector.dhis2.Dhis2EndPoints.*;

@Component
public class DHIS2ImportOrganizationUnits extends DHIS2Service {

	protected Dhis2HttpUtils dhis2HttpUtils;

	@Autowired
	private PhysicalLocationService physicalLocationService;

	@Autowired
	private LocationTagService locationTagService;

	private Gson gson = new Gson();

	@Autowired
	public DHIS2ImportOrganizationUnits(@Value("#{opensrp['dhis2.url']}") String dhis2Url,
			@Value("#{opensrp['dhis2.username']}") String user, @Value("#{opensrp['dhis2.password']}") String password) {
		super(dhis2Url, user, password);
		dhis2HttpUtils = new Dhis2HttpUtils(dhis2Url, user, password);
	}

	//	@Async
	public String importOrganizationUnits(String startPage) {

		try {
			JSONObject root = getOrganisationalUnitList(Integer.parseInt(startPage));
			JSONObject pager = (JSONObject) root.get("pager");

			while (true) {
				JSONArray orgUnits = (JSONArray) root.get(DHIS2Constants.ORG_UNIT_KEY);

				for (int i = 0; i < orgUnits.length(); i++) {
					createOrUpdatePhysicalLocation(orgUnits.getJSONObject(i));
				}

				if (pager.has(ORG_UNIT_NEXT_PAGE_KEY)) {
					root = getOrganisationalUnitList(Integer.parseInt(pager.get(ORG_UNIT_PAGE_KEY).toString() + 1));
					pager = root.getJSONObject("pager");
				} else {
					break;
				}
			}
			return "";
		}

		catch (IOException | ParseException e) {
			e.printStackTrace();
			return "";
		}

	}

	private void createOrUpdatePhysicalLocation(JSONObject ou) throws MalformedURLException, IOException, ParseException {

		String orgUnitId = (String) ou.get(ORG_UNIT_ID_KEY);

		PhysicalLocation l = null;

		l = physicalLocationService.getLocation(orgUnitId, true);
		System.out.println("Processing OU :: " + ou);
		JSONObject oudet = getOrganisationalUnit(orgUnitId);

		String locationName = (String) oudet.get(ORG_UNIT_NAME_KEY);
		//find by name

		// if still null create a new one
		if (l == null) {
			l = new PhysicalLocation();
		}
		PhysicalLocation physicalLocation = convertAndPersistPhysicalLocation(oudet, l);
	}

	public PhysicalLocation convertAndPersistPhysicalLocation(JSONObject oudet, PhysicalLocation l)
			throws IOException, ParseException {
		PhysicalLocation physicalLocation = null;

		Set<LocationTag> dTagList = new HashSet<>();
		if (l.getLocationTags() != null && !l.getLocationTags().isEmpty()) {
			dTagList.addAll(l.getLocationTags());
		}
		//
		if (oudet.has(ORG_UNIT_ORG_GROUP_KEY)) {
			JSONArray orgunitgrps = oudet.getJSONArray(ORG_UNIT_ORG_GROUP_KEY);
			for (int i = 0; i < orgunitgrps.length(); i++) {
				JSONObject ogroup = orgunitgrps.getJSONObject(i);
				JSONObject fullGroup = getOrganisationalUnitGroup(
						(String) ogroup.get(ORG_UNIT_ORG_GROUP_ID_KEY));
				LocationTag tag = getOrCreateLocationTag(
						(String) fullGroup.get(ORG_UNIT_ORG_GROUP_NAME_KEY));

				dTagList.add(tag);
			}
		}

		boolean newLocationTagAdded = false;

		if (l.getLocationTags() != null && dTagList != null && l.getLocationTags().size() == dTagList.size()) {
			for (LocationTag dTag : dTagList) {
				boolean found = false;

				for (LocationTag lTag : l.getLocationTags()) {
					if (lTag.getName().equalsIgnoreCase(dTag.getName())) {
						found = true;
						break;
					}
				}

				if (!found) {
					newLocationTagAdded = true;
					break;
				}
			}
		}

		LocationProperty locationProperty = l.getProperties();
		if (locationProperty == null) {
			locationProperty = new LocationProperty();
		}
		if (oudet.has(ORG_UNIT_CODE_KEY)) {
			String codeval = (String) oudet.get(ORG_UNIT_CODE_KEY);

			if (codeval != null) {
				locationProperty.setCode(codeval);
			}

		}
		if (oudet.has(ORG_UNIT_NAME_KEY)) {
			String locationName = (String) oudet.get(ORG_UNIT_NAME_KEY);
			locationProperty.setName(locationName);
		}

		if (oudet.has(ORG_UNIT_LEVEL_KEY)) {
			Integer level = (Integer) oudet.get(ORG_UNIT_LEVEL_KEY);
			locationProperty.setGeographicLevel(level);
		}

		if (oudet.has(ORG_UNIT_PARENT_ID_KEY)) {
			JSONObject parentIdObject = (JSONObject) oudet.get(ORG_UNIT_PARENT_ID_KEY);
			String parentId = parentIdObject.getString("id");
			locationProperty.setParentId(parentId);
		}

		if (oudet.has(ORG_UNIT_OPENING_DATE_KEY)) {
			String openingDateInString = (String) oudet.get(ORG_UNIT_OPENING_DATE_KEY);
			Date openingDate = parseDhisDate(openingDateInString);
			locationProperty.setEffectiveStartDate(openingDate);
		}
		if (oudet.has(ORG_UNIT_CLOSING_DATE_KEY)) {
			String endDateInString = (String) oudet.get(ORG_UNIT_CLOSING_DATE_KEY);
			Date endDate = parseDhisDate(endDateInString);
			locationProperty.setEffectiveEndDate(endDate);
			if (endDate != null) {
				locationProperty.setStatus(LocationProperty.PropertyStatus.INACTIVE);
			} else {
				locationProperty.setStatus(LocationProperty.PropertyStatus.ACTIVE);
			}
		}
		Map<String, String> customProperties = locationProperty.getCustomProperties();
		if (customProperties == null) {
			customProperties = new HashMap<>();
		}

		String heirarchyValue = "";
		if (oudet.has(ORG_UNIT_ANCESTORS_KEY)) {
			JSONArray ancestors = oudet.getJSONArray(ORG_UNIT_ANCESTORS_KEY);
			for (int i = 0; i < ancestors.length(); i++) {
				JSONObject ancestor = ancestors.getJSONObject(i);
				heirarchyValue += ancestor.getString("id");
				if (!(i == ancestors.length() - 1))
					heirarchyValue += ",";
			}
			customProperties.put("heirarchy", heirarchyValue);
		}

		locationProperty.setVersion(0);
		locationProperty.setCustomProperties(customProperties);

		l.setProperties(locationProperty);

		if (oudet.has(ORG_UNIT_ID_KEY)) {
			String idval = (String) oudet.get(ORG_UNIT_ID_KEY);
			if (idval != null) {
				l.setId(idval);
			}
		}

		Geometry geometry = l.getGeometry();
		if (geometry == null) {
			geometry = new Geometry();
		}

		if (oudet.has(ORG_UNIT_GEOMETRY_KEY)) {
			JSONObject geometryValue = (JSONObject) oudet.get(ORG_UNIT_GEOMETRY_KEY);
			JSONArray cordinates = (JSONArray) geometryValue.get(ORG_UNIT_CORDINATES_KEY);
			String cordinatesArray = cordinates.toString();
			JsonArray jsonArrayOfCordinates = gson.fromJson(cordinatesArray, JsonArray.class);
			geometry.setCoordinates(jsonArrayOfCordinates);

			String type = geometryValue.getString(ORG_UNIT_FEATURE_TYPE_KEY);
			geometry.setType(Geometry.GeometryType.valueOf(StringUtils.upperCase(type)));
			l.setGeometry(geometry);
		}

		l.setJurisdiction(Boolean.TRUE);
		physicalLocationService.addOrUpdate(l);
		return l;
	}

	private LocationTag getOrCreateLocationTag(String tagName) {
		for (LocationTag tag : locationTagService.getAllLocationTags()) {
			if (tag.getName().equalsIgnoreCase(tagName)) {
				return tag;
			}
		}

		LocationTag ltag = new LocationTag();
		ltag.setId(0l);
		ltag.setName(tagName);
		return locationTagService.addOrUpdateLocationTag(ltag);
	}
}
