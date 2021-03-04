package org.opensrp.connector.openmrs.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.opensrp.api.domain.Location;
import org.opensrp.api.util.LocationTree;
import org.opensrp.common.util.HttpResponse;
import org.opensrp.common.util.HttpUtil;
import org.opensrp.connector.MultipartUtility;
import org.opensrp.connector.openmrs.constants.ConnectorConstants;
import org.opensrp.connector.openmrs.constants.OpenmrsConstants;
import org.opensrp.connector.openmrs.schedule.OpenmrsSyncerListener;
import org.opensrp.connector.openmrs.service.OpenmrsLocationService.AllowedLevels;
import org.smartregister.domain.Address;
import org.smartregister.domain.Client;
import org.smartregister.domain.Event;
import org.opensrp.domain.Multimedia;
import org.smartregister.domain.Obs;
import org.opensrp.service.ClientService;
import org.opensrp.service.ConfigService;
import org.opensrp.service.ErrorTraceService;
import org.opensrp.service.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
public class PatientService extends OpenmrsService {
	
	// This ID should start with opensrp and end with uid. As matched by atomefeed module`s patient service
	public static final String OPENSRP_IDENTIFIER_TYPE = "OpenSRP Thrive UID";
	
	public static final String OPENSRP_IDENTIFIER_TYPE_MATCHER = "(?i)opensrp.*uid";
	
	public static final String OPENMRS_UUID_IDENTIFIER_TYPE = "OPENMRS_UUID";
	
	public static final String PERSON_ADDRESS_URL_PARAM = "/address";
	
	//TODO include everything for patient registration. i.e. person, person name, patient identifier
	// include get for patient on different params like name, identifier, location, uuid, attribute,etc
	//person methods should be separate
	private static final String PERSON_URL = "ws/rest/v1/person";
	
	private static final String OBS_URL = "ws/rest/v1/obs";
	
	private static final String PATIENT_URL = "ws/rest/v1/patient";
	
	private static final String PATIENT_IMAGE_URL = "ws/rest/v1/patientimage/uploadimage";
	
	private static final String PATIENT_IDENTIFIER_URL = ConnectorConstants.IDENTIFIER;
	
	private static final String PERSON_ATTRIBUTE_TYPE_URL = "ws/rest/v1/personattributetype";
	
	private static final String PATIENT_IDENTIFIER_TYPE_URL = "ws/rest/v1/patientidentifiertype";
	
	private static final String PATIENT_RELATIONSHIP_URL = "ws/rest/v1/relationship";
	
	private static final String CUSTOM_UUID_PARAM = "v=custom:(uuid)";
	
	private static final String CUSTOM_PERSON_PARAM = "v=custom:(person)";
	
	public static final String CUSTOM_PERSON_UUID_PARAM = "v=custom:(person:(uuid))";
	
	public static final String UUID_KEY = "uuid";
	
	public static final String RESULTS_KEY = "results";
	
	public static final String PERSON_KEY = "person";
	
	private static Logger logger = LogManager.getLogger(OpenmrsSyncerListener.class.toString());
	
	private ClientService clientService;
	
	private EventService eventService;
	
	private OpenmrsLocationService openmrsLocationService;
	
	private ConfigService config;
	
	private ErrorTraceService errorTraceService;
	
	public static final String OTHER_NON_CODED_CONCEPT = "5622AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	public static final String OPENSRP_ID_TYPE_KEY = "OpenSRP_ID";
	
	@Value("#{opensrp['registration.events']}")
	protected String REGESTRTIONEVENTS;
	
	public PatientService() {
	}
	
	@Autowired
	public PatientService(ClientService clientService, OpenmrsLocationService openmrsLocationService,
	    EventService eventService, ConfigService config, ErrorTraceService errorTraceService) {
		this.clientService = clientService;
		this.openmrsLocationService = openmrsLocationService;
		this.eventService = eventService;
		this.config = config;
		this.errorTraceService = errorTraceService;
	}
	
	public PatientService(String openmrsUrl, String user, String password) {
		super(openmrsUrl, user, password);
	}
	
	public String getPatientByIdentifierUUID(String identifier) throws JSONException {
		JSONObject j = new JSONObject(HttpUtil.get(getURL() + "/" + PATIENT_URL,
		    CUSTOM_UUID_PARAM + "&identifier=" + identifier, OPENMRS_USER, OPENMRS_PWD).body());
		return getUuidFromJSONObject(j);
	}
	
	public String getPatientByIdentifierPersonUUID(String identifier) throws JSONException {
		JSONObject j = new JSONObject(HttpUtil.get(getURL() + "/" + PATIENT_URL,
		    CUSTOM_PERSON_UUID_PARAM + "&identifier=" + identifier, OPENMRS_USER, OPENMRS_PWD).body());
		if (j.has(RESULTS_KEY) && j.get(RESULTS_KEY) instanceof JSONArray) {
			JSONArray p = j.getJSONArray(RESULTS_KEY);
			if (p.length() > 0) {
				JSONObject resJson = p.getJSONObject(0);
				if (resJson.has(PERSON_KEY)) {
					JSONObject personJoson = resJson.getJSONObject(PERSON_KEY);
					if (personJoson.has(UUID_KEY)) {
						return personJoson.getString(UUID_KEY);
					}
				}
			}
		}
		return null;
	}
	
