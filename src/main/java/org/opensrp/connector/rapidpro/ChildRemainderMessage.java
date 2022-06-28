package org.opensrp.connector.rapidpro;

import org.opensrp.domain.Camp;
import org.smartregister.domain.Client;

import java.util.Map;

public class ChildRemainderMessage implements Message {

    @Override
    public String message(Client client, Camp camp, Map<String, String> data) {

        String message = "Agamikal " + camp.getDate() + "  apnar shishur  tikadaner  tarikh. Shishuke tika dite "
                + camp.getCenterName() + "-e oboshshoi niye ashben. Tika diye shishuke rog theke rokkha korun.";
        System.err.println("messageText: " + message);
        return message;
    }

}
