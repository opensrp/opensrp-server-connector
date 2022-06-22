package org.opensrp.connector.rapidpro;

import org.opensrp.domain.Camp;
import org.smartregister.domain.Client;

import java.util.Map;

public interface Message {

    public String message(Client client, Camp camp, Map<String, String> data);
}
