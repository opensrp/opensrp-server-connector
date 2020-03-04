
package org.opensrp.connector.openmrs.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.opensrp.common.AllConstants.Event.OPENMRS_UUID_IDENTIFIER_TYPE;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.opensrp.common.util.HttpResponse;
import org.opensrp.common.util.HttpUtil;
import org.opensrp.connector.ConnectorTestConstants;
import org.opensrp.domain.Event;
import org.opensrp.domain.Obs;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith (PowerMockRunner.class)
@PrepareForTest ({HttpUtil.class})
@PowerMockIgnore ({"org.apache.http.conn.ssl.*", "javax.net.ssl.*"})
public class EncounterServiceTest extends TestResourceLoader {
	private EncounterService encounterService1;
	private HouseholdService householdService;
	
	public EncounterServiceTest() throws IOException {
		super();
	}
	
	@Before
	public void setUp() throws IOException {
		PowerMockito.mockStatic(HttpUtil.class);
		PatientService patientService1 = new PatientService(openmrsOpenmrsUrl, openmrsUsername, openmrsPassword);
		OpenmrsUserService openmrsUserService = new OpenmrsUserService(openmrsOpenmrsUrl, openmrsUsername, openmrsPassword);
		encounterService1 = new EncounterService(openmrsOpenmrsUrl, openmrsUsername, openmrsPassword,openmrsVersion);
		encounterService1.setPatientService(patientService1);
		encounterService1.setUserService(openmrsUserService);
		householdService = new HouseholdService(openmrsOpenmrsUrl, openmrsUsername, openmrsPassword);
		householdService.setPatientService(patientService1);
		householdService.setEncounterService(encounterService1);
	}
	
	
	@Test
	public void testCreateEncounter() throws JSONException {
		JSONObject obsObject = new JSONObject(ConnectorTestConstants.OBS_ARRAY);
		assertNotNull(obsObject);
		
		JSONArray obsArray = obsObject.getJSONArray("obs");
		assertNotNull(obsArray);
		assertEquals(12, obsArray.length());
		
		
		Event event = new Event().withEventType("Quick Check")
				.withIdentifier(OPENMRS_UUID_IDENTIFIER_TYPE, "2d37543f-bae5-462b-a12e-b29b5dfcff65")
				.withEventDate(new DateTime())
				.withObs(generateObsList(obsArray))
				.withBaseEntityId("191cea0c-53e3-49ea-97c1-ec6d7cd5c3cf")
				.withProviderId("demo")
				.withLocationId("44de66fb-e6c6-4bae-92bb-386dfe626eba")
				.withEntityType("ec_woman");
		assertNotNull(event);
		BDDMockito.given(HttpUtil.get(any(String.class), any(String.class), any(String.class), any(String.class))).willReturn(new HttpResponse(true, "{\n" +
				"        \"results\" : [\n" +
				"          {\n" +
				"            \"uuid\": \"191cea0c-53e3-49ea-97c1-ec6d7cd5c3cf\"\n" +
				"          }\n" +
				"        ]\n" +
				"      }"));
		
		
		BDDMockito.given(HttpUtil.get(any(String.class), any(String.class), any(String.class),
				any(String.class))).willReturn(new HttpResponse(true,
				"{\"results\":[{\"uuid\":\"baa5c5d3-cebe-11e4-9a12-040144de7001\",\"display\":\"admin\",\"username\":\"admin\",\"systemId\":\"admin\",\"privileges\":[],\"roles\":[],\"userProperties\":{\"showRetired\":\"false\",\"defaultLocation\":\"\",\"showVerbose\":\"false\",\"notification\":\"\",\"notificationAddress\":\"\",\"loginAttempts\":\"0\"},\"person\":{\"uuid\":\"aeb5ecd0-cebe-11e4-9a12-040144de7001\",\"display\":\"Super User\",\"gender\":\"M\",\"preferredName\":{\"uuid\":\"aebebad3-cebe-11e4-9a12-040144de7001\",\"display\":\"Super User\"},\"attributes\":[{\"uuid\":\"65040584-b558-4b2a-b73d-f6e681839492\",\"display\":\"Health Center = 2\"},{\"uuid\":\"0f60bb3d-abf8-407e-88fd-2da4b49afef9\",\"display\":\"Location = cd4ed528-87cd-42ee-a175-5e7089521ebd\"}]}}]}"));
		BDDMockito.given(HttpUtil.delete(any(String.class), any(String.class), any(String.class), any(String.class)))
				.willReturn(new HttpResponse(true, "{\"results\":{}"));
		BDDMockito.given(HttpUtil.post(any(String.class),eq(""),any(String.class), any(String.class), any(String.class))).willReturn(new HttpResponse(true, ConnectorTestConstants.QUICK_ENCOUNTER));
		
		
		JSONObject encounter = encounterService1.createEncounter(event);
		assertNotNull(encounter);
		assertEquals(7,encounter.getJSONArray("obs").length());
		assertEquals(encounter.getString("patient"), "55be5788-02f9-4b78-a71c-3dacb6bcb510");
		assertEquals(encounter.getString("location"), "44de66fb-e6c6-4bae-92bb-386dfe626eba");
		assertEquals(encounter.getString("encounterType"), "Quick Check");
	}
	
