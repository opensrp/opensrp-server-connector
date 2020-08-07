package org.opensrp.connector.openmrs.service;

import static org.opensrp.common.util.OpenMRSCrossVariables.TEAM_MEMBER_URL;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.opensrp.api.domain.Location;
import org.opensrp.api.util.LocationTree;
import org.opensrp.api.util.TreeNode;
import org.opensrp.common.util.HttpUtil;
import org.opensrp.connector.openmrs.FetchLocationsHelper;
import org.opensrp.connector.openmrs.constants.ConnectorConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OpenmrsLocationService extends OpenmrsService {
	
	private static Logger logger = LoggerFactory.getLogger(OpenmrsLocationService.class);
	
	public static final String LOCATION_URL = "ws/rest/v1/location";
	
	private FetchLocationsHelper fetchLocationsHelper;
	
	@Autowired
	public OpenmrsLocationService(FetchLocationsHelper fetchLocationsHelper) {
		this.fetchLocationsHelper = fetchLocationsHelper;
	}
	
	public OpenmrsLocationService(String openmrsUrl, String user, String password) {
		super(openmrsUrl, user, password);
	}
	
	public Location getLocation(String locationIdOrName) throws JSONException {
		String response = fetchLocationsHelper.getURL(HttpUtil.removeEndingSlash(OPENMRS_BASE_URL) + "/" + LOCATION_URL + "/"
		        + (locationIdOrName.replaceAll(" ", "%20")) + "?v=full");
		if (!StringUtils.isBlank(response) && (new JSONObject(response).has(ConnectorConstants.UUID))) {
			return fetchLocationsHelper.makeLocation(response);
		}
		
		return null;
	}
	
	public LocationTree getLocationTree() throws JSONException {
		LocationTree ltr = new LocationTree();
		String response = fetchLocationsHelper
		        .getURL(HttpUtil.removeEndingSlash(OPENMRS_BASE_URL) + "/" + LOCATION_URL + "?v=full");
		
		JSONArray res = new JSONObject(response).getJSONArray(ConnectorConstants.RESULTS);
		if (res.length() == 0) {
			return ltr;
		}
		
		for (int i = 0; i < res.length(); i++) {
			ltr.addLocation(fetchLocationsHelper.makeLocation(res.getJSONObject(i)));
		}
		return ltr;
	}
	
	public LocationTree getLocationTreeOf(String locationIdOrName) throws JSONException {
		LocationTree ltr = new LocationTree();
		
		fillTreeWithHierarchy(ltr, locationIdOrName);
		fillTreeWithUpperHierarchy(ltr, locationIdOrName);
		
		return ltr;
	}
	
	public LocationTree getLocationTreeWithUpperHierachyOf(String locationIdOrName) throws JSONException {
		LocationTree ltr = new LocationTree();
		fillTreeWithUpperHierarchy(ltr, locationIdOrName);
		return ltr;
	}
	
	public LocationTree getLocationTreeOf(String[] locationIdsOrNames) throws JSONException {
		LocationTree ltr = new LocationTree();
		
		for (String loc : locationIdsOrNames) {
			String locTreeId = fillTreeWithHierarchy(ltr, loc);
			Location lp = ltr.findLocation(locTreeId).getParentLocation();
			if (lp != null) {
				LoggerFactory.getLogger(this.getClass())
						.debug("getLocationTreeOf node: " + ReflectionToStringBuilder.toString(lp));
				fillTreeWithUpperHierarchy(ltr, lp.getLocationId());
			}
		}
		
		LoggerFactory.getLogger(this.getClass()).debug("getLocationTreeOf tree: " + ReflectionToStringBuilder.toString(ltr));
		return ltr;
	}
	
	private String fillTreeWithHierarchy(LocationTree ltr, String locationIdOrName) throws JSONException {
		
		String response = fetchLocationsHelper.getURL(HttpUtil.removeEndingSlash(OPENMRS_BASE_URL) + "/" + LOCATION_URL + "/"
		        + (locationIdOrName.replaceAll(" ", "%20")) + "?v=full");
		
		JSONObject lo = new JSONObject(response);
		Location l = fetchLocationsHelper.makeLocation(response);
		ltr.addLocation(l);
		
		if (lo.has(ConnectorConstants.CHILD_LOCATIONS)) {
			JSONArray lch = lo.getJSONArray(ConnectorConstants.CHILD_LOCATIONS);
			
			for (int i = 0; i < lch.length(); i++) {
				
				JSONObject cj = lch.getJSONObject(i);
				fillTreeWithHierarchy(ltr, cj.getString(ConnectorConstants.UUID));
			}
		}
		return l.getLocationId();
	}
	
	private void fillTreeWithUpperHierarchy(LocationTree ltr, String locationId) throws JSONException {
		
		String response = fetchLocationsHelper.getURL(HttpUtil.removeEndingSlash(OPENMRS_BASE_URL) + "/" + LOCATION_URL + "/"
		        + (locationId.replaceAll(" ", "%20")) + "?v=full");
		
		Location l = fetchLocationsHelper.makeLocation(response);
		ltr.addLocation(l);
		
		if (l.getParentLocation() != null) {
			fillTreeWithUpperHierarchy(ltr, l.getParentLocation().getLocationId());
		}
	}
	
	public Map<String, String> getLocationsHierarchy(LocationTree locationTree) throws JSONException {
		Map<String, String> map = new HashMap<>();
		if (locationTree == null) {
			return map;
		}
		
		Map<String, TreeNode<String, Location>> locationsHierarchy = locationTree.getLocationsHierarchy();
		if (locationsHierarchy != null && !locationsHierarchy.isEmpty()) {
			for (TreeNode<String, Location> value : locationsHierarchy.values()) {
				extractLocations(map, value);
			}
		}
		return map;
	}
	
	private void extractLocations(Map<String, String> map, TreeNode<String, Location> value) throws JSONException {
		if (value == null || value.getNode() == null) {
			return;
		}
		
		final String[] allowedLevels = { AllowedLevels.COUNTRY.toString(), AllowedLevels.DISTRICT.toString(),
		        AllowedLevels.COUNTY.toString(), AllowedLevels.SUB_COUNTY.toString(),
		        AllowedLevels.HEALTH_FACILITY.toString() };
		
		Location node = value.getNode();
		String name = node.getName();
		Set<String> tags = node.getTags();
		{
			if (tags != null && !tags.isEmpty()) {
				for (String level : tags) {
					if (ArrayUtils.contains(allowedLevels, level)) {
						map.put(level, name);
					}
				}
			}
			
		}
		Map<String, TreeNode<String, Location>> children = value.getChildren();
		if (children != null && !children.isEmpty()) {
			for (TreeNode<String, Location> childValue : children.values()) {
				extractLocations(map, childValue);
			}
		}
	}
	
	public enum AllowedLevels {
		COUNTRY("Country"), PROVINCE("Province"), DISTRICT("District"), COUNTY("County"), SUB_COUNTY(
		        "Sub-county"), HEALTH_FACILITY("Health Facility");
		
		private final String display;
		
		private AllowedLevels(final String display) {
			this.display = display;
		}
		
		@Override
		public String toString() {
			return display;
		}
		
	};
	
	/**
	 * This method is used on OpenSRP web as a wrapper around implementation of fetching location by
	 * level and tags
	 * 
	 * @param uuid unique id of the location
	 * @param locationTopLevel highest level of location hierarchy to begin querying from
	 * @param locationTagsQueried OpenMRS tags to use when filtering the locations
	 * @return List of locations matching the specified tags
	 * @throws JSONException
	 */
	public List<Location> getLocationsByLevelAndTags(String uuid, String locationTopLevel, JSONArray locationTagsQueried)
	    throws JSONException {
		List<Location> allLocationsList = fetchLocationsHelper.getAllOpenMRSlocations();
		return getLocationsByLevelAndTagsFromAllLocationsList(uuid, allLocationsList, locationTopLevel, locationTagsQueried);
	}
	
	/**
	 * This method is used to obtain locations within a hierarchy level by passing the following
	 * parameters
	 * 
	 * @param uuid a uuid of a location within the hierarchy level, this is used for transversing to
	 *            the top location level stated below
	 * @param allLocations this is a list of all locations obtained from OpenMRS
	 * @param locationTopLevel this defines the top most level that locations to be querried from
	 *            e.g 1. for obtaining all locations within a district this value would contain the
	 *            tag name district locations 2. for obtaining all locations within a region this
	 *            value would contain the tag name for region locations
	 * @param locationTagsQueried this defines the tags of all the locations to be returned e.g for
	 *            obtaining all villages this json array would contain the tag name for village
	 *            locations for villages and health facilities, this json array would contain both
	 *            tag names
	 * @return returns a list of all locations matching the above criteria
	 */
	public List<Location> getLocationsByLevelAndTagsFromAllLocationsList(String uuid, List<Location> allLocations,
	                                                                     String locationTopLevel,
	                                                                     JSONArray locationTagsQueried) {
		Location location = null;
		for (Location allLocation : allLocations) {
			if (allLocation.getLocationId().equals(uuid)) {
				location = allLocation;
				break;
			}
		}
		
		if (location == null) {
			return new ArrayList<>();
		}
		
		if (!location.getTags().contains(locationTopLevel)) {
			return getLocationsByLevelAndTagsFromAllLocationsList(location.getParentLocation().getLocationId(), allLocations,
			    locationTopLevel, locationTagsQueried);
		}
		
		if (location.getTags().contains(locationTopLevel)) {
			return getChildLocationsTreeByTagsAndParentLocationUUID(location.getLocationId(), allLocations,
			    locationTagsQueried);
		} else {
			return new ArrayList<>();
		}
	}
	
	private List<Location> getChildLocationsTreeByTagsAndParentLocationUUID(String parentUUID, List<Location> allLocations,
	                                                                        JSONArray locationTagsQueried) {
		List<Location> obtainedLocations = new ArrayList<>();
		for (Location location : allLocations) {
			for (int i = 0; i < locationTagsQueried.length(); i++) {
				try {
					if (location.getParentLocation() != null
					        && location.getParentLocation().getLocationId().equals(parentUUID)
					        && location.getTags().contains(locationTagsQueried.getString(i))) {
						obtainedLocations.add(location);
					}
				}
				catch (Exception e) {
					logger.error(e.getMessage());
				}
			}
			
			if (location.getParentLocation() != null && location.getParentLocation().getLocationId().equals(parentUUID)) {
				obtainedLocations.addAll(getChildLocationsTreeByTagsAndParentLocationUUID(location.getLocationId(),
				    allLocations, locationTagsQueried));
			}
		}
		return obtainedLocations;
	}
	
	/**
	 * Fetch OpenMRS locations that are part of teams with the provided ids
	 * 
	 * @param openMRSTeamLocationsUUIDs ids for the teams
	 * @return a list of location for belonging to the passed teams
	 * @throws JSONException
	 */
	public JSONArray getLocationsByTeamIds(List<String> openMRSTeamLocationsUUIDs) throws JSONException {
		JSONArray teamMemberLocations = new JSONArray();
		JSONArray results = getAllTeamMemberLocations(new JSONArray(), 0);
		for (int i = 0; i < results.length(); i++) {
			try {
				JSONObject locationDetails = results.getJSONObject(i);
				JSONObject team = locationDetails.getJSONObject(ConnectorConstants.TEAM);
				JSONArray locations = locationDetails.getJSONArray(ConnectorConstants.LOCATIONS);
				for (int index = 0; index < locations.length(); index++) {
					if (team != null) {
						String teamLocationUUID = team.getJSONObject(ConnectorConstants.LOCATION)
						        .getString(ConnectorConstants.UUID);
						String locationUUID = locations.getJSONObject(index).getString(ConnectorConstants.UUID);
						if (openMRSTeamLocationsUUIDs.contains(teamLocationUUID)
						        && !locationUUID.equalsIgnoreCase(teamLocationUUID)) {
							teamMemberLocations.put(locationDetails);
						}
					}
				}
			}
			catch (Exception e) {
				logger.error("Error fetching locations: ", e);
			}
		}
		return teamMemberLocations;
	}
	
	/**
	 * Return all team member locations.
	 * 
	 * @return HttpResponse
	 * @param startIndex beginning query offset
	 */
	public JSONArray getAllTeamMemberLocations(JSONArray locationsList, int startIndex) {
		String response = HttpUtil
		        .get(HttpUtil.removeEndingSlash(OPENMRS_BASE_URL) + "/" + TEAM_MEMBER_URL.makeVariable(OPENMRS_VERSION),
		            "v=custom:(locations:(uuid,display),team:(location:(display,uuid)))&limit=100&startIndex=" + startIndex)
		        .body();
		if (!StringUtils.isBlank(response)) {
			JSONObject responseJsonObject = new JSONObject(response);
			if (responseJsonObject.has(ConnectorConstants.RESULTS)) {
				JSONArray results = responseJsonObject.getJSONArray(ConnectorConstants.RESULTS);
				for (int i = 0; i < results.length(); i++) {
					locationsList.put(results.getJSONObject(i));
				}
				return getAllTeamMemberLocations(locationsList, startIndex + 100);
			}
		}
		return locationsList;
	}
	
	/**
	 * Invokes helper method to clear all cached OpenMRS locations
	 */
	public void clearLocationsCache() {
		
		fetchLocationsHelper.clearAllOpenMRSlocationsCached();
		
	}
	
	/**
	 * Invokes helper method to create a cache of all OpenMRS locations
	 */
	public void createLocationsCache() {
		
		fetchLocationsHelper.getAllOpenMRSlocations();
		
	}
}
