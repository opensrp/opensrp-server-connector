package org.opensrp.connector.openmrs.service;

import org.opensrp.common.util.HttpUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

@Component
public abstract class OpenmrsService {

    public static final SimpleDateFormat OPENMRS_DATE = new SimpleDateFormat("yyyy-MM-dd");
    public static final String PROBABLE_CAUSE_OF_DEATH_CONCEPT = "5002AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    public static final String OTHER_NON_CODED_CONCEPT = "5622AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    public static final String PARENT_CHILD_RELATIONSHIP = "8d91a210-c2cc-11de-8d13-0010c6dffd0f";
    public static final String SIBLING_SIBLING_RELATIONSHIP = "8d91a01c-c2cc-11de-8d13-0010c6dffd0f";
    public static final String OPENMRS_PROVIDER = "provider";
    public static final String CUSTOM_UUID_PARAM = "v=custom:(uuid)";
    @Value("#{opensrp['openmrs.url']}")
    protected String OPENMRS_BASE_URL;
    @Value("#{opensrp['openmrs.username']}")
    protected String OPENMRS_USER;
    @Value("#{opensrp['openmrs.password']}")
    protected String OPENMRS_PWD;
    @Value("#{opensrp['openmrs.version']}")
    protected String OPENMRS_VERSION;

    public OpenmrsService() {
    }

    public OpenmrsService(String openmrsUrl, String user, String password) {
        OPENMRS_BASE_URL = openmrsUrl;
        OPENMRS_USER = user;
        OPENMRS_PWD = password;
    }

    public OpenmrsService(String openmrsUrl, String user, String password, String openmrsVersion) {
        OPENMRS_BASE_URL = openmrsUrl;
        OPENMRS_USER = user;
        OPENMRS_PWD = password;
        OPENMRS_VERSION = openmrsVersion;
    }

    public static void main(String[] args) {
        System.out.println(OPENMRS_DATE.format(new Date()));
    }

    /**
     * returns url after trimming ending slash
     *
     * @return
     */
    public String getURL() {
        return HttpUtil.removeEndingSlash(OPENMRS_BASE_URL);
    }

    void setURL(String url) {
        OPENMRS_BASE_URL = url;
    }

}
