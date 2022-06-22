package org.opensrp.connector.atomfeed;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.ektorp.CouchDbConnector;
import org.ektorp.support.GenerateView;
import org.ektorp.support.View;
import org.ict4h.atomfeed.client.repository.AllMarkers;
import org.opensrp.connector.atomfeed.domain.Marker;
import org.opensrp.connector.openmrs.constants.OpenmrsConstants;
import org.opensrp.connector.repository.couch.BaseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

@Profile("atomfeed")
@Repository
public class AllMarkersCouchImpl extends BaseRepository<Marker> implements AllMarkers {

    private CouchDbConnector db;

    @Autowired
    public AllMarkersCouchImpl(@Value("#{opensrp['couchdb.atomfeed-db.revision-limit']}") int revisionLimit,
                               @Qualifier(OpenmrsConstants.ATOMFEED_DATABASE_CONNECTOR) CouchDbConnector db) {
        super(Marker.class, db);
        this.db = db;
        this.db.setRevisionLimit(revisionLimit);
    }

    @GenerateView
    public Marker findByfeedUri(String feedUri) {
        if (StringUtils.isBlank(feedUri))
            return null;
        List<Marker> ol = queryView("by_feedUri", feedUri);
        if (ol == null || ol.isEmpty()) {
            return null;
        }
        return ol.get(0);
    }

    @View(name = "all_markers", map = "function(doc) { if (doc.type === 'Marker') { emit(doc.feedUri); } }")
    public List<Marker> findAllMarkers() {
        return db.queryView(createQuery("all_markers").includeDocs(true), Marker.class);
    }

    @Override
    public org.ict4h.atomfeed.client.domain.Marker get(URI feedUri) {
        Marker marker = findByfeedUri(feedUri.toString());
        try {
            return marker == null ? null : marker.toMarker();
        } catch (URISyntaxException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public void put(URI feedUri, String entryId, URI entryFeedUri) {
        Marker doc = findByfeedUri(feedUri.toString());
        if (doc != null) {
            doc.setLastReadEntryId(entryId);
            doc.setFeedURIForLastReadEntry(entryFeedUri.toString());
            update(doc);
        } else {
            doc = new Marker(new org.ict4h.atomfeed.client.domain.Marker(feedUri, entryId, entryFeedUri));
            add(doc);
        }
    }

    @Override
    public List<org.ict4h.atomfeed.client.domain.Marker> getMarkerList() {
        throw new NotImplementedException();
    }

}
