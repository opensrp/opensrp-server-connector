package org.opensrp.connector.dhis2;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;

@Component
public abstract class DHIS2Service {

    public static final SimpleDateFormat OPENMRS_DATE = new SimpleDateFormat("yyyy-MM-dd");
    @Value("#{opensrp['dhis2.url']}")
    protected String DHIS2_BASE_URL;
    @Value("#{opensrp['dhis2.username']}")
    protected String DHIS2_USER;
    @Value("#{opensrp['dhis2.password']}")
    protected String DHIS2_PWD;

    public DHIS2Service() {
    }

    public DHIS2Service(String dhis2Url, String user, String password) {
        DHIS2_BASE_URL = dhis2Url;
        DHIS2_USER = user;
        DHIS2_PWD = password;
    }

}
