package org.opensrp.connector.openmrs.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
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
public class OpenmrsAPIServiceTest extends OpenmrsApiService {
	
	private OpenmrsAPIService openmrsAPIService;
	
	private static final String URL = "ws/rest/v1/location";
	
	public OpenmrsAPIServiceTest() throws IOException {
		super();
	}
	
	@Before
	public void setUp() throws IOException {
		PowerMockito.mockStatic(HttpUtil.class);
		openmrsAPIService = new OpenmrsAPIService(openmrsOpenmrsUrl, openmrsUsername, openmrsPassword);
	}
	
	@Test
	public void shouldAdd() throws JSONException {
		BDDMockito
		        .given(
		            HttpUtil.post(any(String.class), any(String.class), any(String.class), any(String.class),
		                any(String.class)))
		        .willReturn(
		            new HttpResponse(
		                    true,
		                    "{\"country\":null,\"parentLocation\":null,\"countyDistrict\":null,\"postalCode\":null,\"latitude\":null,\"description\":null,\"uuid\":\"150e2d8a-d7d7-4694-988e-c5d0f31cfde2\",\"address7\":null,\"address6\":null,\"address5\":null,\"retired\":false,\"links\":[{\"rel\":\"self\",\"uri\":\"http://192.168.19.165:8080/openmrs/ws/rest/v1/location/150e2d8a-d7d7-4694-988e-c5d0f31cfde2\"},{\"rel\":\"full\",\"uri\":\"http://192.168.19.165:8080/openmrs/ws/rest/v1/location/150e2d8a-d7d7-4694-988e-c5d0f31cfde2?v=full\"}],\"address4\":null,\"address9\":null,\"longitude\":null,\"address8\":null,\"address3\":null,\"address2\":null,\"address1\":null,\"display\":\"Test location\",\"resourceVersion\":\"2.0\",\"stateProvince\":null,\"cityVillage\":null,\"tags\":[{\"display\":\"Admission Location\",\"links\":[{\"rel\":\"self\",\"uri\":\"http://192.168.19.165:8080/openmrs/ws/rest/v1/locationtag/a675e840-d225-11e4-9c67-080027b662ec\"}],\"uuid\":\"a675e840-d225-11e4-9c67-080027b662ec\"}],\"address10\":null,\"address11\":null,\"address12\":null,\"address13\":null,\"address14\":null,\"name\":\"Test location\",\"address15\":null,\"attributes\":[],\"childLocations\":null}"));
		
		JSONObject response = openmrsAPIService.add(makeLocationsString(LocationName), URL);
		String locationTagName = response.getString(nameKey);
		assertEquals(LocationName, locationTagName);
	}
	
	@Test
	public void shouldDelete() throws JSONException {
		BDDMockito.given(HttpUtil.delete(any(String.class), any(String.class), any(String.class), any(String.class)))
		        .willReturn(new HttpResponse(true, 204, ""));
		assertEquals(204, openmrsAPIService.delete("1234", URL).intValue());
	}
	
	@Test
	public void shouldsearch() throws JSONException {
		BDDMockito
		        .given(HttpUtil.get(any(String.class), any(String.class), any(String.class), any(String.class)))
		        .willReturn(
		            new HttpResponse(
		                    true,
		                    "{\"results\":[{\"country\":null,\"parentLocation\":null,\"countyDistrict\":null,\"postalCode\":null,\"latitude\":null,\"description\":null,\"uuid\":\"150e2d8a-d7d7-4694-988e-c5d0f31cfde2\",\"auditInfo\":{\"dateChanged\":\"2020-01-23T15:59:04.000+0600\",\"creator\":{\"display\":\"mpower\",\"links\":[{\"rel\":\"self\",\"uri\":\"http://192.168.19.165:8080/openmrs/ws/rest/v1/user/f9699494-9d53-4646-b522-ee96a16575da\"}],\"uuid\":\"f9699494-9d53-4646-b522-ee96a16575da\"},\"dateCreated\":\"2020-01-23T15:59:04.000+0600\",\"changedBy\":{\"display\":\"mpower\",\"links\":[{\"rel\":\"self\",\"uri\":\"http://192.168.19.165:8080/openmrs/ws/rest/v1/user/f9699494-9d53-4646-b522-ee96a16575da\"}],\"uuid\":\"f9699494-9d53-4646-b522-ee96a16575da\"}},\"address7\":null,\"address6\":null,\"address5\":null,\"retired\":false,\"links\":[{\"rel\":\"self\",\"uri\":\"http://192.168.19.165:8080/openmrs/ws/rest/v1/location/150e2d8a-d7d7-4694-988e-c5d0f31cfde2\"}],\"address4\":null,\"address9\":null,\"longitude\":null,\"address8\":null,\"address3\":null,\"address2\":null,\"address1\":null,\"display\":\"Test location updated\",\"resourceVersion\":\"2.0\",\"stateProvince\":null,\"cityVillage\":null,\"tags\":[{\"display\":\"Admission Location\",\"resourceVersion\":\"1.8\",\"name\":\"Admission Location\",\"description\":\"General Ward Patients\",\"retired\":false,\"links\":[{\"rel\":\"self\",\"uri\":\"http://192.168.19.165:8080/openmrs/ws/rest/v1/locationtag/a675e840-d225-11e4-9c67-080027b662ec\"},{\"rel\":\"full\",\"uri\":\"http://192.168.19.165:8080/openmrs/ws/rest/v1/locationtag/a675e840-d225-11e4-9c67-080027b662ec?v=full\"}],\"uuid\":\"a675e840-d225-11e4-9c67-080027b662ec\"}],\"address10\":null,\"address11\":null,\"address12\":null,\"address13\":null,\"address14\":null,\"name\":\"Test location updated\",\"address15\":null,\"attributes\":[],\"childLocations\":[]}]}"));
		
		JSONObject response = openmrsAPIService.listBySearch(LocationNameUpdated, 0, 10, URL);
		JSONArray result = response.getJSONArray("results");
		assertEquals(1, result.length());
		JSONObject role = result.getJSONObject(0);
		String roleName = role.getString(nameKey);
		assertEquals(LocationNameUpdated, roleName);
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
