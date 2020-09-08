package org.opensrp.connector.dhis2;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.opensrp.domain.AppStateToken;
import org.opensrp.repository.AppStateTokensRepository;
import org.opensrp.service.LocationTagService;
import org.opensrp.service.PhysicalLocationService;
import org.slf4j.LoggerFactory;
import org.smartregister.domain.Geometry;
import org.smartregister.domain.LocationProperty;
import org.smartregister.domain.LocationTag;
import org.smartregister.domain.PhysicalLocation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.text.ParseException;
import java.util.*;

import static org.opensrp.connector.dhis2.DHIS2Constants.*;
import static org.opensrp.connector.dhis2.DHISUtils.parseDhisDate;
import static org.opensrp.connector.dhis2.Dhis2EndPoints.*;

@Component
public class DHIS2ImportOrganizationUnits extends DHIS2Service {

	private final AppStateTokensRepository allAppStateTokens;

	@Autowired
	private PhysicalLocationService physicalLocationService;

	@Autowired
	private LocationTagService locationTagService;

	private Gson gson = new Gson();

	private Integer rowsProcessed = 0;

	private Boolean isJobRunning = false;

	private Boolean isJobFailed = false;

	private static org.slf4j.Logger logger = LoggerFactory.getLogger(DHIS2ImportOrganizationUnits.class.toString());

	public DHIS2ImportOrganizationUnits(AppStateTokensRepository allAppStateTokens) {
		this.allAppStateTokens = allAppStateTokens;
	}

	@Async
	public void importOrganizationUnits(String startPage) {

		try {
			JSONObject root = getOrganisationalUnitList(Integer.parseInt(startPage));
			JSONObject pager = (JSONObject) root.get("pager");

			while (true) {
				isJobRunning = Boolean.TRUE;
				JSONArray orgUnits = (JSONArray) root.get(ORG_UNIT_KEY);
				for (int i = 0; i < orgUnits.length(); i++) {
				createOrUpdatePhysicalLocation(orgUnits.getJSONObject(i), pager);
				}

				if (pager.has(ORG_UNIT_NEXT_PAGE_KEY)) {
					Integer currentPageNumber = (Integer) pager.get(ORG_UNIT_PAGE_KEY);
					System.out.println("Going to page number  : " + currentPageNumber + 1);
					root = getOrganisationalUnitList(currentPageNumber + 1);
					pager = root.getJSONObject("pager");
				} else {
					isJobRunning = Boolean.FALSE;
					updateDHISLocationJobStatus("Completed");
					break;
				}
			}
		}

		catch (Exception e) {
			isJobRunning = Boolean.FALSE;
			isJobFailed = Boolean.TRUE;
			updateDHISLocationJobStatus("Failed");
			logger.error("Exception occurred in importing DHIS locations : " + e.getMessage());
		}

	}

	private void createOrUpdatePhysicalLocation(JSONObject ou, JSONObject pager)
			throws IOException, ParseException {
		rowsProcessed++;
		String orgUnitId = (String) ou.get(ORG_UNIT_ID_KEY);

		PhysicalLocation l = null;

		l = physicalLocationService.getLocation(orgUnitId, true);
		System.out.println("Processing OU :: " + ou);
		JSONObject oudet = getOrganisationalUnit(orgUnitId);

		// if still null create a new one
		if (l == null) {
			l = new PhysicalLocation();
		}
		PhysicalLocation physicalLocation = convertAndPersistPhysicalLocation(oudet, l);
		if (physicalLocation != null) {
			updateOrCreateAppStateTokens(pager);
		}
	}

