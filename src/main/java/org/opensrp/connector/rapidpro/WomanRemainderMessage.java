package org.opensrp.connector.rapidpro;

import org.opensrp.domain.Camp;
import org.smartregister.domain.Client;

import java.util.Map;

public class WomanRemainderMessage implements Message {

    @Override
    public String message(Client client, Camp camp, Map<String, String> data) {

        String message = " AGAMIKAL " + camp.getDate() + "  apnar tikadaner  tarikh. Tika nite " + camp.getCampName()
                + " -e oboshshoi chole ashben.";
        return message;
    }

}
