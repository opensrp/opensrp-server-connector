package org.opensrp.connector.openmrs.service;

import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.opensrp.common.util.HttpResponse;
import org.opensrp.common.util.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpenmrsLocationTagService extends OpenmrsService {
	
	private static Logger logger = LoggerFactory.getLogger(OpenmrsLocationService.class);
	
	public final static String nameKey = "name";
	
	private static final String LOCATIONTAG_URL = "ws/rest/v1/locationtag";
	
	public OpenmrsLocationTagService() {
	}
	
	public OpenmrsLocationTagService(String openmrsUrl, String user, String password) {
		super(openmrsUrl, user, password);
	}
	
	public JSONObject addLocationTag(String name) {
		JSONObject response = new JSONObject();
		try {
			response = new JSONObject(HttpUtil.post(getURL() + "/" + LOCATIONTAG_URL, "", makeTagsObject(name).toString(),
			    OPENMRS_USER, OPENMRS_PWD).body());
			System.err.println(response);
		}
		catch (JSONException e) {
			logger.error(e.fillInStackTrace() + "");
		}
		return response;
	}
	
	public JSONObject updateLocationTag(String name, String uuid) {
		JSONObject response = new JSONObject();
		try {
			response = new JSONObject(HttpUtil.post(getURL() + "/" + LOCATIONTAG_URL + "/" + uuid, "",
			    makeTagsObject(name).toString(), OPENMRS_USER, OPENMRS_PWD).body());
			System.err.println(response);
		}
		catch (JSONException e) {
			logger.error(e.fillInStackTrace() + "");
		}
		return response;
	}
	
	public Integer deleteLocationTag(String uuid) {
		HttpResponse response = HttpUtil.delete(getURL() + "/" + LOCATIONTAG_URL + "/" + uuid, "purge=true", OPENMRS_USER,
		    OPENMRS_PWD);
		return response.statusCode();
	}
	
	private JSONObject makeTagsObject(String name) throws JSONException {
		JSONObject tag = new JSONObject();
		tag.put(nameKey, name);
		return tag;
	}
	
	public JSONObject searchLocationTags(String name, int limit, int startIndex) {
		JSONObject response = new JSONObject();
		Map<String, String> getURlQuery = getURlQuery(name, limit, startIndex);
		String query = "";
		String limitQuery = "";
		String startIndexQuery = "";
		if (getURlQuery.containsKey("query"))
			query = getURlQuery.get("query");
		if (getURlQuery.containsKey("limitQuery"))
			limitQuery = getURlQuery.get("limitQuery");
		if (getURlQuery.containsKey("startIndexQuery"))
			startIndexQuery = getURlQuery.get("startIndexQuery");
		try {
			response = new JSONObject(HttpUtil.get(getURL() + "/" + LOCATIONTAG_URL,
			    "v=full" + limitQuery + startIndexQuery + query, OPENMRS_USER, OPENMRS_PWD).body());
		}
		catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return response;
	}
}
