package org.opensrp.connector.atomfeed;

import java.net.URI;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.NotImplementedException;
import org.ict4h.atomfeed.client.domain.Marker;
import org.ict4h.atomfeed.client.repository.AllMarkers;
import org.springframework.context.annotation.Profile;

@Profile("atomfeed")
//@Repository
public class AllMarkersInMemoryImpl implements AllMarkers {
	
	private final HashMap<URI, Marker> map;
	
	public AllMarkersInMemoryImpl() {
		map = new HashMap<URI, Marker>();
	}
	
	@Override
	public Marker get(URI feedUri) {
		return map.get(feedUri);
	}
	
	@Override
	public void put(URI feedUri, String entryId, URI entryFeedUri) {
		map.put(feedUri, new Marker(feedUri, entryId, entryFeedUri));
	}

	@Override
	public List<Marker> getMarkerList() {
		throw new NotImplementedException();
	}

}
