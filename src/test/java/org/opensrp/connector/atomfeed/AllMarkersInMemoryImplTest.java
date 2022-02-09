package org.opensrp.connector.atomfeed;

import org.apache.commons.lang3.NotImplementedException;
import org.junit.Before;
import org.junit.Test;

public class AllMarkersInMemoryImplTest {

	private AllMarkersInMemoryImpl allMarkersInMemory;

	@Before
	public void setup(){
		allMarkersInMemory = new AllMarkersInMemoryImpl();
	}

	@Test(expected = NotImplementedException.class)
	public void testGetMarkerListShouldReturnNotImplementedException() {
		allMarkersInMemory.getMarkerList();
	}
}