	public JSONObject getIdentifierType(String identifierType) throws JSONException {
		// we have to use this ugly approach because identifier not found throws exception and
		// its hard to find whether it was network error or object not found or server error
		JSONArray res = new JSONObject(
		        HttpUtil.get(getURL() + "/" + PATIENT_IDENTIFIER_TYPE_URL, "v=full", OPENMRS_USER, OPENMRS_PWD).body())
		                .getJSONArray("results");
		for (int i = 0; i < res.length(); i++) {
			if (res.getJSONObject(i).getString("display").equalsIgnoreCase(identifierType)) {
				return res.getJSONObject(i);
			}
		}
		return null;
	}
	
	public JSONObject getPatientByIdentifierPerson(String identifier) throws JSONException {
		JSONObject j = new JSONObject(HttpUtil.get(getURL() + "/" + PATIENT_URL,
		    CUSTOM_PERSON_PARAM + "&identifier=" + identifier, OPENMRS_USER, OPENMRS_PWD).body());
		if (j.has(RESULTS_KEY) && j.get(RESULTS_KEY) instanceof JSONArray) {
			JSONArray p = j.getJSONArray(RESULTS_KEY);
			if (p.length() > 0) {
				JSONObject resJson = p.getJSONObject(0);
				if (resJson.has(PERSON_KEY)) {
					return resJson.getJSONObject(PERSON_KEY);
				}
			}
		}
		return null;
	}
	
	public JSONObject getPatientByUuid(String uuid, boolean noRepresentationTag) throws JSONException {
		return new JSONObject(HttpUtil.get(getURL() + "/" + PATIENT_URL + "/" + uuid, noRepresentationTag ? "" : "v=full",
		    OPENMRS_USER, OPENMRS_PWD).body());
	}
	
	public String getIdentifierTypeUUID(String identifierType) throws JSONException {
		JSONObject resIdentifier = new JSONObject(
		        HttpUtil.get(getURL() + "/" + PATIENT_IDENTIFIER_TYPE_URL + "/" + identifierType, CUSTOM_UUID_PARAM,
		            OPENMRS_USER, OPENMRS_PWD).body());
		
		if (resIdentifier.has(UUID_KEY)) {
			return resIdentifier.getString(UUID_KEY);
		}
		return null;
	}
	
	public JSONObject createIdentifierType(String name, String description) throws JSONException {
		JSONObject o = convertIdentifierToOpenmrsJson(name, description);
		return new JSONObject(HttpUtil
		        .post(getURL() + "/" + PATIENT_IDENTIFIER_TYPE_URL, "", o.toString(), OPENMRS_USER, OPENMRS_PWD).body());
	}
	
