package org.opensrp.connector.atomfeed;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.NotImplementedException;
import org.ict4h.atomfeed.client.domain.FailedEvent;
import org.ict4h.atomfeed.client.domain.FailedEventRetryLog;
import org.ict4h.atomfeed.client.repository.AllFailedEvents;
import org.springframework.context.annotation.Profile;

@Profile("atomfeed")
//@Repository
public class AllFailedEventsInMemoryImpl implements AllFailedEvents {
	
	private List<FailedEvent> failedEvents;
	
	public AllFailedEventsInMemoryImpl() {
		this.failedEvents = Collections.synchronizedList(new ArrayList<FailedEvent>());
	}
	
	@Override
	public FailedEvent get(String feedUri, String id) {
		for (FailedEvent failedEvent : failedEvents) {
			if (failedEvent.getFeedUri().equalsIgnoreCase(feedUri) && failedEvent.getEventId().equals(id))
				return failedEvent;
		}
		return null;
	}
	
	@Override
	public void addOrUpdate(FailedEvent failedEvent) {
		boolean modified = false;
		for (int i = 0; i < failedEvents.size(); i++) {
			if (failedEvent.getEventId().equals(failedEvents.get(i).getEventId())) {
				modified = true;
				failedEvents.set(i, failedEvent);
				break;
			}
		}
		if (modified)
			failedEvents.add(failedEvent);
	}
	
	@Override
	public List<FailedEvent> getOldestNFailedEvents(String feedUri, int numberOfFailedEvents, int failedEventMaxRetry) {
		if (numberOfFailedEvents < 1)
			throw new IllegalArgumentException("Number of failed events should at least be one.");
		
		List<FailedEvent> lastNFailedEvents = new ArrayList<FailedEvent>();
		for (int i = failedEvents.size() - 1; i >= 0; i--) {
			if (lastNFailedEvents.size() == numberOfFailedEvents)
				break;
			
			FailedEvent failedEvent = failedEvents.get(i);
			if (failedEvent.getFeedUri().toString().equalsIgnoreCase(feedUri)) {
				lastNFailedEvents.add(failedEvent);
			}
		}
		
		return lastNFailedEvents;
	}
	
	public List<FailedEvent> getAllFailedEvents(String feedUri) {
		List<FailedEvent> allFailedEvents = new ArrayList<FailedEvent>();
		for (int i = failedEvents.size() - 1; i >= 0; i--) {
			FailedEvent failedEvent = failedEvents.get(i);
			if (failedEvent.getFeedUri().toString().equalsIgnoreCase(feedUri)) {
				allFailedEvents.add(failedEvent);
			}
		}
		return allFailedEvents;
	}
	
	@Override
	public int getNumberOfFailedEvents(String feedUri) {
		int numberOfFailedEvents = 0;
		for (int i = failedEvents.size() - 1; i >= 0; i--) {
			if (failedEvents.get(i).getFeedUri().toString().equalsIgnoreCase(feedUri)) {
				numberOfFailedEvents++;
			}
		}
		return numberOfFailedEvents;
	}
	
	@Override
	public void remove(FailedEvent failedEvent) {
		for (int i = 0; i < failedEvents.size(); i++) {
			if (failedEvent.getEventId().equals(failedEvents.get(i).getEventId())) {
				failedEvents.remove(i);
				break;
			}
		}
	}
	
	@Override
	public void insert(FailedEventRetryLog failedEventRetryLog) {
		
	}

	@Override
	public List<FailedEvent> getFailedEvents(String s) {
		throw new NotImplementedException();
	}

	@Override
	public FailedEvent getByEventId(String s) {
		throw new NotImplementedException();
	}

	@Override
	public List<FailedEventRetryLog> getFailedEventRetryLogs(String s) {
		throw new NotImplementedException();
	}

}
