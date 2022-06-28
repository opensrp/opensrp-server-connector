package org.opensrp.connector.dhis2.it;

import static org.junit.Assert.assertEquals;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opensrp.connector.dhis2.DHIS2SyncerListener;
import org.opensrp.connector.dhis2.Dhis2HttpUtils;
import org.opensrp.connector.openmrs.service.TestResourceLoader;
import org.opensrp.repository.postgres.ClientsRepositoryImpl;
import org.opensrp.repository.postgres.EventsRepositoryImpl;
import org.smartregister.domain.Client;
import org.smartregister.domain.Event;
import org.smartregister.domain.Obs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:test-applicationContext-opensrp-connector.xml")
public class DHIS2SyncerListenerTest extends TestResourceLoader {

    @Autowired
    private ClientsRepositoryImpl allClients;

    @Autowired
    private EventsRepositoryImpl allEvents;

    @Autowired
    private DHIS2SyncerListener dhis2SyncerListener;

    @Autowired
    private Dhis2HttpUtils dhis2HttpUtils;

    public DHIS2SyncerListenerTest() throws IOException {
        super();
        // TODO Auto-generated constructor stub
    }

    @Before
    public void setup() {
        allClients.removeAll();
    }

    @Test
    public void testPushToDHIS2() throws JSONException {
        /** Household ***/
        String baseEntityId = "130";
        Client household = (Client) new Client(baseEntityId).withFirstName("pamoom").withGender("female").withLastName("la")
                .withBirthdate(new DateTime(), false).withDateCreated(new DateTime());
        Map<String, Object> householdAttributes = new HashMap<>();
        householdAttributes.put("householdCode", "34Zoomrttt");
        household.setAttributes(householdAttributes);
        allClients.add(household);

        Event householdEvent = new Event(baseEntityId, "Household Registration", new DateTime(0l, DateTimeZone.UTC),
                "entityType", "provider", "locationId", "formSubmissionId");
        List<Obs> householdOservations = new ArrayList<>();
        householdOservations.add(getObsWithValue("Date_Of_Reg", "21-09-2017"));
        householdEvent.setObs(householdOservations);
        allEvents.add(householdEvent);

        JSONObject returns = dhis2SyncerListener.pushToDHIS2();
        JSONObject response = returns.getJSONObject("response");
        String expectedImport = "1";
        String actualImport = response.getString("imported");
        String expectedHttpStatusCode = "200";
        String actualHttpStatusCode = returns.getString("httpStatusCode");
        assertEquals(expectedImport, actualImport);
        assertEquals(expectedHttpStatusCode, actualHttpStatusCode);
        String trackReference = returns.getString("track");

        JSONArray importSummariesArray = response.getJSONArray("importSummaries");
        JSONObject importSummariesJsonObject = importSummariesArray.getJSONObject(0);
        String refId = importSummariesJsonObject.getString("reference");

        /*Clening data*/
        deleteEnrollment(refId);
        deleteTrackInstances(trackReference);
    }

    public Obs getObsWithValue(String fielCode, String value) {
        Obs obs = new Obs();
        obs.setFieldCode(fielCode);
        obs.setFieldDataType("text");
        obs.setFormSubmissionField(fielCode);

        List<Object> values = new ArrayList<Object>();
        values.add(value);
        obs.setValues(values);

        return obs;

    }

    public Obs getObsWithHumanReadableValue(String fielCode, String value) {
        Obs obs = new Obs();
        obs.setFieldCode(fielCode);
        obs.setFieldDataType("select one");
        obs.setFormSubmissionField(fielCode);
        List<Object> humanReadableValues = new ArrayList<Object>();
        humanReadableValues.add(value);
        obs.setHumanReadableValues(humanReadableValues);
        return obs;

    }

    public void deleteEnrollment(String id) {
        String url = "enrollments/" + id;
        dhis2HttpUtils.delete(url, "", "");
    }

    public void deleteTrackInstances(String id) {
        String url = "trackedEntityInstances/" + id;
        dhis2HttpUtils.delete(url, "", "");
    }
}