	private String getUuidFromJSONObject(JSONObject object) {
		try {
			if (object.has(RESULTS_KEY) && object.get(RESULTS_KEY) instanceof JSONArray) {
				JSONArray p = object.getJSONArray(RESULTS_KEY);
				if (p.length() > 0) {
					JSONObject resJson = p.getJSONObject(0);
					if (resJson.has(UUID_KEY)) {
						return resJson.getString(UUID_KEY);
					}
				}
			}
			return null;
		}
		catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private JSONObject getRelationshipFromOpenMRS(String personUUID) {
		try {
			return new JSONObject(
			        HttpUtil.get(getURL() + "/" + PATIENT_RELATIONSHIP_URL + "/" + personUUID, "", OPENMRS_USER, OPENMRS_PWD)
			                .body());
		}
		catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public boolean checkIfRelationShipExist(String personB, String personA, String relationshipType) {
		JSONArray res;
		JSONObject relationship;
		String relationShipUUID;
		String relationShipPersonA;
		String relationShipPersonB;
		try {
			res = new JSONObject(HttpUtil
			        .get(getURL() + "/" + PATIENT_RELATIONSHIP_URL, "person=" + personB, OPENMRS_USER, OPENMRS_PWD).body())
			                .getJSONArray("results");
			for (int i = 0; i < res.length(); i++) {
				if (res.getJSONObject(i).getString("uuid") != null) {
					relationship = getRelationshipFromOpenMRS(res.getJSONObject(i).getString("uuid"));
					if (relationship != null && relationship.getString("relationshipType") != null
					        && relationship.getString("personB") != null && relationship.getString("personA") != null) {
						relationShipUUID = res.getJSONObject(i).getString("uuid");
						relationShipPersonB = new JSONObject(relationship.getString("personB")).getString("uuid");
						relationShipPersonA = new JSONObject(relationship.getString("personA")).getString("uuid");
						if (relationShipPersonB.equals(personB) && relationShipPersonA.equals(personA)) {
							return true;
						} else {
							if (relationshipType.equals(PARENT_CHILD_RELATIONSHIP) && !personA.equals(relationShipPersonA))
								voidPatientRelationShip(relationShipUUID);
						}
					}
				}
			}
		}
		catch (JSONException e) {
			logger.error("", e);
		}
		return false;
	}
	
	public JSONObject voidPatientRelationShip(String relationShipUUID) {
		try {
			return new JSONObject(HttpUtil.delete(getURL() + "/" + PATIENT_RELATIONSHIP_URL + "/" + relationShipUUID, "",
			    OPENMRS_USER, OPENMRS_PWD).body());
		}
		catch (JSONException e) {
			logger.error("", e);
			return null;
		}
	}
	
	public JSONObject createPatientRelationShip(String personB, String personA, String relationshipType) {
		if (checkIfRelationShipExist(personB, personA, relationshipType)) {
			return null;
		}
		try {
			JSONObject o = convertRaletionShipToOpenmrsJson(personB, personA, relationshipType);
			return new JSONObject(HttpUtil
			        .post(getURL() + "/" + PATIENT_RELATIONSHIP_URL, "", o.toString(), OPENMRS_USER, OPENMRS_PWD).body());
		}
		catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public JSONObject convertIdentifierToOpenmrsJson(String name, String description) throws JSONException {
		JSONObject a = new JSONObject();
		a.put("name", name);
		a.put("description", description);
		return a;
	}
	
	public JSONObject convertRaletionShipToOpenmrsJson(String personB, String personA, String relationshipType)
	        throws JSONException {
		JSONObject relation = new JSONObject();
		relation.put("personB", personB);
		relation.put("personA", personA);
		relation.put("relationshipType", relationshipType);
		return relation;
	}
	
	public void createRelationShip(List<Client> cl, String errorType) {
		if (cl != null && !cl.isEmpty())
			for (Client c : cl) {
				try {
					if (c.getRelationships() != null && !c.getRelationships().isEmpty()
					        && c.getRelationships().containsKey("mother") && c.getIdentifier("OPENMRS_UUID") != null) {
						String motherBaseId = c.getRelationships().get("mother").get(0);
						String personUUID = getPatientByIdentifierPersonUUID(motherBaseId);
						if (personUUID != null) {
							createPatientRelationShip(c.getIdentifier("OPENMRS_UUID"), personUUID,
							    PARENT_CHILD_RELATIONSHIP);
						}
						List<Client> siblings = clientService.findByRelationship(motherBaseId);
						if (siblings != null && !siblings.isEmpty()) {
							for (Client client : siblings) {
								if (motherBaseId.equals(client.getRelationships().get("mother").get(0))
								        && !c.getBaseEntityId().equals(client.getBaseEntityId())) {
									String siblingUUID = getPatientByIdentifierPersonUUID(client.getBaseEntityId());
									if (siblingUUID != null) {
										createPatientRelationShip(c.getIdentifier("OPENMRS_UUID"), siblingUUID,
										    SIBLING_SIBLING_RELATIONSHIP);
									}
								}
							}
						}
					}
				}
				catch (Exception e) {
					logger.error("", e);
					errorTraceService.log(errorType, Client.class.getName(), c.getBaseEntityId(),
					    ExceptionUtils.getStackTrace(e), "");
				}
			}
	}
	
	public void processClients(List<Client> cl, JSONArray patientsJsonArray,
	        OpenmrsConstants.SchedulerConfig schedulerConfig, String errorType) {
		JSONObject patient = new JSONObject();// only for test code purpose
		logger.info("Reprocessing_clients " + cl.size());
		for (Client c : cl) {
			try {
				// FIXME This is to deal with existing records and should be
				// removed later
				if (c.getIdentifiers().containsKey("M_ZEIR_ID")) {
					if (c.getBirthdate() == null) {
						c.setBirthdate(new DateTime("1970-01-01"));
					}
					c.setGender("Female");
				}
				String uuid = c.getIdentifier(PatientService.OPENMRS_UUID_IDENTIFIER_TYPE);
				
				if (uuid == null) {
					uuid = getPatientByIdentifierUUID(c.getBaseEntityId());
					if (uuid == null) {
						for (Entry<String, String> id : c.getIdentifiers().entrySet()) {
							uuid = getPatientByIdentifierUUID(id.getValue());
							if (uuid != null) {
								break;
							}
						}
					}
				}
				if (uuid != null) {
					logger.info("Updating patient " + uuid);
					patient = updatePatient(c, uuid);
					if (c.getIdentifier(PatientService.OPENMRS_UUID_IDENTIFIER_TYPE) != null) {
						c.removeIdentifier(PatientService.OPENMRS_UUID_IDENTIFIER_TYPE);
					}
					c.addIdentifier(PatientService.OPENMRS_UUID_IDENTIFIER_TYPE, uuid);
					clientService.addorUpdate(c, false);
					config.updateAppStateToken(schedulerConfig, c.getServerVersion());
					
				} else {
					JSONObject patientJson = createPatient(c);
					patient = patientJson;//only for test code purpose
					if (patientJson != null && !StringUtils.isEmpty(patientJson.optString("uuid"))) {
						c.addIdentifier(PatientService.OPENMRS_UUID_IDENTIFIER_TYPE, patientJson.getString("uuid"));
						clientService.addorUpdate(c, false);
					}
					config.updateAppStateToken(schedulerConfig, c.getServerVersion());
				}
			}
			catch (Exception ex1) {
				logger.error("", ex1);
				errorTraceService.log(errorType, Client.class.getName(), c.getBaseEntityId(),
				    ExceptionUtils.getStackTrace(ex1), "");
			}
			patientsJsonArray.put(patient);
		}
	}
	
	public String getPersonAttributeTypeUUID(String attributeName) throws JSONException {
		JSONObject resAttributeType = new JSONObject(HttpUtil.get(getURL() + "/" + PERSON_ATTRIBUTE_TYPE_URL,
		    CUSTOM_UUID_PARAM + "&q=" + attributeName, OPENMRS_USER, OPENMRS_PWD).body());
		return getUuidFromJSONObject(resAttributeType);
	}
	
	public JSONObject createPerson(Client be) throws JSONException {
		JSONObject per = convertBaseEntityToOpenmrsJson(be, false);
		logger.info("PERSON TO CREATE " + per.toString());
		String response = HttpUtil.post(getURL() + "/" + PERSON_URL, "", per.toString(), OPENMRS_USER, OPENMRS_PWD).body();
		JSONObject jsonResponse = new JSONObject(response);
		
		if (jsonResponse.has("error")) {
			JSONObject responseError = new JSONObject(jsonResponse.getString("error"));
			if (responseError.has("message") && responseError.getString("message").equals("User is not logged in")) {
				be.setServerVersion(0L);
				clientService.updateClient(be);
			}
		}
		return new JSONObject(response);
	}
	
	public JSONObject convertBaseEntityToOpenmrsJson(Client be, boolean update) throws JSONException {
		JSONObject per = new JSONObject();
		per.put("gender", be.getGender());
		per.put("birthdate", OPENMRS_DATE.format(be.getBirthdate().toDate()));
		per.put("birthdateEstimated", be.getBirthdateApprox());
		if (be.getDeathdate() != null) {
			per.put("deathDate", OPENMRS_DATE.format(be.getDeathdate().toDate()));
		}
		
		String fn = be.getFirstName() == null || StringUtils.isBlank(be.getFirstName()) ? "-"
		        : be.getFirstName();
		if (!fn.equals("-")) {
			fn = fn.replaceAll("[^A-Za-z0-9\\s]+", "");
		}
		fn = convertToOpenmrsString(fn);
		
		String mn = be.getMiddleName() == null ? "" : be.getMiddleName();
		
		if (!mn.equals("-")) {
			mn = mn.replaceAll("[^A-Za-z0-9\\s]+", "");
		}
		mn = convertToOpenmrsString(mn);
		
		String ln = (be.getLastName() == null || be.getLastName().equals(".")) ? "-" : be.getLastName();
		if (!ln.equals("-")) {
			ln = ln.replaceAll("[^A-Za-z0-9\\s]+", "");
		}
		ln = convertToOpenmrsString(ln);
		String address4UUID = null;
		
		List<Event> registrationEvents = eventService.findByBaseEntityId(be.getBaseEntityId());
		for (Event event : registrationEvents) {
			if (event.getEventType().equals("Birth Registration") || event.getEventType().equals("New Woman Registration")) {
				List<Obs> obs = event.getObs();
				for (Obs obs2 : obs) {
					if (obs2 != null && obs2.getFieldType().equals("formsubmissionField")
					        && obs2.getFormSubmissionField().equals("Home_Facility") && obs2.getValue() != null) {
						address4UUID = obs2.getValue().toString();
						String clientAddress4 = openmrsLocationService.getLocation(address4UUID).getName();
						if (be.getAttribute("Home_Facility") != null) {
							be.removeAttribute("Home_Facility");
						}
						be.addAttribute("Home_Facility", clientAddress4);
					}
				}
				break;
			}
		}
		
		if (!update) {
			per.put("names", new JSONArray(
			        "[{\"givenName\":\"" + fn + "\",\"middleName\":\"" + mn + "\", \"familyName\":\"" + ln + "\"}]"));
			per.put("addresses", convertAddressesToOpenmrsJson(be, address4UUID));
		}
		
		return per;
	}
	
	public JSONArray convertAttributesToOpenmrsJson(Map<String, Object> attributes) throws JSONException {
		if (CollectionUtils.isEmpty(attributes)) {
			return null;
		}
		JSONArray attrs = new JSONArray();
		for (Entry<String, Object> at : attributes.entrySet()) {
			String uuid = getPersonAttributeTypeUUID(at.getKey());
			if (uuid != null) {
				JSONObject a = new JSONObject();
				a.put("attributeType", uuid);
				a.put("value", convertToOpenmrsString(at.getValue()));
				attrs.put(a);
			}
		}
		
		return attrs;
	}
	
	public JSONArray convertAddressesToOpenmrsJson(Client client, String clientAddress4UUID) throws JSONException {
		List<Address> adl = client.getAddresses();
		if (adl.isEmpty() && !client.getRelationships().isEmpty()) {
			adl = clientService.getByBaseEntityId(client.getRelationships().get("mother").get(0)).getAddresses();
		}
		
		String address5 = null;
		
		JSONArray jaar = new JSONArray();
		JSONObject jao = new JSONObject();
		for (Address ad : adl) {
			if (ad.getAddressFields() != null) {
				jao.put("address1", convertToOpenmrsString(
				    ad.getAddressFieldMatchingRegex("(?i)(ADDRESS1|HOUSE_NUMBER|HOUSE|HOUSE_NO|UNIT|UNIT_NUMBER|UNIT_NO)")));
				jao.put("address2", convertToOpenmrsString(
				    ad.getAddressFieldMatchingRegex("(?i)(ADDRESS2|STREET|STREET_NUMBER|STREET_NO|LANE)")));
				String address3 = ad.getAddressFieldMatchingRegex("(?i)(ADDRESS3|SECTOR|AREA)");
				address3 = fetchLocationByUUID(address3);
				jao.put("address3", convertToOpenmrsString(address3));
				
				address5 = convertToOpenmrsString(ad.getAddressFieldMatchingRegex("(?i)(ADDRESS5|OTHER_RESIDENTIAL_AREA)"));
				if (org.apache.commons.lang3.StringUtils.isNotBlank(address5)) {
					if (isUUID(address5)) {
						jao.put("address5", openmrsLocationService.getLocation(address5).getName());
					} else {
						jao.put("address5", convertToOpenmrsString(address5));
					}
				}
				
				jao.put("cityVillage", convertToOpenmrsString(ad.getCityVillage()));
				jao.put("address6", convertToOpenmrsString(ad.getAddressType()));
				jao.put("postalCode", convertToOpenmrsString(ad.getPostalCode()));
				jao.put("latitude", ad.getLatitude());
				jao.put("longitude", ad.getLongitude());
				if (ad.getStartDate() != null) {
					jao.put("startDate", OPENMRS_DATE.format(ad.getStartDate().toDate()));
				}
				if (ad.getEndDate() != null) {
					jao.put("endDate", OPENMRS_DATE.format(ad.getEndDate().toDate()));
				}
			}
		}
		
		if (org.apache.commons.lang3.StringUtils.isNotBlank(address5) && isUUID(address5)) {
			LocationTree locationTree = openmrsLocationService.getLocationTreeWithUpperHierachyOf(address5);
			
			try {
				
				Map<String, String> locationsHierarchyMap = openmrsLocationService.getLocationsHierarchy(locationTree);
				
				jao.put("country",
				    locationsHierarchyMap.containsKey(AllowedLevels.COUNTRY.toString())
				            ? locationsHierarchyMap.get(AllowedLevels.COUNTRY.toString())
				            : "");
				
				jao.put("countyDistrict",
				    locationsHierarchyMap.containsKey(AllowedLevels.DISTRICT.toString())
				            ? locationsHierarchyMap.get(AllowedLevels.DISTRICT.toString())
				            : "");
				
				jao.put("address2",
				    locationsHierarchyMap.containsKey(AllowedLevels.COUNTY.toString())
				            ? locationsHierarchyMap.get(AllowedLevels.COUNTY.toString())
				            : "");
				jao.put("address3",
				    locationsHierarchyMap.containsKey(AllowedLevels.SUB_COUNTY.toString())
				            ? locationsHierarchyMap.get(AllowedLevels.SUB_COUNTY.toString())
				            : "");
				
				jao.put("address4",
				    locationsHierarchyMap.containsKey(AllowedLevels.DISTRICT.toString())
				            ? locationsHierarchyMap.get(AllowedLevels.HEALTH_FACILITY.toString())
				            : "");
				
			}
			catch (Exception e) {
				logger.error("", e);
			}
		} else if (clientAddress4UUID != null) {
			String clientAddress4 = fetchLocationByUUID(clientAddress4UUID);
			jao.put("address4", convertToOpenmrsString(clientAddress4));
			
			try {
				LocationTree locationTree = openmrsLocationService.getLocationTreeWithUpperHierachyOf(clientAddress4UUID);
				Map<String, String> locationsHierarchyMap = openmrsLocationService.getLocationsHierarchy(locationTree);
				
				jao.put("countyDistrict",
				    locationsHierarchyMap.containsKey(AllowedLevels.DISTRICT.toString())
				            ? locationsHierarchyMap.get(AllowedLevels.DISTRICT.toString())
				            : "");
				jao.put("stateProvince",
				    locationsHierarchyMap.containsKey(AllowedLevels.PROVINCE.toString())
				            ? locationsHierarchyMap.get(AllowedLevels.PROVINCE.toString())
				            : "");
				jao.put("country",
				    locationsHierarchyMap.containsKey(AllowedLevels.COUNTRY.toString())
				            ? locationsHierarchyMap.get(AllowedLevels.COUNTRY.toString())
				            : "");
				
			}
			catch (Exception e) {
				logger.error("", e);
			}
		}
		
		jaar.put(jao);
		
		return jaar;
	}
	
	public JSONObject createPatient(Client c) throws JSONException {
		String personUUIDString = createPerson(c).optString("uuid");
		if (!StringUtils.isEmpty(personUUIDString)) {
			JSONObject p = new JSONObject();
			p.put("person", personUUIDString);
			
			JSONArray ids = new JSONArray();
			if (c.getIdentifiers() != null) {
				for (Entry<String, String> id : c.getIdentifiers().entrySet()) {
					if (StringUtils.isEmpty(id.getValue())) {
						continue;
					}
					JSONObject jio = new JSONObject();
					jio.put("identifierType", fetchIdentifierTypeUUID(id.getKey()));
					if (id.getKey().equalsIgnoreCase(OPENSRP_ID_TYPE_KEY)) {
						jio.put("identifier", cleanIdentifierWithCheckDigit(id.getValue()));
					} else {
						jio.put("identifier", id.getValue());
					}
					Object cloc = c.getAttribute("Location");
					jio.put("location", cloc == null ? "Unknown Location" : cloc);
					ids.put(jio);
				}
			}
			
			JSONObject jio = new JSONObject();
			jio.put("identifierType", fetchIdentifierTypeUUID(OPENSRP_IDENTIFIER_TYPE));
			jio.put("identifier", c.getBaseEntityId());
			Object cloc = c.getAttribute("Location");
			jio.put("location", cloc == null ? "Unknown Location" : cloc);
			jio.put("preferred", true);
			
			ids.put(jio);
			
			p.put("identifiers", ids);
			String response = HttpUtil.post(getURL() + "/" + PATIENT_URL, "", p.toString(), OPENMRS_USER, OPENMRS_PWD)
			        .body();
			return new JSONObject(response);
			
		} else {
			return null;
		}
	}
	
	public JSONObject updatePatientIdentifier(String patientUUID, String identifierUUID, String newIdentifier)
	        throws JSONException {
		String url = PATIENT_URL + "/" + patientUUID + "/identifier/" + identifierUUID;
		JSONObject p = new JSONObject();
		p.put(ConnectorConstants.IDENTIFIER, newIdentifier);
		
		return new JSONObject(HttpUtil.post(getURL() + "/" + url, "", p.toString(), OPENMRS_USER, OPENMRS_PWD).body());
		
	}
	
	public JSONObject updatePatient(Client c, String uuid) throws JSONException {
		JSONObject p = new JSONObject();
		
		p.put("person", convertBaseEntityToOpenmrsJson(c, true));
		JSONArray ids = new JSONArray();
		if (c.getIdentifiers() != null) {
			updateIdentifiers(uuid, c);
		}
		addPersonAttributes(c, p, ids);
		updatePersonName(getPatientByUuid(uuid, false).getJSONObject(PERSON_KEY), c);
		return new JSONObject(HttpUtil
		        .post(getURL() + "/" + PATIENT_URL + "/" + uuid, "", p.toString(), OPENMRS_USER, OPENMRS_PWD).body());
		
	}
	
	private void addPersonAttributes(Client c, JSONObject p, JSONArray ids) throws JSONException {
		JSONObject jio = new JSONObject();
		jio.put("identifierType", fetchIdentifierTypeUUID(OPENSRP_IDENTIFIER_TYPE));
		jio.put(ConnectorConstants.IDENTIFIER, c.getBaseEntityId());
		Object cloc = c.getAttribute("Location");
		jio.put("location", cloc == null ? "Unknown Location" : cloc);
		jio.put("preferred", true);
		ids.put(jio);
		
		p.put("identifiers", ids);
	}
	
	public JSONObject addThriveId(String baseEntityId, JSONObject patient) throws JSONException {
		JSONObject jio = new JSONObject();
		jio.put("identifierType", fetchIdentifierTypeUUID(OPENSRP_IDENTIFIER_TYPE));
		jio.put(ConnectorConstants.IDENTIFIER, baseEntityId);
		jio.put("location", "Unknown Location");
		jio.put("preferred", true);
		return new JSONObject(
		        HttpUtil.post(getURL() + "/" + PATIENT_URL + "/" + patient.getString("uuid") + "/" + PATIENT_IDENTIFIER_URL,
		            "", jio.toString(), OPENMRS_USER, OPENMRS_PWD).body());
	}
	
	public Client convertToClient(JSONObject patient) throws JSONException {
		Client c = new Client(null);
		JSONArray ar = patient.getJSONArray("identifiers");
		for (int i = 0; i < ar.length(); i++) {
			JSONObject ji = ar.getJSONObject(i);
			if (ji.getJSONObject("identifierType").getString("display").equalsIgnoreCase(OPENSRP_IDENTIFIER_TYPE)) {
				c.setBaseEntityId(ji.getString(ConnectorConstants.IDENTIFIER));
			} else {
				c.addIdentifier(ji.getJSONObject("identifierType").getString("display"),
				    ji.getString(ConnectorConstants.IDENTIFIER));
			}
		}
		
		c.addIdentifier(OPENMRS_UUID_IDENTIFIER_TYPE, patient.getString("uuid"));
		
		JSONObject pr = patient.getJSONObject("person");
		
		String mn = pr.getJSONObject("preferredName").has("middleName")
		        ? pr.getJSONObject("preferredName").getString("middleName")
		        : null;
		DateTime dd = pr.has("deathDate") && !pr.getString("deathDate").equalsIgnoreCase("null")
		        ? new DateTime(pr.getString("deathDate"))
		        : null;
		c.withFirstName(pr.getJSONObject("preferredName").getString("givenName")).withMiddleName(mn)
		        .withLastName(pr.getJSONObject("preferredName").getString("familyName")).withGender(pr.getString("gender"))
		        .withBirthdate(new DateTime(pr.getString("birthdate")), pr.getBoolean("birthdateEstimated"))
		        .withDeathdate(dd, false);
		
		if (pr.has("attributes")) {
			for (int i = 0; i < pr.getJSONArray("attributes").length(); i++) {
				JSONObject at = pr.getJSONArray("attributes").getJSONObject(i);
				if (at.optJSONObject("value") == null) {
					c.addAttribute(at.getJSONObject("attributeType").getString("display"), at.getString("value"));
				} else {
					c.addAttribute(at.getJSONObject("attributeType").getString("display"),
					    at.getJSONObject("value").getString("display"));
				}
			}
		}
		
		if (pr.has("addresses")) {
			for (int i = 0; i < pr.getJSONArray("addresses").length(); i++) {
				JSONObject ad = pr.getJSONArray("addresses").getJSONObject(i);
				DateTime startDate = ad.has("startDate") && !ad.getString("startDate").equalsIgnoreCase("null")
				        ? new DateTime(ad.getString("startDate"))
				        : null;
				DateTime endDate = ad.has("startDate") && !ad.getString("endDate").equalsIgnoreCase("null")
				        ? new DateTime(ad.getString("endDate"))
				        : null;
				Address a = new Address(ad.getString("address6"), startDate, endDate, null, ad.getString("latitude"),
				        ad.getString("longitude"), ad.getString("postalCode"), ad.getString("stateProvince"),
				        ad.getString("country"));
				//a.setGeopoint(geopoint);
				a.setSubTown(ad.getString("address2"));
				a.setTown(ad.getString("address3"));
				a.setSubDistrict(ad.getString("address4"));
				a.setCountyDistrict(ad.getString("countyDistrict"));
				a.setCityVillage(ad.getString("cityVillage"));
				
				c.addAddress(a);
			}
			
		}
		return c;
	}
	
	public List<String> patientImageUpload(Multimedia multimedia) {
		List<String> response = new ArrayList<>();
		try {
			File convFile = new File("/opt" + multimedia.getFilePath());
			MultipartUtility multipart = new MultipartUtility(getURL() + "/" + PATIENT_IMAGE_URL, OPENMRS_USER, OPENMRS_PWD);
			multipart.addFormField("patientidentifier", multimedia.getCaseId());
			multipart.addFormField("category", multimedia.getFileCategory());
			multipart.addFilePart("file", convFile);
			response = multipart.finish();
			System.out.println("SERVER REPLIED:");
			
			for (String line : response) {
				System.out.println(line);
			}
		}
		catch (IOException ex) {
			logger.error("", ex);
		}
		return response;
	}
	
	public JSONObject updatePersonAsDeceased(Event deathEvent) throws JSONException {
		List<Obs> ol = deathEvent.getObs();
		JSONObject obsBody = new JSONObject();
		JSONObject requestBody = new JSONObject();
		String patientUUID = getPatientByIdentifierUUID(deathEvent.getBaseEntityId());
		
		if (ol == null || ol.isEmpty()) {
			throw new IllegalArgumentException(
			        "Death Encounter does not have any observations for the required causeOfDeath ");
		}
		for (Obs obs : ol) {
			if (obs.getFormSubmissionField().equals("Cause_Death") && obs.getValue() != null) {
				obsBody.put("person", patientUUID);
				obsBody.put("concept", PROBABLE_CAUSE_OF_DEATH_CONCEPT);
				obsBody.put("obsDatetime", OPENMRS_DATE.format(deathEvent.getEventDate().toDate()));
				obsBody.put("value", obs.getValue().toString());
				break;
			}
		}
		
		requestBody.put("dead", true);
		requestBody.put("deathDate", OPENMRS_DATE.format(deathEvent.getEventDate().toDate()));
		requestBody.put("causeOfDeath", OTHER_NON_CODED_CONCEPT);
		
		HttpResponse op = HttpUtil.post(HttpUtil.removeEndingSlash(OPENMRS_BASE_URL) + "/" + PERSON_URL + "/" + patientUUID,
		    "", requestBody.toString(), OPENMRS_USER, OPENMRS_PWD);
		
		if (new JSONObject(op.body()).has("uuid")) {
			HttpResponse obsResponse = HttpUtil.post(HttpUtil.removeEndingSlash(OPENMRS_BASE_URL) + "/" + OBS_URL, "",
			    obsBody.toString(), OPENMRS_USER, OPENMRS_PWD);
			return new JSONObject(obsResponse.body());
		}
		return null;
		
	}
	
	public JSONObject updatePersonName(JSONObject patientObject, Client client) throws JSONException {
		
		JSONObject nameObject = patientObject.getJSONObject("preferredName");
		
		String fn = client.getFirstName() == null || client.getFirstName().isEmpty() ? "-" : client.getFirstName();
		if (!fn.equals("-")) {
			fn = fn.replaceAll("[^A-Za-z0-9\\s]+", "");
		}
		
		String ln = (client.getLastName() == null || client.getLastName().equals(".")) ? "-" : client.getLastName();
		if (!ln.equals("-")) {
			ln = ln.replaceAll("[^A-Za-z0-9\\s]+", "");
		}
		
		if (fn.equals(nameObject.getString("givenName")) && ln.equals(nameObject.getString("givenName"))) {
			return null;
		}
		
		JSONObject requestBody = new JSONObject();
		requestBody.put("givenName", fn);
		requestBody.put("familyName", ln);
		
		String url = PERSON_URL + "/" + patientObject.getString("uuid") + "/name/" + nameObject.getString("uuid");
		
		return new JSONObject(
		        HttpUtil.post(getURL() + "/" + url, "", requestBody.toString(), OPENMRS_USER, OPENMRS_PWD).body());
		
	}
	
	public JSONObject updatePersonAddress(Event addressUpdateEvent) {
		String clientAddress4 = null;
		List<Obs> obs = addressUpdateEvent.getObs();
		for (Obs obs2 : obs) {
			if (obs2 != null && obs2.getFieldType().equals("formsubmissionField")
			        && obs2.getFormSubmissionField().equals("Home_Facility") && obs2.getValue() != null) {
				clientAddress4 = fetchLocationByUUID(obs2.getValue().toString());
				break;
			}
		}
		if (clientAddress4 == null) {
			return null;
		}
		return postNewAddress(addressUpdateEvent, clientAddress4);
		
	}
	
	public JSONObject moveToCatchment(Event event) {
		String clientAddress4 = null;
		List<Obs> obs = event.getObs();
		for (Obs obs2 : obs) {
			if (obs2.getFieldCode() != null && obs2.getFieldCode().equals("To_LocationId") && obs2.getValue() != null) {
				{
					clientAddress4 = fetchLocationByUUID(obs2.getValue().toString());
					break;
				}
			}
		}
		if (clientAddress4 == null) {
			return null;
		}
		return postNewAddress(event, clientAddress4);
	}
	
	public JSONObject postNewAddress(Event event, String clientAddress4) {
		try {
			
			Client client = clientService.getByBaseEntityId(event.getBaseEntityId());
			JSONObject patientObject = getPatientByIdentifierPerson(client.getBaseEntityId());
			JSONObject addressObject = patientObject.getJSONObject("preferredAddress");
			JSONObject updateAddress = convertAddressesToOpenmrsJson(client, clientAddress4).getJSONObject(0);
			String url = PERSON_URL + "/" + patientObject.getString("uuid") + "/address/" + addressObject.getString("uuid");
			return new JSONObject(
			        HttpUtil.post(getURL() + "/" + url, "", updateAddress.toString(), OPENMRS_USER, OPENMRS_PWD).body());
		}
		catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public void updateIdentifiers(String personUUID, Client c) throws JSONException {
		JSONObject p = getPatientByUuid(personUUID, false);
		if (p.has("identifiers") && p.get("identifiers") instanceof JSONArray) {
			JSONArray identifiers = p.getJSONArray("identifiers");
			Map<String, String> idsMap = c.getIdentifiers();
			
			for (int j = 0; j < identifiers.length(); j++) {
				JSONObject idObject = identifiers.getJSONObject(j);
				String identifierType = idObject.has("display") ? idObject.getString("display") : null;
				String identifierUuid = idObject.has("uuid") ? idObject.getString("uuid") : null;
				
				if (identifierType == null || identifierUuid == null) {
					continue;
				}
				if (identifierType.contains("=")) {
					String[] fn = identifierType.split("\\=");
					identifierType = fn[0].trim();
					String identifierValue = fn[1].trim();
					if (idsMap.containsKey(identifierType) && !identifierValue.equals(idsMap.get(identifierType))) {
						updatePatientIdentifier(personUUID, identifierUuid, idsMap.get(identifierType));
					}
				}
			}
		}
	}
	
	private String convertToOpenmrsString(String sParam) {
		String s = sParam;
		if (org.apache.commons.lang3.StringUtils.isEmpty(s)) {
			return s;
		}
		
		s = s.replaceAll("\t", "");
		s = org.apache.commons.lang3.StringUtils.stripAccents(s);
		return s;
		
	}
	
	private Object convertToOpenmrsString(Object o) {
		if (o != null && o instanceof String) {
			return convertToOpenmrsString(o.toString());
		}
		return o;
		
	}
	
	public String fetchLocationByUUID(String locationUUID) {
		try {
			if (locationUUID == null || StringUtils.isBlank(locationUUID)
			        || locationUUID.equalsIgnoreCase("Other")) {
				return locationUUID;
			}
			Location location = openmrsLocationService.getLocation(locationUUID);
			if (location == null) {
				return "Unknown Location Id: " + locationUUID;
			} else {
				return location.getName();
			}
		}
		catch (Exception e) {
			logger.error("", e);
			return "Unknown Location Id: " + locationUUID;
		}
	}
	
	private String fetchIdentifierTypeUUID(String identifierType) throws JSONException {
		String uuid = getIdentifierTypeUUID(identifierType);
		if (uuid == null) {
			JSONObject json = createIdentifierType(identifierType, identifierType + " - FOR THRIVE OPENSRP");
			uuid = json.getString("uuid");
		}
		return uuid;
	}
	
	private String cleanIdentifierWithCheckDigit(String rawId) {
		
		if (StringUtils.isEmpty(rawId)) {
			return rawId;
		}
		
		boolean containsHyphen = rawId.contains("-");
		
		if (!containsHyphen) {
			return formatId(rawId);
		} else {
			return rawId;
		}
	}
	
	private String formatId(String openmrsId) {
		int lastIndex = openmrsId.length() - 1;
		String tail = openmrsId.substring(lastIndex);
		return openmrsId.substring(0, lastIndex) + "-" + tail;
	}
	
	public boolean isUUID(String string) {
		try {
			UUID.fromString(string);
			return true;
		}
		catch (IllegalArgumentException ex) {
			return false;
		}
	}
}
