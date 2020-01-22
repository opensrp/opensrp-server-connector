package org.opensrp.connector.openmrs.service.it;

import static junit.framework.Assert.assertEquals;

import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.opensrp.connector.openmrs.service.OpenmrsLocationTagService;

public class OpenmrsLocationTagTest extends OpenmrsApiService {
	
	private OpenmrsLocationTagService openmrsLocationTagService;
	
	public OpenmrsLocationTagTest() throws IOException {
		super();
	}
	
	@Before
	public void setUp() throws IOException {
		openmrsLocationTagService = new OpenmrsLocationTagService(openmrsOpenmrsUrl, openmrsUsername, openmrsPassword);
	}
	
	@Test
	public void addUpdateDeleteSearchLocationTag() {
		String uuid = "";
		try {
			JSONObject response = openmrsLocationTagService.addLocationTag(locationTagname);
			String locationTagName = response.getString(nameKey);
			assertEquals(locationTagname, locationTagName);
			uuid = response.getString(uuidKey);
			JSONObject updateResponse = openmrsLocationTagService.updateLocationTag(locationTagnameUpdated, uuid);
			String updatedLocationTagName = updateResponse.getString(nameKey);
			assertEquals(locationTagnameUpdated, updatedLocationTagName);
			searchLocationTags(locationTagnameUpdated);
			uuid = response.getString(uuidKey);
			Integer statusCode = openmrsLocationTagService.deleteLocationTag(uuid);
			assertEquals(204, statusCode.intValue());
			
		}
		catch (JSONException e) {
			
		}
	}
	
	private void searchLocationTags(String name) {
		JSONObject searchResponse = new JSONObject();
		searchResponse = openmrsLocationTagService.searchLocationTags(name, 0, 0);
		JSONArray responseArray = new JSONArray();
		try {
			responseArray = searchResponse.getJSONArray("results");
			assertEquals(1, responseArray.length());
			JSONObject responseObject = (JSONObject) responseArray.get(0);
			assertEquals(locationTagnameUpdated, responseObject.get("name"));
			
		}
		catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
}
