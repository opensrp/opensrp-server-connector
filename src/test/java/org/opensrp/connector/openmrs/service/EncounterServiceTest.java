
package org.opensrp.connector.openmrs.service;

import com.google.gson.JsonIOException;

import org.hamcrest.Matchers;
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
import org.opensrp.connector.openmrs.constants.OpenmrsHouseHold;
import org.opensrp.domain.Client;
import org.opensrp.domain.Event;
import org.opensrp.domain.Obs;
import org.opensrp.form.domain.FormSubmission;
import org.opensrp.form.service.FormAttributeParser;
import org.opensrp.service.formSubmission.FormEntityConverter;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.opensrp.common.AllConstants.Event.OPENMRS_UUID_IDENTIFIER_TYPE;

@RunWith (PowerMockRunner.class)
@PrepareForTest ({HttpUtil.class})
@PowerMockIgnore ({"org.apache.http.conn.ssl.*", "javax.net.ssl.*"})
public class EncounterServiceTest extends TestResourceLoader {
	
	public EncounterServiceTest() throws IOException {
		super();
	}
	
	private EncounterService encounterService1;
	private FormEntityConverter formEntityConverter;
	private PatientService patientService1;
	private OpenmrsUserService openmrsUserService;
	private HouseholdService householdService;
	
	SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd");
	
	@Before
	public void setup() throws IOException {
		PowerMockito.mockStatic(HttpUtil.class);
		patientService1 = new PatientService(openmrsOpenmrsUrl, openmrsUsername, openmrsPassword);
		openmrsUserService = new OpenmrsUserService(openmrsOpenmrsUrl, openmrsUsername, openmrsPassword);
		encounterService1 = new EncounterService(openmrsOpenmrsUrl, openmrsUsername, openmrsPassword,openmrsVersion);
		encounterService1.setPatientService(patientService1);
		encounterService1.setUserService(openmrsUserService);
		householdService = new HouseholdService(openmrsOpenmrsUrl, openmrsUsername, openmrsPassword);
		householdService.setPatientService(patientService1);
		householdService.setEncounterService(encounterService1);
		FormAttributeParser fam = new FormAttributeParser(formDirPath);
		formEntityConverter = new FormEntityConverter(fam);
	}
	
	@Test
	public void testEncounter() throws JSONException, IOException {
		FormSubmission fs = getFormSubmissionFor("basic_reg");
		
		Client c = formEntityConverter.getClientFromFormSubmission(fs);
		assertEquals(c.getBaseEntityId(), "b716d938-1aea-40ae-a081-9ddddddcccc9");
		assertEquals(c.getFirstName(), "test woman_name");
		assertEquals(c.getGender(), "FEMALE");
		assertEquals(c.getAddresses().get(0).getAddressType(), "birthplace");
		assertEquals(c.getAddresses().get(1).getAddressType(), "usual_residence");
		assertEquals(c.getAddresses().get(2).getAddressType(), "previous_residence");
		assertEquals(c.getAddresses().get(3).getAddressType(), "deathplace");
		assertTrue(c.getAttributes().isEmpty());
		
		Event e = formEntityConverter.getEventFromFormSubmission(fs);
		assertEquals(e.getEventType(), "patient_register");
		assertEquals(e.getEventDate(), new DateTime(new DateTime("2015-02-01")));
		assertEquals(e.getLocationId(), "unknown location");
		
		if (pushToOpenmrsForTest) {
			JSONObject en = encounterService1.createEncounter(e);
			System.out.println(en);
		}
	}
	
	@Test
	public void testGroupedEncounter() throws IOException {
		FormSubmission fs = getFormSubmissionFor("repeatform");
		
		Client c = formEntityConverter.getClientFromFormSubmission(fs);
		//TODO		
		Event e = formEntityConverter.getEventFromFormSubmission(fs);
		//TODO
		/*if(true){
			JSONObject p = patientService1.getPatientByIdentifier(c.getBaseEntityId());
			if(p == null){
				p = patientService1.createPatient(c);
			}
			JSONObject en = encounterService1.createEncounter(e);
			System.out.println(en);
		}*/
	}
	
