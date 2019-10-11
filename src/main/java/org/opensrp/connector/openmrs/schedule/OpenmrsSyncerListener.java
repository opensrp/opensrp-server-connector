package org.opensrp.connector.openmrs.schedule;

import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONObject;
import org.opensrp.connector.openmrs.constants.OpenmrsConstants.SchedulerConfig;
import org.opensrp.connector.openmrs.service.EncounterService;
import org.opensrp.connector.openmrs.service.PatientService;
import org.opensrp.domain.AppStateToken;
import org.opensrp.domain.Client;
import org.opensrp.domain.Event;
import org.opensrp.service.ClientService;
import org.opensrp.service.ConfigService;
import org.opensrp.service.ErrorTraceService;
import org.opensrp.service.EventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OpenmrsSyncerListener {
	
	private static final ReentrantLock lock = new ReentrantLock();
	
	private static Logger logger = LoggerFactory.getLogger(OpenmrsSyncerListener.class.toString());
	
	private final ConfigService config;
	
	private final ErrorTraceService errorTraceService;
	
	private final PatientService patientService;
	
	private final EncounterService encounterService;
	
	private final EventService eventService;
	
	private final ClientService clientService;
	
	@Autowired
	public OpenmrsSyncerListener(ConfigService config, ErrorTraceService errorTraceService, PatientService patientService,
	    EncounterService encounterService, ClientService clientService, EventService eventService) {
		
		this.config = config;
		this.errorTraceService = errorTraceService;
		this.patientService = patientService;
		this.encounterService = encounterService;
		this.eventService = eventService;
		this.clientService = clientService;
		
		this.config.registerAppStateToken(SchedulerConfig.openmrs_syncer_sync_schedule_tracker_by_last_update_enrollment, 0,
		    "ScheduleTracker token to keep track of enrollment synced with OpenMRS", true);
		
		this.config.registerAppStateToken(SchedulerConfig.openmrs_syncer_sync_client_by_date_updated, 0,
		    "OpenMRS data pusher token to keep track of new / updated clients synced with OpenMRS", true);
		
		this.config.registerAppStateToken(SchedulerConfig.openmrs_syncer_sync_client_by_date_voided, 0,
		    "OpenMRS data pusher token to keep track of voided clients synced with OpenMRS", true);
		
		this.config.registerAppStateToken(SchedulerConfig.openmrs_syncer_sync_event_by_date_updated, 0,
		    "OpenMRS data pusher token to keep track of new / updated events synced with OpenMRS", true);
		
		this.config.registerAppStateToken(SchedulerConfig.openmrs_syncer_sync_event_by_date_voided, 0,
		    "OpenMRS data pusher token to keep track of voided events synced with OpenMRS", true);
	}
	
	public void pushToOpenMRS() {
		
		if (!lock.tryLock()) {
			logger.warn("Not fetching forms from Message Queue. It is already in progress.");
			return;
		}
		try {
			
			logger("RUNNING OPENMRS DATA PUSH Service at " + DateTime.now(), "");
			
			logger.info("RUNNING FOR EVENTS");
			
			AppStateToken lastsync = config
			        .getAppStateTokenByName(SchedulerConfig.openmrs_syncer_sync_client_by_date_updated);
			Long start = lastsync == null || lastsync.getValue() == null ? 0 : lastsync.longValue();
			
			pushClient(start);
			
			logger.info("RUNNING FOR EVENTS");
			
			lastsync = config.getAppStateTokenByName(SchedulerConfig.openmrs_syncer_sync_event_by_date_updated);
			start = lastsync == null || lastsync.getValue() == null ? 0 : lastsync.longValue();
			
			pushEvent(start);
			
			logger("PUSH TO OPENMRS FINISHED AT ", "");
			
		}
		catch (Exception ex) {
			logger.error("", ex);
		}
		finally {
			lock.unlock();
		}
	}
	
	public DateTime logger(String message, String subject) {
		logger.info(message + subject + " at " + DateTime.now());
		return DateTime.now();
	}
	
	public JSONObject pushClient(long start) {
		try {
			List<Client> cl = clientService.findByServerVersion(start);
			logger.info("Clients list size " + cl.size());
			JSONArray patientsJsonArray = new JSONArray();// only for test code purpose
			JSONArray relationshipsArray = new JSONArray();// only for test code purpose
			JSONObject returnJsonObject = new JSONObject();// only for test code purpose
			if (cl != null && !cl.isEmpty()) {
				patientService.processClients(cl, patientsJsonArray,
				    SchedulerConfig.openmrs_syncer_sync_client_by_date_updated, "OPENMRS FAILED CLIENT PUSH");
				logger.info("RUNNING FOR RELATIONSHIPS");
				patientService.createRelationShip(cl, "OPENMRS FAILED CLIENT RELATIONSHIP PUSH");
			}
			returnJsonObject.put("patient", patientsJsonArray); // only for test code purpose
			returnJsonObject.put("relation", relationshipsArray);// only for test code purpose
			return returnJsonObject;
		}
		catch (Exception e) {
			logger.error("", e);
			return null;
		}
		
	}
	
	public JSONObject pushEvent(long start) {
		List<Event> eventList = eventService.findByServerVersion(start);
		logger.info("Event list size " + eventList.size() + " [start]" + start);
		JSONObject encounter = null;
		if (eventList != null && !eventList.isEmpty())
			for (Event event : eventList) {
				try {
					String uuid = event.getIdentifier(EncounterService.OPENMRS_UUID_IDENTIFIER_TYPE);
					if (uuid != null) {
						encounter = encounterService.updateEncounter(event);
						config.updateAppStateToken(SchedulerConfig.openmrs_syncer_sync_event_by_date_updated,
						    event.getServerVersion());
					} else {
						JSONObject eventJson = encounterService.createEncounter(event);
						encounter = eventJson;// only for test code purpose
						if (eventJson != null && eventJson.has("uuid")) {
							event.addIdentifier(EncounterService.OPENMRS_UUID_IDENTIFIER_TYPE, eventJson.getString("uuid"));
							eventService.updateEvent(event);
							config.updateAppStateToken(SchedulerConfig.openmrs_syncer_sync_event_by_date_updated,
							    event.getServerVersion());
						}
					}
				}
				catch (Exception ex2) {
					logger.error("", ex2);
					errorTraceService.log("OPENMRS FAILED EVENT PUSH", Event.class.getName(), event.getId(),
					    ExceptionUtils.getStackTrace(ex2), "");
				}
			}
		return encounter;
		
	}
	
}
