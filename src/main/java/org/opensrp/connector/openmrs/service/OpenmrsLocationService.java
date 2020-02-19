package org.opensrp.connector.openmrs.service;

import java.io.IOException;
import java.util.*;

import com.google.gson.Gson;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.opensrp.api.domain.Location;
import org.opensrp.api.util.LocationTree;
import org.opensrp.api.util.TreeNode;
import org.opensrp.common.util.HttpUtil;
import org.opensrp.connector.openmrs.constants.ConnectorConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.mysql.jdbc.StringUtils;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Credentials;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

@Service
public class OpenmrsLocationService extends OpenmrsService {

	private static Logger logger = LoggerFactory.getLogger(OpenmrsLocationService.class);
	
	private static final String LOCATION_URL = "ws/rest/v1/location";
	
	public OpenmrsLocationService() {
	}
	
	public OpenmrsLocationService(String openmrsUrl, String user, String password) {
		super(openmrsUrl, user, password);
	}
	
	private String getURL(String url) {
		Request request = new Request.Builder().url(url)
		        .addHeader("Authorization", Credentials.basic(OPENMRS_USER, OPENMRS_PWD)).build();
		OkHttpClient client = new OkHttpClient();
		Call call = client.newCall(request);
		Response response;
		try {
			response = call.execute();
			String responseBody=response.body().string();
			if (!StringUtils.isEmptyOrWhitespaceOnly(responseBody)) {
				return responseBody;
			}
		}
		catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
		return null;
		
	}
	
	public Location getLocation(String locationIdOrName) throws JSONException {
		String response = getURL(HttpUtil.removeEndingSlash(OPENMRS_BASE_URL) + "/" + LOCATION_URL + "/"
		        + (locationIdOrName.replaceAll(" ", "%20")) + "?v=full");
		if (!StringUtils.isEmptyOrWhitespaceOnly(response) && (new JSONObject(response).has(ConnectorConstants.UUID))) {
			return makeLocation(response);
		}
		
		return null;
	}
	
	public Location getParent(JSONObject locobj) throws JSONException {
		JSONObject parentL = (locobj.has(ConnectorConstants.PARENT_LOCATION) && !locobj.isNull(ConnectorConstants.PARENT_LOCATION))
		        ? locobj.getJSONObject(ConnectorConstants.PARENT_LOCATION)
		        : null;
		
		if (parentL != null) {
			return new Location(parentL.getString(ConnectorConstants.UUID), parentL.getString(ConnectorConstants.DISPLAY), null, getParent(parentL));
		}
		return null;
	}
	
	private Location makeLocation(String locationJson) throws JSONException {
		logger.info("makeLocation: " + locationJson);
		JSONObject obj = new JSONObject(locationJson);
		Location p = getParent(obj);
		Location l = new Location(obj.getString(ConnectorConstants.UUID), obj.getString(ConnectorConstants.NAME), null, null, p, null, null);
		JSONArray t = obj.getJSONArray(ConnectorConstants.TAGS);
		
		for (int i = 0; i < t.length(); i++) {
			l.addTag(t.getJSONObject(i).getString(ConnectorConstants.DISPLAY));
		}
		
		JSONArray a = obj.getJSONArray(ConnectorConstants.ATTRIBUTES);
		
		for (int i = 0; i < a.length(); i++) {
			boolean voided = a.getJSONObject(i).optBoolean(ConnectorConstants.VOIDED);
			if (!voided) {
				String ad = a.getJSONObject(i).getString(ConnectorConstants.DISPLAY);
				l.addAttribute(ad.substring(0, ad.indexOf(":")), ad.substring(ad.indexOf(":") + 2));
			}
		}
		
		logger.info("location: " + ReflectionToStringBuilder.toString(l));
		return l;
	}
	
	private Location makeLocation(JSONObject location) throws JSONException {
		return makeLocation(location.toString());
	}
	
	public LocationTree getLocationTree() throws JSONException {
		LocationTree ltr = new LocationTree();
		String response = getURL(HttpUtil.removeEndingSlash(OPENMRS_BASE_URL) + "/" + LOCATION_URL+ "?v=full");
		
		JSONArray res = new JSONObject(response).getJSONArray(ConnectorConstants.RESULTS);
		if (res.length() == 0) {
			return ltr;
		}
		
		for (int i = 0; i < res.length(); i++) {
			ltr.addLocation(makeLocation(res.getJSONObject(i)));
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
			LoggerFactory.getLogger(this.getClass())
			        .info("getLocationTreeOf node: " + ReflectionToStringBuilder.toString(lp));
			if (lp != null) {
				fillTreeWithUpperHierarchy(ltr, lp.getLocationId());
			}
		}
		
		LoggerFactory.getLogger(this.getClass()).info("getLocationTreeOf tree: " + ReflectionToStringBuilder.toString(ltr));
		return ltr;
	}
	
