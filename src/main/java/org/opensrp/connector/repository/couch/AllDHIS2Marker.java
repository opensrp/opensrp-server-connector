package org.opensrp.connector.repository.couch;

import org.ektorp.CouchDbConnector;
import org.ektorp.support.GenerateView;
import org.opensrp.common.AllConstants;
import org.opensrp.common.AllConstants.DHIS2Constants;
import org.opensrp.connector.domain.DHIS2Marker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.List;

@Profile("dhis2-sync-vaccine-tracker")
@Repository
public class AllDHIS2Marker extends BaseRepository<DHIS2Marker> {

    @Autowired
    protected AllDHIS2Marker(@Qualifier(AllConstants.OPENSRP_DATABASE_CONNECTOR) CouchDbConnector db) {
        super(DHIS2Marker.class, db);
    }

    @GenerateView
    public List<DHIS2Marker> findByName(String name) {
        return queryView("by_name", name);
    }

    public void add() {
        DHIS2Marker dHIS2MarkerEntry = new DHIS2Marker();
        dHIS2MarkerEntry.setName(DHIS2Constants.DHIS2_TRACK_DATA_SYNCER_VERSION_MARKER);
        dHIS2MarkerEntry.setValue(0l);
        this.add(dHIS2MarkerEntry);
    }

    public void addEventMarker() {
        DHIS2Marker dHIS2MarkerEntry = new DHIS2Marker();
        dHIS2MarkerEntry.setName(DHIS2Constants.DHIS2_TRACK_DATA_SYNCER_VERSION_MARKER_EVENT);
        dHIS2MarkerEntry.setValue(0l);
        this.add(dHIS2MarkerEntry);
    }

    public void update(long ServerVersion) {
        try {
            DHIS2Marker lastsync = this.findByName(DHIS2Constants.DHIS2_TRACK_DATA_SYNCER_VERSION_MARKER).get(0);
            lastsync.setValue(ServerVersion);
            this.update(lastsync);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateEventMarker(long ServerVersion) {
        try {
            DHIS2Marker lastsync = this.findByName(DHIS2Constants.DHIS2_TRACK_DATA_SYNCER_VERSION_MARKER_EVENT).get(0);
            lastsync.setValue(ServerVersion);
            this.update(lastsync);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
