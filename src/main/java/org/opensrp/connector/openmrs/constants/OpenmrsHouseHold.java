package org.opensrp.connector.openmrs.constants;

import org.smartregister.domain.Client;
import org.smartregister.domain.Event;

import java.util.ArrayList;
import java.util.List;

/**
 * Organizer for multiple Clients and Events as a single household.
 */
public class OpenmrsHouseHold {

    private HouseholdMember householdHead;

    private List<HouseholdMember> members = new ArrayList<>();

    public OpenmrsHouseHold(Client hhhead, Event hhhEncounter) {
        this.householdHead = new HouseholdMember(hhhead, hhhEncounter);
    }

    public void addHHMember(Client member, Event memberEncounter) {
        this.members.add(new HouseholdMember(member, memberEncounter));
    }

    public List<HouseholdMember> getMembers() {
        return members;
    }

    public HouseholdMember getHouseholdHead() {
        return householdHead;
    }

    public class HouseholdMember {

        private Client client;

        private List<Event> event = new ArrayList<>();

        HouseholdMember(Client client, Event event) {
            this.setClient(client);
            this.event.add(event);
        }

        public void addEvent(Event event) {
            this.event.add(event);
        }

        public Client getClient() {
            return client;
        }

        public void setClient(Client client) {
            this.client = client;
        }

        public List<Event> getEvent() {
            return event;
        }

        public void setEvent(List<Event> event) {
            this.event = event;
        }
    }
}
