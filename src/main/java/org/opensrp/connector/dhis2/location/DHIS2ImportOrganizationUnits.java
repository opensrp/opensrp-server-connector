package org.opensrp.connector.dhis2.location;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.opensrp.connector.dhis2.DHIS2Service;
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

import java.util.*;

import static org.opensrp.connector.dhis2.location.DHIS2Constants.DHIS2_LAST_PAGE_SYNC_TOKEN_NAME;
import static org.opensrp.connector.dhis2.location.DHIS2Constants.DHIS_LOCATION_JOB_STATUS_TOKEN_NAME;
import static org.opensrp.connector.dhis2.location.DHIS2Constants.DHIS_LOCATION_ROWS_PROCESSED_TOKEN_NAME;
import static org.opensrp.connector.dhis2.location.DHIS2Constants.DHIS_PAGE_COUNT_TOKEN_NAME;
import static org.opensrp.connector.dhis2.location.DHIS2Constants.ORG_UNIT_ANCESTORS_KEY;
import static org.opensrp.connector.dhis2.location.DHIS2Constants.ORG_UNIT_CLOSING_DATE_KEY;
import static org.opensrp.connector.dhis2.location.DHIS2Constants.ORG_UNIT_CODE_KEY;
import static org.opensrp.connector.dhis2.location.DHIS2Constants.ORG_UNIT_CORDINATES_KEY;
import static org.opensrp.connector.dhis2.location.DHIS2Constants.ORG_UNIT_FEATURE_TYPE_KEY;
import static org.opensrp.connector.dhis2.location.DHIS2Constants.ORG_UNIT_GEOMETRY_KEY;
import static org.opensrp.connector.dhis2.location.DHIS2Constants.ORG_UNIT_ID_KEY;
import static org.opensrp.connector.dhis2.location.DHIS2Constants.ORG_UNIT_KEY;
import static org.opensrp.connector.dhis2.location.DHIS2Constants.ORG_UNIT_LEVEL_KEY;
import static org.opensrp.connector.dhis2.location.DHIS2Constants.ORG_UNIT_NAME_KEY;
import static org.opensrp.connector.dhis2.location.DHIS2Constants.ORG_UNIT_NEXT_PAGE_KEY;
import static org.opensrp.connector.dhis2.location.DHIS2Constants.ORG_UNIT_OPENING_DATE_KEY;
import static org.opensrp.connector.dhis2.location.DHIS2Constants.ORG_UNIT_ORG_GROUP_ID_KEY;
import static org.opensrp.connector.dhis2.location.DHIS2Constants.ORG_UNIT_ORG_GROUP_KEY;
import static org.opensrp.connector.dhis2.location.DHIS2Constants.ORG_UNIT_ORG_GROUP_NAME_KEY;
import static org.opensrp.connector.dhis2.location.DHIS2Constants.ORG_UNIT_PAGER_KEY;
import static org.opensrp.connector.dhis2.location.DHIS2Constants.ORG_UNIT_PAGE_KEY;
import static org.opensrp.connector.dhis2.location.DHIS2Constants.ORG_UNIT_PARENT_ID_KEY;
import static org.opensrp.connector.dhis2.location.DHIS2Constants.TOTAL_LOCATIONS_TOKEN_NAME;
import static org.opensrp.connector.dhis2.location.DHISUtils.parseDhisDate;

@Component
public class DHIS2ImportOrganizationUnits extends DHIS2Service {

	private final AppStateTokensRepository allAppStateTokens;

	@Autowired
	private PhysicalLocationService physicalLocationService;

	@Autowired
	private LocationTagService locationTagService;

	@Autowired
	private Dhis2EndPoints dhis2EndPoints;

	private Gson gson = new Gson();

	private int rowsProcessed;

	private boolean isJobRunning;

	private boolean isJobFailed;

	private static org.slf4j.Logger logger = LoggerFactory.getLogger(DHIS2ImportOrganizationUnits.class.toString());

	@Autowired
	public DHIS2ImportOrganizationUnits(AppStateTokensRepository allAppStateTokens) {
		this.allAppStateTokens = allAppStateTokens;
	}