	@Test
	public void shouldHandleSubform() throws IOException {
		FormSubmission fs = getFormSubmissionFor("new_household_registration", 1);
		
		Client c = formEntityConverter.getClientFromFormSubmission(fs);
		assertEquals(c.getBaseEntityId(), "a3f2abf4-2699-4761-819a-cea739224164");
		assertEquals(c.getFirstName(), "test");
		assertEquals(c.getGender(), "male");
		assertEquals(c.getBirthdate(), new DateTime("1900-01-01"));
		assertEquals(c.getAddresses().get(0).getAddressField("landmark"), "nothing");
		assertEquals(c.getAddresses().get(0).getAddressType(), "usual_residence");
		assertEquals(c.getIdentifiers().get("GOB HHID"), "1234");
		assertEquals(c.getIdentifiers().get("JiVitA HHID"), "1234");
		
		Event e = formEntityConverter.getEventFromFormSubmission(fs);
		assertEquals(e.getBaseEntityId(), "a3f2abf4-2699-4761-819a-cea739224164");
		assertEquals(e.getEventDate(), new DateTime(new DateTime("2015-05-07")));
		assertEquals(e.getLocationId(), "KUPTALA");
		assertEquals(e.getFormSubmissionId(), "88c0e824-10b4-44c2-9429-754b8d823776");
		
		assertEquals(e.getObs().get(0).getFieldCode(), "160753AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
		assertEquals(e.getObs().get(0).getFormSubmissionField(), "FWNHREGDATE");
		assertEquals(e.getObs().get(0).getValue(), "2015-05-07");
		
		assertEquals(e.getObs().get(1).getFieldCode(), "5611AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
		assertEquals(e.getObs().get(1).getFormSubmissionField(), "FWNHHMBRNUM");
		assertEquals(e.getObs().get(1).getValue(), "2");
		
		Map<String, Map<String, Object>> dc = formEntityConverter.getDependentClientsFromFormSubmission(fs);
		for (String id : dc.keySet()) {
			Client cl = (Client) dc.get(id).get("client");
			Event ev = (Event) dc.get(id).get("event");
			assertEquals(cl.getBaseEntityId(), id);
			assertEquals(ev.getBaseEntityId(), id);
		}
	}
	
	@Test
	public void shouldHandleEmptyRepeatGroup() throws IOException {
		FormSubmission fs = getFormSubmissionFor("new_household_registration", 5);
		
		Client c = formEntityConverter.getClientFromFormSubmission(fs);
		assertEquals(c.getBaseEntityId(), "a3f2abf4-2699-4761-819a-cea739224164");
		assertEquals(c.getFirstName(), "test");
		assertEquals(c.getGender(), "male");
		assertEquals(c.getBirthdate(), new DateTime("1900-01-01"));
		assertEquals(c.getAddresses().get(0).getAddressField("landmark"), "nothing");
		assertEquals(c.getAddresses().get(0).getAddressType(), "usual_residence");
		assertEquals(c.getIdentifiers().get("GOB HHID"), "1234");
		assertEquals(c.getIdentifiers().get("JiVitA HHID"), "1234");
		
		Event e = formEntityConverter.getEventFromFormSubmission(fs);
		assertEquals(e.getBaseEntityId(), "a3f2abf4-2699-4761-819a-cea739224164");
		assertEquals(e.getEventDate(), new DateTime(new DateTime("2015-05-07")));
		assertEquals(e.getLocationId(), "KUPTALA");
		assertEquals(e.getFormSubmissionId(), "88c0e824-10b4-44c2-9429-754b8d823776");
		
		assertEquals(e.getObs().get(0).getFieldCode(), "160753AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
		assertEquals(e.getObs().get(0).getFormSubmissionField(), "FWNHREGDATE");
		assertEquals(e.getObs().get(0).getValue(), "2015-05-07");
		
		assertEquals(e.getObs().get(1).getFieldCode(), "5611AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
		assertEquals(e.getObs().get(1).getFormSubmissionField(), "FWNHHMBRNUM");
		assertEquals(e.getObs().get(1).getValue(), "2");
		
		Map<String, Map<String, Object>> dc = formEntityConverter.getDependentClientsFromFormSubmission(fs);
		assertTrue(dc.isEmpty());
	}
	
	@Test
	public void shouldGetBirthdateNotEstimatedForMainAndApproxForRepeatGroup()
			throws IOException {
		FormSubmission fs = getFormSubmissionFor("new_household_registration", 7);
		
		Client c = formEntityConverter.getClientFromFormSubmission(fs);
		assertEquals(c.getBirthdate(), new DateTime("1900-01-01"));
		assertTrue(c.getBirthdateApprox());
		
		Map<String, Map<String, Object>> dc = formEntityConverter.getDependentClientsFromFormSubmission(fs);
		for (String id : dc.keySet()) {
			Client cl = (Client) dc.get(id).get("client");
			assertEquals(cl.getBirthdate(), new DateTime("2000-05-07"));
			assertFalse(cl.getBirthdateApprox());
		}
	}
	
	@Test
	public void shouldGetBirthdateNotEstimatedForMainAndRepeatGroupIfNotSpecified()
			throws IOException {
		FormSubmission fs = getFormSubmissionFor("new_household_registration", 8);
		
		Client c = formEntityConverter.getClientFromFormSubmission(fs);
		assertEquals(c.getBirthdate(), new DateTime("1900-01-01"));
		assertFalse(c.getBirthdateApprox());
		
		Map<String, Map<String, Object>> dc = formEntityConverter.getDependentClientsFromFormSubmission(fs);
		for (String id : dc.keySet()) {
			Client cl = (Client) dc.get(id).get("client");
			assertEquals(cl.getBirthdate(), new DateTime("2000-05-07"));
			assertFalse(cl.getBirthdateApprox());
		}
	}
	
	@SuppressWarnings ("unchecked")
	@Test
	public void shouldGetDataSpecifiedInGroupInsideSubform() throws IOException, ParseException, JSONException {
		FormSubmission fs = getFormSubmissionFor("new_household_registration_with_grouped_subform_data", 1);
		
		Client c = formEntityConverter.getClientFromFormSubmission(fs);
		assertEquals(c.getBirthdate(), new DateTime("1900-01-01"));
		assertFalse(c.getBirthdateApprox());
		assertThat(c.getAttributes(), Matchers.<String, Object>hasEntry(equalTo("GoB_HHID"), equalTo((Object) "2322")));
		assertThat(c.getAttributes(), Matchers.<String, Object>hasEntry(equalTo("JiVitA_HHID"), equalTo((Object) "9889")));
		
		Event e = formEntityConverter.getEventFromFormSubmission(fs);
		assertEquals(e.getBaseEntityId(), c.getBaseEntityId());
		assertEquals(e.getEventType(), "New Household Registration");
		assertEquals(e.getEventDate(), new DateTime(new SimpleDateFormat("yyyy-M-dd").parse("2015-10-11")));
		assertEquals(e.getLocationId(), "2fc43738-ace5-g961-8e8f-ab7dg0e5bc63");
		
		assertThat(e.getObs(),
				Matchers.<Obs>hasItem(Matchers.<Obs>allOf(
						Matchers.<Obs>hasProperty("fieldCode", equalTo("5611AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")),
						Matchers.<Obs>hasProperty("value", equalTo("23")),
						Matchers.<Obs>hasProperty("formSubmissionField", equalTo("FWNHHMBRNUM")))));
		
		Map<String, Map<String, Object>> dc = formEntityConverter.getDependentClientsFromFormSubmission(fs);
		for (String id : dc.keySet()) {
			Client cl = (Client) dc.get(id).get("client");
			assertEquals(cl.getBirthdate(), new DateTime("1988-10-08"));
			assertFalse(cl.getBirthdateApprox());
			assertEquals(cl.getFirstName(), "jackfruit");
			assertEquals(cl.getAddresses().get(0).getCountry(), "Bangladesh");
			assertEquals(cl.getAddresses().get(0).getAddressType(), "usual_residence");
			assertEquals(cl.getAddresses().get(0).getStateProvince(), "RANGPUR");
			assertThat(cl.getIdentifiers(), Matchers.<String, String>hasEntry(equalTo("NID"), equalTo("7675788777775")));
			assertThat(cl.getIdentifiers(),
					Matchers.<String, String>hasEntry(equalTo("Birth Registration ID"), equalTo("98899998888888888")));
			assertThat(cl.getAttributes(),
					Matchers.<String, Object>hasEntry(equalTo("GoB_HHID"), equalTo((Object) "2322")));
			assertThat(cl.getAttributes(),
					Matchers.<String, Object>hasEntry(equalTo("JiVitA_HHID"), equalTo((Object) "9889")));
			
			Event ev = (Event) dc.get(id).get("event");
			assertEquals(ev.getBaseEntityId(), cl.getBaseEntityId());
			assertEquals(ev.getEventType(), "New Woman Registration");
			assertEquals(ev.getEventDate(), new DateTime(new SimpleDateFormat("yyyy-M-dd").parse("2015-10-11")));
			assertEquals(ev.getLocationId(), "2fc43738-ace5-g961-8e8f-ab7dg0e5bc63");
			
			assertThat(ev.getObs(),
					Matchers.<Obs>hasItem(Matchers.<Obs>allOf(
							Matchers.<Obs>hasProperty("fieldCode", equalTo("161135AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")),
							Matchers.<Obs>hasProperty("value", equalTo("zoom")),
							Matchers.<Obs>hasProperty("formSubmissionField", equalTo("FWHUSNAME")))));
			assertThat(ev.getObs(),
					Matchers.<Obs>hasItem(Matchers.<Obs>allOf(
							Matchers.<Obs>hasProperty("fieldCode", equalTo("163087AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")),
							Matchers.<Obs>hasProperty("values",
									hasItems(equalTo("163084AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"),
											equalTo("163083AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"))),
							Matchers.<Obs>hasProperty("formSubmissionField", equalTo("FWWOMANYID")))));
		}
		
		if (pushToOpenmrsForTest) {
			OpenmrsHouseHold hh = new OpenmrsHouseHold(c, e);
			for (Map<String, Object> cm : dc.values()) {
				hh.addHHMember((Client) cm.get("client"), (Event) cm.get("event"));
			}
			
			householdService.saveHH(hh, true);
		}
	}
	
	@SuppressWarnings ("unchecked")
	@Test
	public void shouldGetDataSpecifiedInMultiselect() throws IOException {
		FormSubmission fs = getFormSubmissionFor("new_household_registration_with_grouped_subform_data", 1);
		
		Client c = formEntityConverter.getClientFromFormSubmission(fs);
		Event e = formEntityConverter.getEventFromFormSubmission(fs);
		
		Map<String, Map<String, Object>> dc = formEntityConverter.getDependentClientsFromFormSubmission(fs);
		for (String id : dc.keySet()) {
			Client cl = (Client) dc.get(id).get("client");
			Event ev = (Event) dc.get(id).get("event");
			
			assertThat(ev.getObs(),
					Matchers.<Obs>hasItem(Matchers.<Obs>allOf(
							Matchers.<Obs>hasProperty("fieldCode", equalTo("163087AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")),
							Matchers.<Obs>hasProperty("values",
									hasItems(equalTo("163084AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"),
											equalTo("163083AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"))),
							Matchers.<Obs>hasProperty("formSubmissionField", equalTo("FWWOMANYID")),
							Matchers.<Obs>hasProperty("fieldType", equalTo("concept")),
							Matchers.<Obs>hasProperty("fieldDataType", startsWith("select all")))));
		}
	}
	
	@Test
	public void parentChildObsTest() throws JsonIOException, IOException, JSONException {
		FormSubmission fs = getFormSubmissionFor("psrf_form");
		
		Client c = formEntityConverter.getClientFromFormSubmission(fs);
		Event e = (Event) formEntityConverter.getEventFromFormSubmission(fs);
		
		if (pushToOpenmrsForTest) {
			encounterService1.createEncounter(e);
		}
		
	}
	
	@SuppressWarnings ("unchecked")
	@Test
	public void shouldHandleTTEnrollmentform() throws IOException {
		FormSubmission fs = getFormSubmissionFor("woman_enrollment");
		
		Client c = formEntityConverter.getClientFromFormSubmission(fs);
		assertEquals(c.getBaseEntityId(), "69995674-bb29-4985-967a-fec8d372a475");
		assertEquals(c.getFirstName(), "barsaat");
		assertEquals(c.getGender(), "female");
		assertEquals(c.getBirthdate(), new DateTime("1979-04-05"));
		assertEquals(c.getAddresses().get(0).getAddressField("landmark"), "nishaani");
		assertEquals(c.getAddresses().get(0).getStateProvince(), "sindh");
		assertEquals(c.getAddresses().get(0).getCityVillage(), "karachi");
		assertEquals(c.getAddresses().get(0).getTown(), "liaquatabad");
		assertEquals(c.getAddresses().get(0).getSubTown(), "sharifabad");
		assertEquals(c.getAddresses().get(0).getAddressField("house"), "6h");
		assertEquals(c.getIdentifiers().get("Program Client ID"), "14608844");
		assertEquals(c.getAttributes().get("EPI Card Number"), "20160003");
		
		Event e = formEntityConverter.getEventFromFormSubmission(fs);
		assertEquals(e.getBaseEntityId(), "69995674-bb29-4985-967a-fec8d372a475");
		assertEquals(e.getEventDate(), new DateTime(new DateTime("2016-04-05")));
		assertEquals(e.getLocationId(), "Homeopathic Center");
		assertEquals(e.getFormSubmissionId(), "de408c93-2ec5-40bc-a957-eaf375583e27");
		assertEquals(e.getEntityType(), "pkwoman");
		assertEquals(e.getEventType(), "Woman TT enrollment");
		assertEquals(e.getProviderId(), "demotest");
		
		assertThat(e.getObs(),
				Matchers.<Obs>hasItem(Matchers.<Obs>allOf(
						Matchers.<Obs>hasProperty("fieldCode", equalTo("154384AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")),
						Matchers.<Obs>hasProperty("values", hasItems(equalTo("37"))),
						Matchers.<Obs>hasProperty("formSubmissionField", equalTo("calc_age_confirm")),
						Matchers.<Obs>hasProperty("fieldType", equalTo("concept")),
						Matchers.<Obs>hasProperty("fieldDataType", startsWith("calculate")),
						Matchers.<Obs>hasProperty("effectiveDatetime", equalTo(e.getEventDate())))));
		
		assertThat(e.getObs(),
				Matchers.<Obs>hasItem(Matchers.<Obs>allOf(
						Matchers.<Obs>hasProperty("fieldCode", equalTo("163137AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")),
						Matchers.<Obs>hasProperty("values", hasItems(equalTo("2016-04-05 16:21:32"))),
						Matchers.<Obs>hasProperty("formSubmissionField", equalTo("start")),
						Matchers.<Obs>hasProperty("fieldType", equalTo("concept")),
						Matchers.<Obs>hasProperty("fieldDataType", startsWith("start")),
						Matchers.<Obs>hasProperty("effectiveDatetime", equalTo(e.getEventDate())))));
		
		assertThat(e.getObs(),
				Matchers.<Obs>hasItem(Matchers.<Obs>allOf(
						Matchers.<Obs>hasProperty("fieldCode", equalTo("163138AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")),
						Matchers.<Obs>hasProperty("values", hasItems(equalTo("2016-04-05 16:23:59"))),
						Matchers.<Obs>hasProperty("formSubmissionField", equalTo("end")),
						Matchers.<Obs>hasProperty("fieldType", equalTo("concept")),
						Matchers.<Obs>hasProperty("fieldDataType", startsWith("end")),
						Matchers.<Obs>hasProperty("effectiveDatetime", equalTo(e.getEventDate())))));
		
	}
	
	@SuppressWarnings ("unchecked")
	@Test
	public void shouldHandleChildVaccinationEnrollmentform() throws IOException {
		FormSubmission fs = getFormSubmissionFor("child_enrollment");
		
		Client c = formEntityConverter.getClientFromFormSubmission(fs);
		assertEquals(c.getBaseEntityId(), "ad653225-6bed-48d3-8e5d-741d3d50d61a");
		assertEquals(c.getFirstName(), "aase");
		assertEquals(c.getLastName(), "zeest");
		assertEquals(c.getGender(), "male");
		assertEquals(c.getBirthdate(), new DateTime("2016-01-03"));
		assertEquals(c.getAddresses().get(0).getAddressField("landmark"), "nishaani");
		assertEquals(c.getAddresses().get(0).getStateProvince(), "sindh");
		assertEquals(c.getAddresses().get(0).getCityVillage(), "karachi");
		assertEquals(c.getAddresses().get(0).getTown(), "liaquatabad");
		assertEquals(c.getAddresses().get(0).getSubTown(), "mujahid_colony");
		assertEquals(c.getAddresses().get(0).getAddressField("house"), "hi65");
		assertEquals(c.getIdentifiers().get("Program Client ID"), "98120722");
		assertEquals(c.getAttributes().get("EPI Card Number"), "20160009");
		
		Event e = formEntityConverter.getEventFromFormSubmission(fs);
		assertEquals(e.getBaseEntityId(), "ad653225-6bed-48d3-8e5d-741d3d50d61a");
		assertEquals(e.getEventDate(), new DateTime(new DateTime("2016-03-05")));
		assertEquals(e.getLocationId(), "Homeopathic Center");
		assertEquals(e.getFormSubmissionId(), "8524f6b8-441a-4769-aa74-03e1dde0901a");
		assertEquals(e.getEntityType(), "pkchild");
		assertEquals(e.getEventType(), "Child Vaccination Enrollment");
		assertEquals(e.getProviderId(), "demotest");
		
		assertThat(e.getObs(),
				Matchers.<Obs>hasItem(Matchers.<Obs>allOf(
						Matchers.<Obs>hasProperty("fieldCode", equalTo("154384AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")),
						Matchers.<Obs>hasProperty("values", hasItems(equalTo("2"))),
						Matchers.<Obs>hasProperty("formSubmissionField", equalTo("calc_age_confirm")),
						Matchers.<Obs>hasProperty("fieldType", equalTo("concept")),
						Matchers.<Obs>hasProperty("fieldDataType", startsWith("calculate")),
						Matchers.<Obs>hasProperty("effectiveDatetime", equalTo(e.getEventDate())))));
		
		assertThat(e.getObs(),
				Matchers.<Obs>hasItem(Matchers.<Obs>allOf(
						Matchers.<Obs>hasProperty("fieldCode", equalTo("163137AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")),
						Matchers.<Obs>hasProperty("values", hasItems(equalTo("2016-03-05 23:01:13"))),
						Matchers.<Obs>hasProperty("formSubmissionField", equalTo("start")),
						Matchers.<Obs>hasProperty("fieldType", equalTo("concept")),
						Matchers.<Obs>hasProperty("fieldDataType", startsWith("start")),
						Matchers.<Obs>hasProperty("effectiveDatetime", equalTo(e.getEventDate())))));
		
		assertThat(e.getObs(),
				Matchers.<Obs>hasItem(Matchers.<Obs>allOf(
						Matchers.<Obs>hasProperty("fieldCode", equalTo("163138AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")),
						Matchers.<Obs>hasProperty("values", hasItems(equalTo("2016-03-05 23:03:51"))),
						Matchers.<Obs>hasProperty("formSubmissionField", equalTo("end")),
						Matchers.<Obs>hasProperty("fieldType", equalTo("concept")),
						Matchers.<Obs>hasProperty("fieldDataType", startsWith("end")),
						Matchers.<Obs>hasProperty("effectiveDatetime", equalTo(e.getEventDate())))));
		
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