	public PhysicalLocation convertAndPersistPhysicalLocation(JSONObject oudet, PhysicalLocation l)
			throws IOException, ParseException {

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
		Date endDate = null;
		if (oudet.has(ORG_UNIT_CLOSING_DATE_KEY)) {
			String endDateInString = (String) oudet.get(ORG_UNIT_CLOSING_DATE_KEY);
			endDate = parseDhisDate(endDateInString);
			locationProperty.setEffectiveEndDate(endDate);
		}

		if (endDate != null) {
			locationProperty.setStatus(LocationProperty.PropertyStatus.INACTIVE);
		} else {
			locationProperty.setStatus(LocationProperty.PropertyStatus.ACTIVE);
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
			System.out.println("ID is : " + idval);
			if (idval != null) {
				l.setId(idval);
			}
		}

		Geometry geometry = l.getGeometry();
		JsonArray jsonArrayOfCordinates = new JsonArray();
		String type;
		if (geometry == null) {
			geometry = new Geometry();
			geometry.setCoordinates(jsonArrayOfCordinates);
			geometry.setType(null);
		}

		if (oudet.has(ORG_UNIT_GEOMETRY_KEY)) {
			JSONObject geometryValue = (JSONObject) oudet.get(ORG_UNIT_GEOMETRY_KEY);
			JSONArray cordinates = (JSONArray) geometryValue.get(ORG_UNIT_CORDINATES_KEY);
			String cordinatesArray = cordinates.toString();
			jsonArrayOfCordinates = gson.fromJson(cordinatesArray, JsonArray.class);
			geometry.setCoordinates(jsonArrayOfCordinates);
			type = geometryValue.getString(ORG_UNIT_FEATURE_TYPE_KEY);
			System.out.println("type is  : " + type);
			if (type.equals("MultiPolygon")) {
				geometry.setType(Geometry.GeometryType.MULTI_POLYGON);
			} else {
				geometry.setType(Geometry.GeometryType.valueOf(StringUtils.upperCase(type)));
			}

		}

		l.setGeometry(geometry);

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

	private void updateOrCreateAppStateTokens(JSONObject pager) {

		AppStateToken dhisLastSyncPageToken;
		AppStateToken dhisRowsProcessed;
		AppStateToken pageCountToken;
		AppStateToken totalLocationsToken;
		AppStateToken dhisLocationJobStatus;

		Integer pageCount = (Integer) pager.get("pageCount");
		Integer totalLocations = (Integer) pager.get("total");
		DHISImportLocationsJobStatus dhisImportLocationsJobStatus =
				isJobRunning && !isJobFailed ? DHISImportLocationsJobStatus.RUNNING :
						!isJobRunning && !isJobFailed ?
								DHISImportLocationsJobStatus.COMPLETED :
								DHISImportLocationsJobStatus.FAILED;

		dhisLastSyncPageToken = new AppStateToken("DHIS2-LAST-SYNC-PAGE", pager.get("page"), new Date().getTime());
		if (allAppStateTokens.findByName("DHIS2-LAST-SYNC-PAGE").size() > 0) {
			allAppStateTokens.update(dhisLastSyncPageToken);
		} else {
			allAppStateTokens.add(dhisLastSyncPageToken);
		}
		//

		dhisRowsProcessed = new AppStateToken("DHIS2-LOCATION-ROWS-PROCESSED", rowsProcessed, new Date().getTime());
		if (allAppStateTokens.findByName("DHIS2-LOCATION-ROWS-PROCESSED").size() > 0) {
			allAppStateTokens.update(dhisRowsProcessed);
		} else {
			allAppStateTokens.add(dhisRowsProcessed);
		}

		pageCountToken = new AppStateToken("DHIS-PAGE-COUNT", pageCount, new Date().getTime());
		if (allAppStateTokens.findByName("DHIS-PAGE-COUNT").size() > 0) {
			allAppStateTokens.update(pageCountToken);
		} else {
			allAppStateTokens.add(pageCountToken);
		}

		totalLocationsToken = new AppStateToken("TOTAL-LOCATIONS", totalLocations, new Date().getTime());
		if (allAppStateTokens.findByName("TOTAL-LOCATIONS").size() > 0) {
			allAppStateTokens.update(totalLocationsToken);
		} else {
			allAppStateTokens.add(totalLocationsToken);
		}

		dhisLocationJobStatus = new AppStateToken("DHIS-LOCATIONS-JOB-STATUS",
				dhisImportLocationsJobStatus != null ? dhisImportLocationsJobStatus.name() : null, new Date().getTime());
		if (allAppStateTokens.findByName("DHIS-LOCATIONS-JOB-STATUS").size() > 0) {
			allAppStateTokens.update(dhisLocationJobStatus);
		} else {
			allAppStateTokens.add(dhisLocationJobStatus);
		}
	}

	private void updateDHISLocationJobStatus(String status) {
		AppStateToken dhisLocationJobStatus = null;
		if (status.equals("Failed")) {
			dhisLocationJobStatus = new AppStateToken("DHIS-LOCATIONS-JOB-STATUS",
					DHISImportLocationsJobStatus.FAILED.name(), new Date().getTime());
		} else if (status.equals("Completed")) {
			dhisLocationJobStatus = new AppStateToken("DHIS-LOCATIONS-JOB-STATUS",
					DHISImportLocationsJobStatus.COMPLETED.name(), new Date().getTime());
		}

		if (allAppStateTokens.findByName("DHIS-LOCATIONS-JOB-STATUS").size() > 0) {
			allAppStateTokens.update(dhisLocationJobStatus);
		} else {
			allAppStateTokens.add(dhisLocationJobStatus);
		}
	}
}
