package org.opensrp.connector.rapidpro;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.opensrp.domain.Camp;
import org.opensrp.service.ClientService;
import org.opensrp.service.RapidProServiceImpl;
import org.smartregister.domain.Client;
import org.smartregister.domain.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MessageService {

    private static Logger logger = LogManager.getLogger(MessageService.class.toString());

    private RapidProServiceImpl rapidproService;

    private ClientService clientService;

    public MessageService() {

    }

    @Autowired
    public MessageService(RapidProServiceImpl rapidproService, ClientService clientService) {

        this.rapidproService = rapidproService;
        this.clientService = clientService;
    }

    public void sentMessageToClient(MessageFactory messageFactory, List<Event> events, Camp camp) throws JSONException {

        if (events != null) {
            for (Event event : events) {
				/*				Map<String, String> data = action.data();
								logger.info("sentMessageToClient actiondata:" +  data.toString());*/
                if (event.getEntityType().equalsIgnoreCase(ClientType.child.name())) {
                    Client child = clientService.find(event.getBaseEntityId());
                    if (child != null) {
                        logger.info("sending message to child childBaseEntityId:" + child.getBaseEntityId());
                        Map<String, List<String>> relationships = child.getRelationships();
                        String motherId = relationships.get("mother").get(0);
                        Client mother = clientService.find(motherId);
                        logger.info("sending message to mother moterBaseEntityId:" + mother.getBaseEntityId());
                        generateDataAndsendMessageToRapidpro(mother, ClientType.child, messageFactory, camp);
                    }
                } else if (event.getEntityType().equalsIgnoreCase(ClientType.mother.name())) {
                    Client mother = clientService.find(event.getBaseEntityId());
                    if (mother != null) {
                        logger.info("sending message to mother moterBaseEntityId:" + mother.getBaseEntityId());
                        generateDataAndsendMessageToRapidpro(mother, ClientType.mother, messageFactory, camp);
                    }

                }
            }
        } else {
            logger.info("No vaccine data Found Today");
        }
    }

    private void generateDataAndsendMessageToRapidpro(Client client, ClientType clientType, MessageFactory messageFactory,
                                                      Camp camp) {

        Map<String, Object> attributes = new HashMap<>();
        attributes = client.getAttributes();
        List<String> urns;
        urns = new ArrayList<String>();
        if (attributes.containsKey("phoneNumber")) {
            logger.info("sending mesage to mobileno:" + addExtensionToMobile((String) attributes.get("phoneNumber")));
            urns.add("tel:" + addExtensionToMobile((String) attributes.get("phoneNumber")));
            List<String> contacts;
            contacts = new ArrayList<String>();
            List<String> groups = new ArrayList<String>();
            rapidproService.sendMessage(urns, contacts, groups,
                    messageFactory.getClientType(clientType).message(client, camp, null), "");
        }
    }

    private String addExtensionToMobile(String mobile) {
        String phoneNumber;
        if (mobile.length() == 10) {
            phoneNumber = new StringBuilder().append("+880").append(mobile).toString();
        } else if (mobile.length() > 10) {
            phoneNumber = new StringBuilder().append("+880").append(mobile.substring(mobile.length() - 10)).toString();
        } else {
            throw new IllegalArgumentException("invalid mobile no!!");
        }
        return phoneNumber;

    }

}
