package org.opensrp.connector.dhis2;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.MalformedURLException;

import static org.opensrp.connector.dhis2.DHIS2Constants.ORG_UNIT_GROUP_LIST_URL;

@Component
public class Dhis2EndPoints extends DHIS2Service{

	protected static Dhis2HttpUtils dhis2HttpUtils;

	@Autowired
	public Dhis2EndPoints(@Value("#{opensrp['dhis2.url']}") String dhis2Url,
			@Value("#{opensrp['dhis2.username']}") String user, @Value("#{opensrp['dhis2.password']}") String password) {
		super(dhis2Url, user, password);
		dhis2HttpUtils = new Dhis2HttpUtils(dhis2Url, user, password);
	}

	public static JSONObject getOrganisationalUnitList(Integer page) throws MalformedURLException, IOException {
		return dhis2HttpUtils.get(DHIS2Constants.ORG_UNIT_LIST_URL + (page == null ? "" : "?page=" + page), "");
	}

	public static JSONObject getOrganisationalUnit(String id) throws MalformedURLException, IOException {
		return dhis2HttpUtils.get(DHIS2Constants.ORG_UNIT_LIST_URL + "/" + id, "");
	}

	public static JSONObject getOrganisationalUnitGroup(String id) throws MalformedURLException, IOException {
		return dhis2HttpUtils.get(ORG_UNIT_GROUP_LIST_URL + "/" + id, "");
	}


}
