package org.opensrp.connector.openmrs.service;

import java.io.IOException;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.opensrp.connector.dhis2.DHIS2DatasetPush;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

public class TestResourceLoader {
	protected static String openmrsOpenmrsUrl;
	protected static String openmrsUsername;
	protected static String openmrsPassword;
	protected String openmrsVersion;
	protected String formDirPath;
	protected String dhis2Url;
	protected String dhis2Username;
	protected String dhis2Password;
	protected boolean pushToOpenmrsForTest;
	protected PatientService patientService;
	protected EncounterService encounterService;
	protected DHIS2DatasetPush dhis2DatasetPush;
	protected OpenmrsLocationService openmrsLocationService;
	protected String couchDBUserName;
	protected String couchDBPassword;
	
	public TestResourceLoader() throws IOException {
		Resource resource = new ClassPathResource("/opensrp.properties");
		Properties props = PropertiesLoaderUtils.loadProperties(resource);
		openmrsOpenmrsUrl = props.getProperty("openmrs.url");
		openmrsUsername = props.getProperty("openmrs.username");
		openmrsPassword = props.getProperty("openmrs.password");
		openmrsVersion = props.getProperty("openmrs.version");
		formDirPath = props.getProperty("form.directory.name");
		
		dhis2Url = props.getProperty("dhis2.url");
		dhis2Username = props.getProperty("dhis2.username");
		dhis2Password = props.getProperty("dhis2.password");
		
		String rc = props.getProperty("openmrs.test.make-rest-call");
		pushToOpenmrsForTest = StringUtils.isBlank(rc) ? false : Boolean.parseBoolean(rc);
		
		this.patientService = new PatientService(openmrsOpenmrsUrl, openmrsUsername, openmrsPassword);
		this.encounterService = new EncounterService(openmrsOpenmrsUrl, openmrsUsername, openmrsPassword, openmrsVersion);
		this.openmrsLocationService = new OpenmrsLocationService(openmrsOpenmrsUrl, openmrsUsername, openmrsPassword);
		this.dhis2DatasetPush = new DHIS2DatasetPush(dhis2Url, dhis2Username, dhis2Password);
		this.encounterService.setPatientService(patientService);
		couchDBUserName = props.getProperty("couchdb.username");
		couchDBPassword = props.getProperty("couchdb.password");
	}
	
	
}