	private String fillTreeWithHierarchy(LocationTree ltr, String locationIdOrName) throws JSONException {
		
		
		String response = getURL(HttpUtil.removeEndingSlash(OPENMRS_BASE_URL) + "/" + LOCATION_URL + "/"
		        + (locationIdOrName.replaceAll(" ", "%20"))+"?v=full");
		
		JSONObject lo = new JSONObject(response);
		Location l = makeLocation(response);
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
		
		String response = getURL( HttpUtil.removeEndingSlash(OPENMRS_BASE_URL) + "/" + LOCATION_URL + "/" + (locationId.replaceAll(" ", "%20"))+
		    "?v=full");
	
		Location l = makeLocation(response);
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
		COUNTRY("Country"),
		PROVINCE("Province"),
		DISTRICT("District"),
		COUNTY("County"),
		SUB_COUNTY("Sub-county"),
		HEALTH_FACILITY("Health Facility");
		
		private final String display;
		
		private AllowedLevels(final String display) {
			this.display = display;
		}
		
		@Override
		public String toString() {
			return display;
		}
		
	};

	public List<Location> getLocationsByLevelAndTags(String uuid, String locationTopLevel, JSONArray locationTagsQueried) throws JSONException {
		List<Location> allLocationsList = new ArrayList<>();
		allLocationsList = getAllLocations(allLocationsList,0);
		String locationsJson = new Gson().toJson(allLocationsList);
		logger.info(locationsJson);
		return getLocationsByLevelAndTagsFromAllLocationsList(uuid, allLocationsList,locationTopLevel,locationTagsQueried);
	}

	public List<Location> getAllLocations(List<Location> locationList, int startIndex) throws JSONException {
		String response = this.getURL(HttpUtil.removeEndingSlash(this.OPENMRS_BASE_URL) + "/" + LOCATION_URL +
				"?v=custom:(uuid,display,name,tags:(uuid,display),parentLocation:(uuid,display),attributes)&limit=100&startIndex="+startIndex);
		logger.info("response received : {} ", response);
		if (!StringUtils.isEmptyOrWhitespaceOnly(response) && (new JSONObject(response)).has(ConnectorConstants.RESULTS)) {
			JSONArray results = new JSONObject(response).getJSONArray(ConnectorConstants.RESULTS);
			for (int i = 0; i < results.length(); i++) {
				locationList.add(makeLocation(results.getJSONObject(i)));
			}
			return getAllLocations(locationList,startIndex+100);

		}
		return  locationList;
	}


	/**
	 * This method is used to obtain locations within a hierarchy level by passing the following parameters
	 * @param uuid a uuid of a location within the hierarchy level, this is used for transversing to the top location level stated below
	 * @param allLocations this is a list of all locations obtained from OpenMRS
	 * @param locationTopLevel this defines the top most level that locations to be querried from
	 *                         e.g
	 *                         1. for obtaining all locations within a district this value would contain the tag name
	 *                         district locations
	 *                         2. for obtaining all locations within a region this value would contain the tag name for
	 *                         region locations
	 *
	 * @param locationTagsQueried this defines the tags of all the locations to be returned
	 *                            e.g for obtaining all villages this json array would contain the tag name for village locations
	 *                            for villages and health facilities, this json array would contain both tag names
	 * @return returns a list of all locations matching the above criteria
	 */
	public List<Location> getLocationsByLevelAndTagsFromAllLocationsList(String uuid, List<Location> allLocations, String locationTopLevel, JSONArray locationTagsQueried) {
		Location location=null;
		for (Location allLocation : allLocations) {
			if (allLocation.getLocationId().equals(uuid)) {
				location = allLocation;
				break;
			}
		}

		if (location==null) {
			return new ArrayList<>();
		}

		if (!location.getTags().contains(locationTopLevel)) {
			return getLocationsByLevelAndTagsFromAllLocationsList(location.getParentLocation().getLocationId(), allLocations,locationTopLevel,locationTagsQueried);
		}

		if (location.getTags().contains(locationTopLevel)) {
			return getChildLocationsTreeByTagsAndParentLocationUUID(location.getLocationId(), allLocations,locationTagsQueried);
		} else {
			return new ArrayList<>();
		}
	}


	private List<Location> getChildLocationsTreeByTagsAndParentLocationUUID(String parentUUID, List<Location> allLocations,JSONArray locationTagsQueried) {
		List<Location> obtainedLocations = new ArrayList<>();
		for (Location location : allLocations) {
			for(int i=0;i<locationTagsQueried.length();i++){
				try {
					if (location.getParentLocation()!=null && location.getParentLocation().getLocationId().equals(parentUUID) && location.getTags().contains(locationTagsQueried.getString(i))) {
						obtainedLocations.add(location);
					}
				} catch (Exception e) {
					logger.error(e.getMessage());
				}
			}

			if (location.getParentLocation()!=null && location.getParentLocation().getLocationId().equals(parentUUID)) {
				obtainedLocations.addAll(getChildLocationsTreeByTagsAndParentLocationUUID(location.getLocationId(), allLocations, locationTagsQueried));
			}
		}
		return obtainedLocations;
	}
}
