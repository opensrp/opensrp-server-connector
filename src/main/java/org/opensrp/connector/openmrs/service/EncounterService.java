package org.opensrp.connector.openmrs.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.opensrp.api.domain.Location;
import org.opensrp.common.util.HttpResponse;
import org.opensrp.common.util.HttpUtil;
import org.opensrp.connector.openmrs.constants.ConnectorConstants;
import org.opensrp.domain.Client;
import org.opensrp.domain.Event;
import org.opensrp.domain.Obs;
import org.opensrp.domain.User;
import org.opensrp.service.ClientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EncounterService extends OpenmrsService {
	private static Logger logger = LoggerFactory.getLogger(EncounterService.class.toString());
	public static final String OPENMRS_UUID_IDENTIFIER_TYPE = "OPENMRS_UUID";
	private static final String ENCOUNTER_URL = "ws/rest/v1/encounter";//"ws/rest/emrapi/encounter";
	private static final String OBS_URL = "ws/rest/v1/obs";
	private static final String ENCOUNTER__TYPE_URL = "ws/rest/v1/encountertype";
	private PatientService patientService;
	private OpenmrsUserService userService;
	private ClientService clientService;
	private OpenmrsLocationService openmrsLocationService;
	private static final String CONCEPT_REMOVE_REASON_DEATH = "161641AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	@Autowired
	public EncounterService(PatientService patientService, OpenmrsUserService userService, ClientService clientService,
	                        OpenmrsLocationService openmrsLocationService) {
		this.patientService = patientService;
		this.userService = userService;
		this.clientService = clientService;
		this.openmrsLocationService = openmrsLocationService;
	}
	
	public EncounterService(String openmrsUrl, String user, String password) {
		super(openmrsUrl, user, password);
	}
	
	public EncounterService(String openmrsUrl, String user, String password, String openMrsVersion) {
		super(openmrsUrl, user, password, openMrsVersion);
	}
	
	public PatientService getPatientService() {
		return patientService;
	}
	
	public void setPatientService(PatientService patientService) {
		this.patientService = patientService;
	}
	
	public void setUserService(OpenmrsUserService userService) {
		this.userService = userService;
	}
	
	public JSONObject getEncounterByUuid(String uuid, boolean noRepresentationTag) throws JSONException {
		return new JSONObject(HttpUtil.get(getURL() + "/" + ENCOUNTER_URL + "/" + uuid, noRepresentationTag ? "" : "v=full",
				OPENMRS_USER, OPENMRS_PWD).body());
	}
	
	public JSONObject getObsByEncounterUuid(String encounterUuid) throws JSONException {
		// The data format returned contains the obs uuid and concept uuids
		return new JSONObject(HttpUtil.get(getURL() + "/" + ENCOUNTER_URL + "/" + encounterUuid,
				"v=custom:(uuid,obs:(uuid,concept:(uuid)))", OPENMRS_USER, OPENMRS_PWD).body());
	}
	
	public JSONObject getObsUuidByParentObsUuid(String obsUuid) throws JSONException {
		//The data format returned contains the children obs uuid and concept uuids
		return new JSONObject(HttpUtil.get(getURL() + "/" + OBS_URL + "/" + obsUuid,
				"v=custom:(groupMembers:(uuid,concept:(uuid)))", OPENMRS_USER, OPENMRS_PWD).body());
	}
	
	public JSONObject getEncounterType(String encounterType) throws JSONException {
		// we have to use this ugly approach because identifier not found throws exception and
		// its hard to find whether it was network error or object not found or server error
		JSONObject resEncounterType = new JSONObject(
				HttpUtil.get(getURL() + "/" + ENCOUNTER__TYPE_URL, "v=full", OPENMRS_USER, OPENMRS_PWD).body());
		
		if (resEncounterType.has(ConnectorConstants.RESULTS) && resEncounterType.get(ConnectorConstants.RESULTS) instanceof JSONArray) {
			JSONArray res = resEncounterType.getJSONArray(ConnectorConstants.RESULTS);
			for (int i = 0; i < res.length(); i++) {
				if (res.getJSONObject(i).getString(ConnectorConstants.DISPLAY).equalsIgnoreCase(encounterType)) {
					return res.getJSONObject(i);
				}
			}
		}
		return null;
	}
	
	public JSONObject createEncounterType(String name, String description) throws JSONException {
		JSONObject encounterType = convertEncounterToOpenmrsJson(name, description);
		return new JSONObject(
				HttpUtil.post(getURL() + "/" + ENCOUNTER__TYPE_URL, "", encounterType.toString(), OPENMRS_USER, OPENMRS_PWD).body());
	}
	
	public JSONObject convertEncounterToOpenmrsJson(String name, String description) throws JSONException {
		JSONObject encounterJsonObject = new JSONObject();
		encounterJsonObject.put(ConnectorConstants.NAME, name);
		encounterJsonObject.put("description", description);
		return encounterJsonObject;
	}
	
	public JSONObject createEncounter(Event event) throws JSONException {
		String patientUuid = patientService.getPatientByIdentifierUUID(event.getBaseEntityId());
		if (patientUuid == null) {
			return null;
		} else {
			switch (event.getEventType()) {
				case ConnectorConstants.UPDATE_BIRTH_REGISTRATION:
					patientService.updatePersonAddress(event);
					break;
				case "Death":
					patientService.updatePersonAsDeceased(event);
					break;
				case "Move To Catchment":
					patientService.moveToCatchment(event);
					break;
				default:
					break;
			}
			
			JSONObject encounter = getEncounterJsonObject(event, patientUuid);
			
			List<Obs> observationLists = event.getObs();
			Map<String, JSONArray> parent = new HashMap<>();
			Map<String, JSONArray> parentChild = new HashMap<>();
			
			if (observationLists != null)
				for (Obs obs : observationLists) {
					if (!StringUtils.isBlank(obs.getFieldCode())
							&& (obs.getFieldType() == null || obs.getFieldType().equalsIgnoreCase(ConnectorConstants.CONCEPT))) {
						//						skipping empty obs and fields that don't have concepts if no parent simply make it root obs
						if (ConnectorConstants.CONCEPT.equals(obs.getFieldType())) {
							if (ConnectorConstants.BIRTH_FACILITY_NAME.equals(obs.getFormSubmissionField()) && obs.getValue() != null) {
								Location location = openmrsLocationService.getLocation(obs.getValue().toString());
								if (location != null && location.getName() != null) {
									obs.setValue(location.getName());
								}
							}
							
							if (obs.getFieldCode() != null && ConnectorConstants.REMOVE.equals(event.getEventType())
									&& obs.getFieldCode().equalsIgnoreCase(CONCEPT_REMOVE_REASON_DEATH)) {
								patientService.updatePersonAsDeceased(event);
							}
						}
						
						generateObs(parent, parentChild, obs, observationLists);
					}
				}
			JSONArray obsArray = getObsJsonArray(event, parent, parentChild);
			
			encounter.put(ConnectorConstants.OBS, obsArray);
			HttpResponse op = HttpUtil.post(HttpUtil.removeEndingSlash(OPENMRS_BASE_URL) + "/" + ENCOUNTER_URL, "", encounter.toString(), OPENMRS_USER, OPENMRS_PWD);
			return new JSONObject(op.body());
		}
	}
	
	private JSONArray getObsJsonArray(Event event, Map<String, JSONArray> parent, Map<String, JSONArray> parentChild) throws JSONException {
		JSONArray obsArray = new JSONArray();
		
		for (String ok : parent.keySet()) {
			for (int i = 0; i < parent.get(ok).length(); i++) {
				JSONObject obsObject = parent.get(ok).getJSONObject(i);
				JSONArray childObs = parentChild.get(ok);
				if (childObs != null && childObs.length() > 0) {
					//fix for vaccines wrong parent concept remove the if-condition once the right concepts are passed
					if (event.getEventType().equals(ConnectorConstants.VACCINATION) || event.getEventType().equals(ConnectorConstants.HPV_VACCINATION)) {
						JSONObject vaccineParent = new JSONObject();
						vaccineParent.put(ConnectorConstants.CONCEPT, ok);
						childObs.put(vaccineParent);
						obsArray = concatArray(obsArray, childObs);
					} else {
						obsObject.put(ConnectorConstants.GROUP_MEMBERS, childObs);
						obsObject.remove(ConnectorConstants.VALUE);
						obsArray.put(obsObject);
					}
				} else {
					obsArray.put(obsObject);
				}
			}
		}
		return obsArray;
	}
	
	private JSONObject getEncounterJsonObject(Event event, String patientUuid) throws JSONException {
		JSONObject encounter = new JSONObject();
		
		encounter.put(ConnectorConstants.ENCOUNTER_DATETIME, OPENMRS_DATE.format(event.getEventDate().toDate()));
		// patient must be existing in OpenMRS before it submits an encounter. if it doesnot it would throw NPE
		encounter.put(ConnectorConstants.PATIENT, patientUuid);
		encounter.put(ConnectorConstants.ENCOUNTER_TYPE, event.getEventType());
		encounter.put(ConnectorConstants.LOCATION, event.getLocationId());
		
		makeProvider(encounter, event.getProviderId());
		return encounter;
	}
	
	private void makeProvider(JSONObject jsonObject, String providerId) {
		try {
			if (OPENMRS_VERSION.startsWith("1")) {
				jsonObject.put(OPENMRS_PROVIDER, userService.getPersonUUIDByUser(providerId));
			} else {
				JSONArray providerRoleArray = new JSONArray();
				JSONObject providerObj = new JSONObject();
				providerObj.put(OPENMRS_PROVIDER,
						userService.getProvider(null, userService.getUser(providerId).getBaseEntityId()).getString(ConnectorConstants.UUID));
				providerObj.put(ConnectorConstants.ENCOUNTER_ROLE, userService.getEncounterRoleUUID(OPENMRS_PROVIDER));
				providerRoleArray.put(providerObj);
				jsonObject.put(ConnectorConstants.ENCOUNTER_PROVIDERS, providerRoleArray);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	public JSONObject buildUpdateEncounter(Event event) throws JSONException {
		String openmrsUuid = event.getIdentifier(OPENMRS_UUID_IDENTIFIER_TYPE);
		JSONObject encounterObsUuids = getObsByEncounterUuid(openmrsUuid);
		JSONArray obsUuids = encounterObsUuids.getJSONArray(ConnectorConstants.OBS);
		
		System.out.print("[OBS-UUIDS]" + obsUuids);
		
		String patientUuid = patientService.getPatientByIdentifierUUID(event.getBaseEntityId());//TODO find by any identifier
		JSONObject encounter = getEncounterJsonObject(event, patientUuid);
		
		List<Obs> obsList = event.getObs();
		Map<String, JSONArray> parent = new HashMap<>();
		Map<String, JSONArray> parentChild = new HashMap<>();
		
		if (obsList != null)
			for (Obs obs : obsList) {
				if (!StringUtils.isBlank(obs.getFieldCode())
						&& (obs.getFieldType() == null || obs.getFieldType().equalsIgnoreCase(ConnectorConstants.CONCEPT))) {
					//skipping empty obs if no parent simply make it root obs
					if (obs.getFieldType().equals(ConnectorConstants.CONCEPT) && obs.getFormSubmissionField().equals(ConnectorConstants.BIRTH_FACILITY_NAME)
							&& obs.getValue() != null
							&& openmrsLocationService.getLocation(obs.getValue().toString()).getName() != null) {
						obs.setValue(openmrsLocationService.getLocation(obs.getValue().toString()).getName());
					}
					generateObs(parent, parentChild, obs, obsList);
				}
			}
		
		JSONArray obsArray = new JSONArray();
		for (String ok : parent.keySet()) {
			for (int i = 0; i < parent.get(ok).length(); i++) {
				JSONObject obsObject = parent.get(ok).getJSONObject(i);
				obsObject.put(ConnectorConstants.UUID, getObsUuid(obsObject, obsUuids));
				
				JSONArray childObs = parentChild.get(ok);
				if (childObs != null && childObs.length() > 0) {
					// Fetch children obs uuids
					JSONObject obsGroupUuids = getObsUuidByParentObsUuid(obsObject.getString(ConnectorConstants.UUID));
					JSONArray groupUuids = obsGroupUuids.getJSONArray(ConnectorConstants.GROUP_MEMBERS);
					// Add uuids to group members
					for (int j = 0; j < childObs.length(); j++) {
						JSONObject childObsJSONObject = childObs.getJSONObject(j);
						childObsJSONObject.put(ConnectorConstants.UUID, getObsUuid(childObsJSONObject, groupUuids));
					}
					
					obsObject.put(ConnectorConstants.GROUP_MEMBERS, childObs);
					obsObject.remove(ConnectorConstants.VALUE);
				}
				
				obsArray.put(obsObject);
			}
		}
		encounter.put(ConnectorConstants.OBS, obsArray);
		
		return encounter;
	}
	
	private void generateObs(Map<String, JSONArray> parent, Map<String, JSONArray> parentChild, Obs obs, List<Obs> obsList) {
		try {
			if (StringUtils.isBlank(obs.getParentCode())) {
				parent.put(obs.getFieldCode(), convertObsToJson(obs));
			} else {
				//find parent obs if not found search and fill or create one
				JSONArray parentObs = parent.get(obs.getParentCode());
				if (parentObs == null) {
					parent.put(obs.getParentCode(), convertObsToJson(getOrCreateParent(obsList, obs)));
				}
				// find if any other exists with same parent if so add to the list otherwise create new list
				JSONArray obsArray = parentChild.get(obs.getParentCode());
				if (obsArray == null) {
					obsArray = new JSONArray();
				}
				JSONArray addObs = convertObsToJson(obs);
				for (int i = 0; i < addObs.length(); i++) {
					obsArray.put(addObs.getJSONObject(i));
				}
				parentChild.put(obs.getParentCode(), obsArray);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}
	
	public JSONObject updateEncounter(Event event) throws JSONException {
		if (StringUtils.isBlank(event.getIdentifier(OPENMRS_UUID_IDENTIFIER_TYPE))) {
			throw new IllegalArgumentException("Encounter was never pushed to OpenMRS as " + OPENMRS_UUID_IDENTIFIER_TYPE
					+ " is empty. Consider creating a new one");
		}
		
		String openmrsUuid = event.getIdentifier(OPENMRS_UUID_IDENTIFIER_TYPE);
		JSONObject encounter = buildUpdateEncounter(event);
		
		HttpResponse httpResponse = HttpUtil.post(
				HttpUtil.removeEndingSlash(OPENMRS_BASE_URL) + "/" + ENCOUNTER_URL + "/" + openmrsUuid, "", encounter.toString(),
				OPENMRS_USER, OPENMRS_PWD);
		return new JSONObject(httpResponse.body());
	}
	
	private String getObsUuid(JSONObject obs, JSONArray obsUuids) throws JSONException {
		String uuid = "";
		for (int i = 0; i < obsUuids.length(); i++) {
			JSONObject obsUuid = obsUuids.getJSONObject(i);
			JSONObject conceptObj = obsUuid.getJSONObject(ConnectorConstants.CONCEPT);
			
			if (conceptObj.get(ConnectorConstants.UUID).equals(obs.get(ConnectorConstants.CONCEPT))) {
				return obsUuid.getString(ConnectorConstants.UUID);
			}
		}
		
		return uuid;
	}
	
	private JSONArray convertObsToJson(Obs obs) throws JSONException {
		JSONArray obsArray = new JSONArray();
		if (obs.getValues() == null || obs.getValues().size() == 0) {//must be parent of some obs
			JSONObject jsonObject = new JSONObject();
			jsonObject.put(ConnectorConstants.CONCEPT, obs.getFieldCode());
			obsArray.put(jsonObject);
		} else {
			//OpenMRS can not handle multivalued obs so add obs with multiple values as two different obs
			for (Object obsObject : obs.getValues()) {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put(ConnectorConstants.CONCEPT, obs.getFieldCode());
				jsonObject.put(ConnectorConstants.VALUE, obsObject);
				
				obsArray.put(jsonObject);
			}
		}
		return obsArray;
	}
	
	private Obs getOrCreateParent(List<Obs> obsList, Obs observations) {
		for (Obs obs : obsList) {
			if (observations.getParentCode().equalsIgnoreCase(obs.getFieldCode())) {
				return obs;
			}
		}
		return new Obs(ConnectorConstants.CONCEPT, ConnectorConstants.PARENT, observations.getParentCode(), null, null, null, null);
	}
	
	// TODO needs review and refactor
	public Event convertToEvent(JSONObject encounter) throws JSONException {
		if (!encounter.has(ConnectorConstants.PATIENT)) {
			throw new IllegalStateException("No 'patient' object found in given encounter");
		}
		Event event = new Event();
		String patientUuid = encounter.getJSONObject(ConnectorConstants.PATIENT).getString(ConnectorConstants.UUID);
		Client client = clientService.find(patientUuid);
		if (client == null || client.getBaseEntityId() == null) {
			//try to get the client from openmrs based on the uuid
			JSONObject openmrsPatient = patientService.getPatientByUuid(patientUuid, false);
			client = patientService.convertToClient(openmrsPatient);
			if (client == null || client.getBaseEntityId() == null) {
				throw new IllegalStateException(
						"Client was not found registered while converting Encounter to an Event in OpenSRP");
			} else {
				clientService.addClient(client);
			}
		}
		
		JSONObject creator = encounter.getJSONObject(ConnectorConstants.AUDIT_INFO).getJSONObject(ConnectorConstants.CREATOR);
		event.withBaseEntityId(client.getBaseEntityId())
				.withCreator(new User(creator.getString(ConnectorConstants.UUID), creator.getString(ConnectorConstants.DISPLAY), null, null))
				.withDateCreated(DateTime.now());
		
		event.withEventDate(new DateTime(encounter.getString(ConnectorConstants.ENCOUNTER_DATETIME)))
				//.withEntityType(entityType) //TODO
				.withEventType(encounter.getJSONObject(ConnectorConstants.ENCOUNTER_TYPE).getString(ConnectorConstants.NAME))
				//.withFormSubmissionId(formSubmissionId)//TODO
				.withLocationId((encounter.has(ConnectorConstants.LOCATION) && encounter.get(ConnectorConstants.LOCATION) instanceof JSONObject)
						? encounter.getJSONObject(ConnectorConstants.LOCATION).getString(ConnectorConstants.NAME)
						: "")
				//TODO manage providers and uuid in couch
				.withProviderId(creator.getString(ConnectorConstants.DISPLAY)).withVoided(encounter.getBoolean(ConnectorConstants.VOIDED));
		
		event.addIdentifier(OPENMRS_UUID_IDENTIFIER_TYPE, encounter.getString(ConnectorConstants.UUID));
		
		JSONArray observations = encounter.getJSONArray(ConnectorConstants.OBS);
		for (int i = 0; i < observations.length(); i++) {
			JSONObject observation = observations.getJSONObject(i);
			List<Object> values = new ArrayList<Object>();
			
			if (observation.optJSONObject(ConnectorConstants.VALUE) != null) {
				values.add(observation.getJSONObject(ConnectorConstants.VALUE).getString(ConnectorConstants.UUID));
			} else if (observation.has(ConnectorConstants.VALUE)) {
				values.add(observation.getString(ConnectorConstants.VALUE));
			}
			
			event.addObs(new Obs(null, null, observation.getJSONObject(ConnectorConstants.CONCEPT).getString(ConnectorConstants.UUID), null /*//TODO handle parent*/, values,
					null/*comments*/, null/*formSubmissionField*/));
		}
		
		return event;
	}
	
	private JSONArray concatArray(JSONArray... arrs) throws JSONException {
		JSONArray result = new JSONArray();
		for (JSONArray arr : arrs) {
			if (arr != null && arr.length() > 0) {
				for (int i = 0; i < arr.length(); i++) {
					result.put(arr.get(i));
				}
			}
			
		}
		return result;
	}
	
}