	private List<Obs> generateObsList(JSONArray jsonArray) throws JSONException {
		List<Obs> obsList = new ArrayList<>();
		if (jsonArray != null) {
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject object = jsonArray.getJSONObject(i);
				if (object != null) {
					Obs obs = new Obs();
					obs.setValues(getObjectList(object.getJSONArray("values")));
					obs.setFieldCode(object.getString("fieldCode"));
					obs.setFieldType(object.getString("fieldType"));
					obs.setFieldDataType(object.getString("fieldDataType"));
					obs.setFormSubmissionField(object.getString("formSubmissionField"));
					obs.setParentCode(object.getString("parentCode"));
					obs.setHumanReadableValues(getObjectList(object.getJSONArray("humanReadableValues")));
					obsList.add(obs);
				}
			}
		}
		
		return obsList;
	}
	
	private List<Object> getObjectList(JSONArray jsonArray) throws JSONException {
		List<Object> objectList = new ArrayList<>();
		if (jsonArray != null) {
			for (int i = 0; i < jsonArray.length(); i++) {
				Object object = jsonArray.get(i);
				objectList.add(object);
			}
		}
		
		return objectList;
	}
	/*@Test
	public void testUpdateEncounter() throws JSONException {
		// mock call to get obs uuids for encounter
		openmrsUserService = mock(OpenmrsUserService.class);
		patientService1 = mock(PatientService.class);
		
		encounterService.setUserService(openmrsUserService);
		encounterService.setPatientService(patientService1);
		
		String username = "DLucia";
		JSONObject providerDLucia = new JSONObject();
		providerDLucia.put("uuid", "13daa865-9df7-4062-8b32-9b0b42b27d41");
		JSONObject clientPatient = new JSONObject();
		clientPatient.put("uuid", "fbb1ea28-2ea2-4bcb-bbc5-948f5699f688");
		
		when(openmrsUserService.getPersonByUser(username)).thenReturn(providerDLucia);
		when(patientService1.getPatientByIdentifier(anyString())).thenReturn(clientPatient);
		
		// create a client
		Client client = new Client("fbb1ea28-2ea2-4bcb-bbc5-948f5699f688");
	
		// create test event encounter with encounter uuid
		Obs startObs = new Obs("concept", "start", "163137AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "", "2017-03-20 16:29:16", "", "start");
		Obs endObs = new Obs("concept", "end", "163138AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "", "2017-03-20 16:38:17", "", "end");
		Obs deviceObs = new Obs("concept", "deviceid", "163149AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "", "enketo.ona.io:Z1Sr2fifsKBExXgX", "", "deviceid");
		
		String vaccine = "opv";
		String date = "2017-03-21";
		String fieldType = "concept";
		String dateFieldDataType = "date";
		String calculateFieldDataType = "calculate";
		String dateFieldCode = "1410AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
		String calculateFieldCode = "1418AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
		String parentCode = "783AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
		String sequence = "2";
		String formSubmissionField1 = vaccine + "_" + sequence;
		String formSubmissionField2 = vaccine + "_" + sequence + "_dose";
		List<Object> values1 = new ArrayList<Object>();
		values1.add(date);
		List<Object> values2 = new ArrayList<Object>();
		values2.add(sequence);
		
		Obs bcgDateObs = new Obs(fieldType, dateFieldDataType, dateFieldCode, parentCode, values1, null, formSubmissionField1);
		Obs bcgCalculateObs = new Obs(fieldType, calculateFieldDataType, calculateFieldCode, parentCode, values2, null, formSubmissionField2);
		
		DateTime eventDate = DateTimeFormat.forPattern("yyyy-MM-dd").parseDateTime(date);
		
		Event event = new Event(client.getBaseEntityId(),
								"Vaccination",
								eventDate,
								"vaccination",
								"DLucia",
								"5bf3b4ca-9482-4e85-ab7a-0c44e4edb329",
								"acb135c3-b501-4c12-9beb-d13b8b11deeb");
		event.addObs(startObs);
		event.addObs(endObs);
		event.addObs(deviceObs);
		event.addObs(bcgDateObs);
		event.addObs(bcgCalculateObs);
		event.addIdentifier(EncounterService.OPENMRS_UUID_IDENTIFIER_TYPE, "ff00a625-5309-4c12-a2cb-38e4985d9a94");
		
		JSONObject updatedEncounter = encounterService.buildUpdateEncounter(event);
		
		// check that all obs have the expected uuid
		System.out.println("[updatedEncounter]" + updatedEncounter);
		JSONArray obsArray = updatedEncounter.getJSONArray("obs");
		
		assertEquals(obsArray.length(), 4);
		
		for(int i = 0; i < obsArray.length(); i++) {
			JSONObject obs = obsArray.getJSONObject(i);
			assertNotEquals(obs.get("uuid"), "");
			
			if(obs.has("groupMembers")) {
				JSONArray groupMembers = obs.getJSONArray("groupMembers");
				for(int k = 0; k < groupMembers.length(); k++) {
					assertNotEquals(obs.get("uuid"), "");
				}
			}
		}
		
		encounterService.updateEncounter(event);
		
	}*/
}
