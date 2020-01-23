package org.opensrp.connector.openmrs.service;

import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.opensrp.common.util.HttpResponse;
import org.opensrp.common.util.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class OpenmrsAPIService extends OpenmrsService {
	
	private static Logger logger = LoggerFactory.getLogger(OpenmrsAPIService.class);
	
	public OpenmrsAPIService() {
	}
	
	public OpenmrsAPIService(String openmrsUrl, String user, String password) {
		super(openmrsUrl, user, password);
	}
	
	public JSONObject add(String payload, String url) {
		JSONObject response = new JSONObject();
		try {
			response = new JSONObject(HttpUtil.post(getURL() + "/" + url, "", payload, OPENMRS_USER, OPENMRS_PWD).body());
		}
		catch (JSONException e) {
			logger.error(e.fillInStackTrace() + "");
		}
		return response;
	}
	
	/**
	 * user role can't be updated
	 */
	public JSONObject update(String payload, String uuid, String url) {
		JSONObject response = new JSONObject();
		try {
			response = new JSONObject(HttpUtil.post(getURL() + "/" + url + "/" + uuid, "", payload, OPENMRS_USER,
			    OPENMRS_PWD).body());
		}
		catch (JSONException e) {
			logger.error(e.fillInStackTrace() + "");
		}
		return response;
	}
	
	public Integer delete(String uuid, String url) {
		HttpResponse response = HttpUtil.delete(getURL() + "/" + url + "/" + uuid, "purge=true", OPENMRS_USER, OPENMRS_PWD);
		return response.statusCode();
	}
	
	public JSONObject list(String url) {
		JSONObject response = new JSONObject();
		try {
			response = new JSONObject(HttpUtil.get(getURL() + "/" + url, "v=full", OPENMRS_USER, OPENMRS_PWD).body());
		}
		catch (JSONException e) {
			
		}
		
		return response;
	}
	
	/**
	 * This method is not available for role,privilege etc
	 */
	public JSONObject listBySearch(String name, int limit, int startIndex, String url) {
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
			response = new JSONObject(HttpUtil.get(getURL() + "/" + url, "v=full" + limitQuery + startIndexQuery + query,
			    OPENMRS_USER, OPENMRS_PWD).body());
		}
		catch (JSONException e) {
			
		}
		
		return response;
	}
}
