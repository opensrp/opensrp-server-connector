package org.opensrp.connector.dhis2;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.opensrp.common.AllConstants.DHIS2Constants;
import org.opensrp.common.util.DateUtil;
import org.opensrp.connector.domain.DHIS2Marker;
import org.opensrp.connector.repository.couch.AllDHIS2Marker;
import org.opensrp.service.ClientService;
import org.opensrp.service.EventService;
import org.smartregister.domain.Client;
import org.smartregister.domain.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

@Profile("dhis2-sync-vaccine-tracker")
@Component
public class DHIS2SyncerListener {

    private final ClientService clientService;
    @Autowired
    public VaccinationTracker vaccinationTracker;
    @Autowired
    private AllDHIS2Marker allDHIS2Marker;
    @Autowired
    private Dhis2TrackCaptureConnector dhis2TrackCaptureConnector;
    @Autowired
    private DHIS2TrackerService dhis2TrackerService;
    private DHIS2Tracker dhis2Tracker;
    @Autowired
    private EventService eventService;

    @Autowired
    public DHIS2SyncerListener(ClientService clientService) {
        this.clientService = clientService;
    }

    public JSONObject pushToDHIS2() {
        JSONObject response = null;
        try {
            Long start = 0l;
            Long eventStart = 0l;
            List<DHIS2Marker> clientSync = allDHIS2Marker.findByName(DHIS2Constants.DHIS2_TRACK_DATA_SYNCER_VERSION_MARKER);
            List<DHIS2Marker> eventSync = allDHIS2Marker
                    .findByName(DHIS2Constants.DHIS2_TRACK_DATA_SYNCER_VERSION_MARKER_EVENT);
            if (clientSync.size() == 0) {
                allDHIS2Marker.add();
                start = 0l;
            } else {
                start = clientSync == null || clientSync.get(0).getValue() == null ? 0 : clientSync.get(0).getValue();
            }
            System.err.println("start:" + start);
            List<Client> cl = clientService.findByServerVersion(start, null);
            if (eventSync.size() == 0) {
                allDHIS2Marker.addEventMarker();
                eventStart = 0l;
            } else {
                eventStart = eventSync == null || eventSync.get(0).getValue() == null ? 0 : eventSync.get(0).getValue();
            }
            List<Event> events = eventService.findByServerVersion(eventStart);
            for (Client c : cl) {
                System.err.println("Name:" + c.fullName());
                try {
                    response = processTrackerAndSendToDHIS2(c);
                    allDHIS2Marker.update(c.getServerVersion());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            processAndSendVaccineTrackerToDHIS2(events);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return response;
    }

    private JSONObject processAndSendVaccineTrackerToDHIS2(List<Event> events) throws JSONException {
        vaccinationTracker.getTrackCaptureDataAndSend(events);
        return null;

    }

    private JSONObject processTrackerAndSendToDHIS2(Client client) throws JSONException {

        dhis2Tracker = dhis2TrackerService.getTrackerType(client);
        JSONArray clientData = dhis2Tracker.getTrackCaptureData(client);
        return dhis2Tracker.sendTrackCaptureData(clientData);

    }

    public JSONObject sentTrackCaptureDataToDHIS2(Client client) throws JSONException {

        JSONObject clientData = new JSONObject();
        JSONArray clientAttribute = new JSONArray();

        JSONObject fullName = new JSONObject();
        fullName.put("attribute", "pzuh7zrs9Xx");
        fullName.put("value", client.fullName());
        clientAttribute.put(fullName);

        JSONObject gender = new JSONObject();
        gender.put("attribute", "xDvyz0ezL4e");
        gender.put("value", client.getGender());
        clientAttribute.put(gender);
        if (client.getAttributes().containsKey("Father_NRC_Number")) {
            JSONObject Father_NRC_Number = new JSONObject();
            Father_NRC_Number.put("attribute", "UpMkVyXSk4b");
            Father_NRC_Number.put("value", client.getAttributes().get("Father_NRC_Number"));
            clientAttribute.put(Father_NRC_Number);
        }

        if (client.getAttributes().containsKey("Child_Register_Card_Number")) {
            JSONObject Child_Register_Card_Number = new JSONObject();
            Child_Register_Card_Number.put("attribute", "P5Ew7lka7GR");
            Child_Register_Card_Number.put("value", client.getAttributes().get("Child_Register_Card_Number"));
            clientAttribute.put(Child_Register_Card_Number);
        }
        if (client.getAttributes().containsKey("CHW_Phone_Number")) {
            JSONObject CHW_Phone_Number = new JSONObject();
            CHW_Phone_Number.put("attribute", "wCom53wUTKf");
            CHW_Phone_Number.put("value", client.getAttributes().get("CHW_Phone_Number"));
            clientAttribute.put(CHW_Phone_Number);
        }

        if (client.getAttributes().containsKey("CHW_Name")) {
            JSONObject CHW_Name = new JSONObject();
            CHW_Name.put("attribute", "t2C80PnQfJH");
            CHW_Name.put("value", client.getAttributes().get("CHW_Name"));
            clientAttribute.put(CHW_Name);
        }

        if (client.getAttributes().containsKey("Child_Birth_Certificate")) {
            JSONObject Child_Birth_Certificate = new JSONObject();
            Child_Birth_Certificate.put("attribute", "ZDWzVhjlgWK");
            Child_Birth_Certificate.put("value", client.getAttributes().get("Child_Birth_Certificate"));
            clientAttribute.put(Child_Birth_Certificate);
        }

        /////////////////////
        JSONArray enrollments = new JSONArray();
        JSONObject enrollmentsObj = new JSONObject();
        enrollmentsObj.put("orgUnit", "IDc0HEyjhvL");
        enrollmentsObj.put("program", "OprRhyWVIM6");
        enrollmentsObj.put("enrollmentDate", DateUtil.getTodayAsString());
        enrollmentsObj.put("incidentDate", DateUtil.getTodayAsString());
        enrollments.put(enrollmentsObj);

        clientData.put("attributes", clientAttribute);
        clientData.put("trackedEntity", "MCPQUTHX1Ze");
        clientData.put("orgUnit", "IDc0HEyjhvL");

        return dhis2TrackCaptureConnector.trackCaptureDataSendToDHIS2(clientData);
    }
}