	@Async
	public void importOrganizationUnits(String startPage) {

		try {
			JSONObject root = dhis2EndPoints.getOrganisationalUnitList(Integer.parseInt(startPage));
			JSONObject pager = root.getJSONObject(ORG_UNIT_PAGER_KEY);

			while (true) {
				isJobRunning = Boolean.TRUE;
				JSONArray orgUnits = root.getJSONArray(ORG_UNIT_KEY);
				for (int i = 0; i < orgUnits.length(); i++) {
					createOrUpdatePhysicalLocation(orgUnits.getJSONObject(i), pager);
				}

				if (pager.has(ORG_UNIT_NEXT_PAGE_KEY)) {
					Integer currentPageNumber = pager.optInt(ORG_UNIT_PAGE_KEY);
					Integer nextPageNumber = currentPageNumber + 1;
					logger.info("Going to page number  : " + nextPageNumber);
					root = dhis2EndPoints.getOrganisationalUnitList(nextPageNumber);
					pager = root.getJSONObject(ORG_UNIT_PAGER_KEY);
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

	private void createOrUpdatePhysicalLocation(JSONObject organizationUnit, JSONObject pager) {
		rowsProcessed++;
		String orgUnitId = organizationUnit.optString(ORG_UNIT_ID_KEY);

		PhysicalLocation physicalLocation;

		physicalLocation = physicalLocationService.getLocation(orgUnitId, true);
		logger.debug("Processing Organization Unit : " + organizationUnit);
		JSONObject organisationUnitDetails = dhis2EndPoints.getOrganisationalUnit(orgUnitId);

		if (physicalLocation == null) {
			physicalLocation = new PhysicalLocation();
		}
		PhysicalLocation persistedPhysicalLocation = null;

		try {
			persistedPhysicalLocation = convertAndPersistPhysicalLocation(organisationUnitDetails,
					physicalLocation);

			if (persistedPhysicalLocation != null) {
				updateOrCreateAppStateTokens(pager);
			}
		}
		catch (Exception e) {
			logger.error("Exception occurred while converting and persisting Physical Location : " + e.getMessage());
		}

	}

	public PhysicalLocation convertAndPersistPhysicalLocation(JSONObject organisationUnitDetails,
			PhysicalLocation physicalLocation)
			throws Exception {

		Set<LocationTag> dTagList = getLocationTagsFromOrgUnitDetails(physicalLocation, organisationUnitDetails);
		physicalLocation.setLocationTags(dTagList);

		LocationProperty locationProperty = physicalLocation.getProperties();
		if (locationProperty == null) {
			locationProperty = new LocationProperty();
		}
		if (organisationUnitDetails.has(ORG_UNIT_CODE_KEY)) {
			String codeval = organisationUnitDetails.optString(ORG_UNIT_CODE_KEY);
			if (codeval != null) {
				locationProperty.setCode(codeval);
			}
		}

		if (organisationUnitDetails.has(ORG_UNIT_NAME_KEY)) {
			String locationName = organisationUnitDetails.optString(ORG_UNIT_NAME_KEY);
			locationProperty.setName(locationName);
		}

		if (organisationUnitDetails.has(ORG_UNIT_LEVEL_KEY)) {
			Integer level = organisationUnitDetails.optInt(ORG_UNIT_LEVEL_KEY);
			locationProperty.setGeographicLevel(level);
		}

		if (organisationUnitDetails.has(ORG_UNIT_PARENT_ID_KEY)) {
			JSONObject parentIdObject = organisationUnitDetails.getJSONObject(ORG_UNIT_PARENT_ID_KEY);
			String parentId = parentIdObject.getString("id");
			locationProperty.setParentId(parentId);
		}

		if (organisationUnitDetails.has(ORG_UNIT_OPENING_DATE_KEY)) {
			String openingDateInString = organisationUnitDetails.optString(ORG_UNIT_OPENING_DATE_KEY);
			Date openingDate = parseDhisDate(openingDateInString);
			locationProperty.setEffectiveStartDate(openingDate);
		}

		Date endDate = null;

		if (organisationUnitDetails.has(ORG_UNIT_CLOSING_DATE_KEY)) {
			String endDateInString = organisationUnitDetails.optString(ORG_UNIT_CLOSING_DATE_KEY);
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
		if (organisationUnitDetails.has(ORG_UNIT_ANCESTORS_KEY)) {
			JSONArray ancestors = organisationUnitDetails.getJSONArray(ORG_UNIT_ANCESTORS_KEY);
			for (int i = 0; i < ancestors.length(); i++) {
				JSONObject ancestor = ancestors.getJSONObject(i);
				heirarchyValue += ancestor.getString("id");
				if (i != ancestors.length() - 1)
					heirarchyValue += ",";
			}
			customProperties.put("heirarchy", heirarchyValue);
		}

		locationProperty.setVersion(0);
		locationProperty.setCustomProperties(customProperties);

		physicalLocation.setProperties(locationProperty);

		if (organisationUnitDetails.has(ORG_UNIT_ID_KEY)) {
			String idValue = organisationUnitDetails.optString(ORG_UNIT_ID_KEY);
			if (idValue != null) {
				physicalLocation.setId(idValue);
			}
		}

		Geometry geometry = physicalLocation.getGeometry();
		JsonArray jsonArrayOfCordinates = new JsonArray();
		String type;
		if (geometry == null) {
			geometry = new Geometry();
			geometry.setCoordinates(jsonArrayOfCordinates);
			geometry.setType(null);
		}

		if (organisationUnitDetails.has(ORG_UNIT_GEOMETRY_KEY)) {
			JSONObject geometryValue = (JSONObject) organisationUnitDetails.get(ORG_UNIT_GEOMETRY_KEY);
			JSONArray cordinates = (JSONArray) geometryValue.get(ORG_UNIT_CORDINATES_KEY);
			String cordinatesArray = cordinates.toString();
			jsonArrayOfCordinates = gson.fromJson(cordinatesArray, JsonArray.class);
			geometry.setCoordinates(jsonArrayOfCordinates);
			type = geometryValue.getString(ORG_UNIT_FEATURE_TYPE_KEY);
			if (type.equals("MultiPolygon")) {
				geometry.setType(Geometry.GeometryType.MULTI_POLYGON);
			} else {
				geometry.setType(Geometry.GeometryType.valueOf(StringUtils.upperCase(type)));
			}

		}

		physicalLocation.setGeometry(geometry);

		physicalLocation.setJurisdiction(Boolean.TRUE);
		physicalLocationService.addOrUpdate(physicalLocation);
		return physicalLocation;
	}

	private Set<LocationTag> getLocationTagsFromOrgUnitDetails(PhysicalLocation physicalLocation,
			JSONObject organisationUnitDetails) {
		Set<LocationTag> dTagList = new HashSet<>();
		if (physicalLocation.getLocationTags() != null && !physicalLocation.getLocationTags().isEmpty()) {
			dTagList.addAll(physicalLocation.getLocationTags());
		}

		if (organisationUnitDetails.has(ORG_UNIT_ORG_GROUP_KEY)) {
			JSONArray organisationUnitGroups = organisationUnitDetails.getJSONArray(ORG_UNIT_ORG_GROUP_KEY);
			for (int i = 0; i < organisationUnitGroups.length(); i++) {
				JSONObject organisationUnitGroup = organisationUnitGroups.getJSONObject(i);
				JSONObject fullGroup = dhis2EndPoints.getOrganisationalUnitGroup(
						(String) organisationUnitGroup.get(ORG_UNIT_ORG_GROUP_ID_KEY));
				LocationTag tag = getOrCreateLocationTag(
						(String) fullGroup.get(ORG_UNIT_ORG_GROUP_NAME_KEY));

				dTagList.add(tag);
			}
		}
		return dTagList;
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
		ltag.setActive(true);
		return locationTagService.addOrUpdateLocationTag(ltag);
	}

	private void updateOrCreateAppStateTokens(JSONObject pager) {

		AppStateToken dhisLastSyncPageToken;
		AppStateToken dhisRowsProcessed;
		AppStateToken pageCountToken;
		AppStateToken totalLocationsToken;
		AppStateToken dhisLocationJobStatus;

		Integer pageCount = pager.optInt("pageCount");
		Integer totalLocations = pager.optInt("total");
		DHISImportLocationsJobStatus dhisImportLocationsJobStatus =
				isJobRunning && !isJobFailed ? DHISImportLocationsJobStatus.RUNNING :
						!isJobRunning && !isJobFailed ?
								DHISImportLocationsJobStatus.COMPLETED :
								DHISImportLocationsJobStatus.FAILED;

		dhisLastSyncPageToken = new AppStateToken(DHIS2_LAST_PAGE_SYNC_TOKEN_NAME, pager.get("page"), new Date().getTime());
		dhisRowsProcessed = new AppStateToken(DHIS_LOCATION_ROWS_PROCESSED_TOKEN_NAME, rowsProcessed, new Date().getTime());
		pageCountToken = new AppStateToken(DHIS_PAGE_COUNT_TOKEN_NAME, pageCount, new Date().getTime());
		totalLocationsToken = new AppStateToken(TOTAL_LOCATIONS_TOKEN_NAME, totalLocations, new Date().getTime());
		dhisLocationJobStatus = new AppStateToken(DHIS_LOCATION_JOB_STATUS_TOKEN_NAME,
				dhisImportLocationsJobStatus != null ? dhisImportLocationsJobStatus.name() : null, new Date().getTime());

		addOrUpdateAppStateToken(DHIS2_LAST_PAGE_SYNC_TOKEN_NAME, dhisLastSyncPageToken);
		addOrUpdateAppStateToken(DHIS_LOCATION_ROWS_PROCESSED_TOKEN_NAME, dhisRowsProcessed);
		addOrUpdateAppStateToken(DHIS_PAGE_COUNT_TOKEN_NAME, pageCountToken);
		addOrUpdateAppStateToken(TOTAL_LOCATIONS_TOKEN_NAME, totalLocationsToken);
		addOrUpdateAppStateToken(DHIS_LOCATION_JOB_STATUS_TOKEN_NAME, dhisLocationJobStatus);
	}

	private void updateDHISLocationJobStatus(String status) {
		AppStateToken dhisLocationJobStatus;

		dhisLocationJobStatus = new AppStateToken(DHIS_LOCATION_JOB_STATUS_TOKEN_NAME, status.equals("Failed") ?
				DHISImportLocationsJobStatus.FAILED.name() :
				status.equals("Completed") ?
						DHISImportLocationsJobStatus.COMPLETED.name() :
						DHISImportLocationsJobStatus.RUNNING.name(),
				new Date().getTime());

		addOrUpdateAppStateToken(DHIS_LOCATION_JOB_STATUS_TOKEN_NAME, dhisLocationJobStatus);
	}

	private void addOrUpdateAppStateToken(String tokenName, AppStateToken appStateToken) {
		if (allAppStateTokens.findByName(tokenName).size() > 0) {
			allAppStateTokens.update(appStateToken);
		} else {
			allAppStateTokens.add(appStateToken);
		}
	}

}
