package org.opensrp.connector.rapidpro;

import org.opensrp.domain.Camp;
import org.smartregister.domain.Client;

import java.util.Map;

public class ChildAnnouncementMessage implements Message {

    @Override
    public String message(Client client, Camp camp, Map<String, String> data) {
        String message = "Ajke " + camp.getCenterName()
                + " -e Tika deya hobe. Apnar shishuke tika deyar jonno taratari kendre niye ashun.";
        return message;

    }

}
