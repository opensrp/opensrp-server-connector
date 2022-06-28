package org.opensrp.connector.openmrs.service;


import static org.mockito.ArgumentMatchers.anyString;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opensrp.common.util.HttpResponse;
import org.opensrp.common.util.HttpUtil;
import org.opensrp.connector.SpringApplicationContextProvider;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.internal.WhiteboxImpl;
import org.smartregister.domain.Client;
import org.springframework.beans.factory.annotation.Autowired;

@RunWith(PowerMockRunner.class)
@PrepareForTest(HttpUtil.class)
@PowerMockIgnore({"com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "org.w3c.dom.*"})
public class PatientServiceTest extends SpringApplicationContextProvider {

    @Autowired
    private PatientService patientService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        patientService = new PatientService();
    }

    @Test
    public void testCreatePatientWhenAllIdentifiersAreProvidedShouldReturnCorrectValues() throws Exception {
        PowerMockito.mockStatic(HttpUtil.class);
        PatientService spyPatientService = Mockito.spy(patientService);
        Client client = new Client("Sdf");
        client.addIdentifier(PatientService.OPENMRS_UUID_IDENTIFIER_TYPE, "2323-adsa-23");
        client.addIdentifier("zeir_id", "343-dsfdsf-23");
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("uuid", "2323");
        Mockito.doReturn(jsonObject).when(spyPatientService).createPerson(client);//getIdentifierTypeUUID
        Mockito.doReturn("30").when(spyPatientService).getIdentifierTypeUUID("zeir_id");
        Mockito.doReturn("https://").when(spyPatientService).getURL();
        Mockito.doReturn("80").when(spyPatientService).getIdentifierTypeUUID(PatientService.OPENMRS_UUID_IDENTIFIER_TYPE);
        Mockito.doReturn("800").when(spyPatientService).getIdentifierTypeUUID(PatientService.OPENSRP_IDENTIFIER_TYPE);
        String expected = "{\"person\":\"2323\"," + "\"identifiers\":["
                + "{\"identifier\":\"343-dsfdsf-23\",\"location\":\"Unknown Location\",\"identifierType\":\"30\"},"
                + "{\"identifier\":\"2323-adsa-23\",\"location\":\"Unknown Location\",\"identifierType\":\"80\"},"
                + "{\"identifier\":\"Sdf\",\"location\":\"Unknown Location\",\"identifierType\":\"800\",\"preferred\":true}]}";
        PowerMockito.when(HttpUtil.class, "post", anyString(), anyString(), Mockito.eq(expected), Mockito.isNull(), Mockito.isNull())
                .thenReturn(new HttpResponse(true, new JSONObject().toString()));
        Assert.assertNotNull(spyPatientService.createPatient(client));
    }

    @Test
    public void testCreatePatientWhenOneOfTheIdentifiersIsNullShouldReturnNonNullIdentifiers() throws Exception {
        PowerMockito.mockStatic(HttpUtil.class);
        PatientService spyPatientService = Mockito.spy(patientService);
        Client client = new Client("Sdf");
        client.addIdentifier(PatientService.OPENMRS_UUID_IDENTIFIER_TYPE, null);
        client.addIdentifier("zeir_id", "343-dsfdsf-23");
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("uuid", "2323");
        Mockito.doReturn(jsonObject).when(spyPatientService).createPerson(client);
        Mockito.doReturn("30").when(spyPatientService).getIdentifierTypeUUID("zeir_id");
        Mockito.doReturn("https://").when(spyPatientService).getURL();
        Mockito.doReturn("80").when(spyPatientService).getIdentifierTypeUUID(PatientService.OPENMRS_UUID_IDENTIFIER_TYPE);
        Mockito.doReturn("800").when(spyPatientService).getIdentifierTypeUUID(PatientService.OPENSRP_IDENTIFIER_TYPE);
        String expected = "{\"person\":\"2323\"," + "\"identifiers\":["
                + "{\"identifier\":\"343-dsfdsf-23\",\"location\":\"Unknown Location\",\"identifierType\":\"30\"},"
                + "{\"identifier\":\"Sdf\",\"location\":\"Unknown Location\",\"identifierType\":\"800\",\"preferred\":true}]}";
        PowerMockito.when(HttpUtil.class, "post", anyString(), anyString(), Mockito.eq(expected), Mockito.isNull(), Mockito.isNull())
                .thenReturn(new HttpResponse(true, new JSONObject().toString()));
        Assert.assertNotNull(spyPatientService.createPatient(client));
    }

    @Test
    public void testAddPersonAttributeShouldFillJsonObjectCorrectly() throws Exception {
        PowerMockito.mockStatic(HttpUtil.class);
        Client client = new Client("Sdf");
        PatientService spyPatientService = Mockito.spy(patientService);
        Mockito.doReturn("800").when(spyPatientService).getIdentifierTypeUUID(PatientService.OPENSRP_IDENTIFIER_TYPE);
        JSONObject jsonObject = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        WhiteboxImpl.invokeMethod(spyPatientService, "addPersonAttributes", client, jsonObject, jsonArray);
        Assert.assertEquals(1, jsonObject.length());
        Assert.assertTrue(jsonObject.has("identifiers"));
    }
}
