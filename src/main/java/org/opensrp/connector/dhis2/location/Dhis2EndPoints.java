package org.opensrp.connector.dhis2.location;

import static org.opensrp.connector.dhis2.location.DHIS2Constants.ORG_UNIT_GROUP_LIST_URL;

import org.json.JSONObject;
import org.opensrp.connector.dhis2.DHIS2Service;
import org.opensrp.connector.dhis2.Dhis2HttpUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class Dhis2EndPoints extends DHIS2Service{

	protected Dhis2HttpUtils dhis2HttpUtils;

	@Autowired
	public Dhis2EndPoints(@Value("#{opensrp['dhis2.url']}") String dhis2Url,
			@Value("#{opensrp['dhis2.username']}") String user, @Value("#{opensrp['dhis2.password']}") String password) {
		super(dhis2Url, user, password);
		dhis2HttpUtils = new Dhis2HttpUtils(dhis2Url, user, password);
	}

	public JSONObject getOrganisationalUnitList(Integer page) {
		return dhis2HttpUtils.get(DHIS2Constants.ORG_UNIT_LIST_URL + (page == null ? "" : "?page=" + page), "");
	}

	public JSONObject getOrganisationalUnit(String id) {
		return dhis2HttpUtils.get(DHIS2Constants.ORG_UNIT_LIST_URL + "/" + id, "");
	}

	public JSONObject getOrganisationalUnitGroup(String id)  {
		return dhis2HttpUtils.get(ORG_UNIT_GROUP_LIST_URL + "/" + id, "");
	}


}
