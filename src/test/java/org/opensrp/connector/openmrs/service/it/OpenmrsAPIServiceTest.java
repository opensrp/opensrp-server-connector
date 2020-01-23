package org.opensrp.connector.openmrs.service.it;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.opensrp.connector.openmrs.service.OpenmrsAPIService;

public class OpenmrsAPIServiceTest extends OpenmrsApiService {
	
	private OpenmrsAPIService openmrsAPIService;
	
	private static final String URL = "ws/rest/v1/location";
	
	public OpenmrsAPIServiceTest() throws IOException {
		super();
	}
	
	@Before
	public void setUp() throws IOException {
		openmrsAPIService = new OpenmrsAPIService(openmrsOpenmrsUrl, openmrsUsername, openmrsPassword);
	}
	
	@Test
	public void addUpdateDeleteListBySearchForLocation() {
		String uuid = "";
		try {
			JSONObject response = openmrsAPIService.add(makeLocationsString(LocationName), URL);
			String locationTagName = response.getString(nameKey);
			assertEquals(LocationName, locationTagName);
			uuid = response.getString(uuidKey);
			JSONObject updateResponse = openmrsAPIService.update(makeLocationsString(LocationNameUpdated), uuid, URL);
			String updatedName = updateResponse.getString(nameKey);
			assertEquals(LocationNameUpdated, updatedName);
			shouldListBySearch(LocationNameUpdated);
			uuid = response.getString(uuidKey);
			Integer statusCode = openmrsAPIService.delete(uuid, URL);
			assertEquals(204, statusCode.intValue());
		}
		catch (JSONException e) {
			
		}
	}
	
	private void shouldListBySearch(String name) {
		JSONObject searchResponse = new JSONObject();
		searchResponse = openmrsAPIService.listBySearch(name, 0, 0, URL);
		JSONArray responseArray = new JSONArray();
		try {
			responseArray = searchResponse.getJSONArray("results");
			assertEquals(1, responseArray.length());
			JSONObject responseObject = (JSONObject) responseArray.get(0);
			assertEquals(LocationNameUpdated, responseObject.get("name"));
		}
		catch (JSONException e) {
			
		}
	}
	
	private String makeLocationsString(String name) throws JSONException {
		JSONObject location = new JSONObject();
		location.put(nameKey, name);
		List<String> tags = new ArrayList<String>();
		tags.add("a675e840-d225-11e4-9c67-080027b662ec");
		location.put("tags", tags);
		return location.toString();
	}
	
}
