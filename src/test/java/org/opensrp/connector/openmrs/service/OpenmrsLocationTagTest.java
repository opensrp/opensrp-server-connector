package org.opensrp.connector.openmrs.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;

import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.opensrp.common.util.HttpResponse;
import org.opensrp.common.util.HttpUtil;
import org.opensrp.connector.openmrs.service.it.OpenmrsApiService;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ HttpUtil.class })
@PowerMockIgnore({ "org.apache.http.conn.ssl.*", "javax.net.ssl.*" })
public class OpenmrsLocationTagTest extends OpenmrsApiService {
	
	private OpenmrsLocationTagService openmrsLocationTagService;
	
	public OpenmrsLocationTagTest() throws IOException {
		super();
	}
	
	@Before
	public void setUp() throws IOException {
		PowerMockito.mockStatic(HttpUtil.class);
		openmrsLocationTagService = new OpenmrsLocationTagService(openmrsOpenmrsUrl, openmrsUsername, openmrsPassword);
	}
	
	@Ignore
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
			
		}
		
	}
	
	@Test
	public void testSearchLocationTag() throws JSONException {
		BDDMockito
		        .given(HttpUtil.get(any(String.class), any(String.class), any(String.class), any(String.class)))
		        .willReturn(
		            new HttpResponse(
		                    true,
		                    "{\"results\":[{\"auditInfo\":{\"dateChanged\":\"2020-01-22T13:49:08.000+0600\",\"creator\":{\"display\":\"mpower\",\"links\":[{\"rel\":\"self\",\"uri\":\"http://192.168.19.165:8080/openmrs/ws/rest/v1/user/f9699494-9d53-4646-b522-ee96a16575da\"}],\"uuid\":\"f9699494-9d53-4646-b522-ee96a16575da\"},\"dateCreated\":\"2020-01-22T13:49:08.000+0600\",\"changedBy\":{\"display\":\"mpower\",\"links\":[{\"rel\":\"self\",\"uri\":\"http://192.168.19.165:8080/openmrs/ws/rest/v1/user/f9699494-9d53-4646-b522-ee96a16575da\"}],\"uuid\":\"f9699494-9d53-4646-b522-ee96a16575da\"}},\"display\":\"TestLocatinTagUpdate\",\"resourceVersion\":\"1.8\",\"name\":\"TestLocatinTagUpdate\",\"description\":null,\"retired\":false,\"links\":[{\"rel\":\"self\",\"uri\":\"http://192.168.19.165:8080/openmrs/ws/rest/v1/locationtag/4a53a8d4-dab2-42bd-88af-52a0883c153f\"}],\"uuid\":\"4a53a8d4-dab2-42bd-88af-52a0883c153f\"}]}"));
		JSONObject searchResponse = new JSONObject();
		searchResponse = openmrsLocationTagService.searchLocationTags("admin", 0, 0);
		JSONArray responseArray = new JSONArray();
		responseArray = searchResponse.getJSONArray("results");
		JSONObject responseObject = (JSONObject) responseArray.get(0);
		assertEquals(locationTagnameUpdated, responseObject.get("name"));
		
	}
	
}
