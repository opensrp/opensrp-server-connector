package org.opensrp.connector.atomfeed;

import org.apache.commons.lang3.NotImplementedException;
import org.junit.Before;
import org.junit.Test;

public class AllFailedEventsInMemoryImplTest {

    private AllFailedEventsInMemoryImpl allFailedEventsInMemory;

    @Before
    public void setUp() {
        allFailedEventsInMemory = new AllFailedEventsInMemoryImpl();
    }

    @Test(expected = NotImplementedException.class)
    public void testGetFailedEventRetryLogsShouldReturnNotImplementedException() {
        allFailedEventsInMemory.getFailedEventRetryLogs("id");
    }

    @Test(expected = NotImplementedException.class)
    public void testGetFailedEventsShouldReturnNotImplementedException() {
        allFailedEventsInMemory.getFailedEvents("id");
    }

    @Test(expected = NotImplementedException.class)
    public void testGetByEventIdShouldReturnNotImplementedException() {
        allFailedEventsInMemory.getByEventId("id");
    }
}
