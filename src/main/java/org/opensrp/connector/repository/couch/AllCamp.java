package org.opensrp.connector.repository.couch;

import java.util.List;

import org.ektorp.CouchDbConnector;
import org.ektorp.support.View;
import org.joda.time.DateTime;
import org.opensrp.common.AllConstants;
import org.opensrp.connector.domain.Camp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Profile("dhis2-sync-vaccine-tracker")
@Repository
public class AllCamp extends BaseRepository<Camp> {
	
	@Autowired
	public AllCamp(@Qualifier(AllConstants.OPENSRP_DATABASE_CONNECTOR) CouchDbConnector db) {
		super(Camp.class, db);
		
	}
	
	@View(name = "by_active", map = "function(doc) { if (doc.type === 'Camp' && doc._id && doc.status==true) { emit(doc.providerName, null); } }")
	public List<Camp> findAllActive() {
		List<Camp> camp = db.queryView(createQuery("by_active").includeDocs(true), Camp.class);
		if (camp == null || camp.isEmpty()) {
			return null;
		}
		return camp;
	}
	
	@View(name = "by_active", map = "function(doc) { if (doc.type === 'Camp' && doc._id && doc.status==true) { emit(doc.providerName, null); } }")
	public List<Camp> findAllActiveByProvider(String provider) {
		List<Camp> camp = db.queryView(createQuery("by_active").key(provider).includeDocs(true), Camp.class);
		if (camp == null || camp.isEmpty()) {
			return null;
		}
		return camp;
	}
	
	public void updateCamp(Camp camp) {
		camp.setStatus(false);
		camp.setTimestamp(DateTime.now().getMillis());
		camp.setId(camp.getId());
		camp.setRevision(camp.getRevision());
		this.update(camp);
	}
	
}
