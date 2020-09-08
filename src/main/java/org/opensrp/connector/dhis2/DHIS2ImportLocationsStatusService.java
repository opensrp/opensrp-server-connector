package org.opensrp.connector.dhis2;

import org.opensrp.domain.AppStateToken;
import org.opensrp.repository.AppStateTokensRepository;
import org.springframework.stereotype.Service;

@Service
public class DHIS2ImportLocationsStatusService {

	private final AppStateTokensRepository allAppStateTokens;

	public DHIS2ImportLocationsStatusService(AppStateTokensRepository allAppStateTokens) {
		this.allAppStateTokens = allAppStateTokens;
	}

	public DHIS2LocationsImportSummary getSummaryOfDHISImportsFromAppStateTokens() {
		DHIS2LocationsImportSummary dhis2LocationsImportSummary = new DHIS2LocationsImportSummary();
		AppStateToken dhisLastSyncPageToken = allAppStateTokens.findByName("DHIS2-LAST-SYNC-PAGE") != null ?
				allAppStateTokens.findByName("DHIS2-LAST-SYNC-PAGE").get(0) : null;

		AppStateToken dhisRowsProcessed = allAppStateTokens.findByName("DHIS2-LOCATION-ROWS-PROCESSED") != null ?
				allAppStateTokens.findByName("DHIS2-LOCATION-ROWS-PROCESSED").get(0) : null;

		AppStateToken pageCountToken = allAppStateTokens.findByName("DHIS-PAGE-COUNT") != null ?
				allAppStateTokens.findByName("DHIS-PAGE-COUNT").get(0) : null;

		AppStateToken totalLocationsToken = allAppStateTokens.findByName("TOTAL-LOCATIONS") != null ?
				allAppStateTokens.findByName("TOTAL-LOCATIONS").get(0) : null;

		AppStateToken dhisLocationJobStatus = allAppStateTokens.findByName("DHIS-LOCATIONS-JOB-STATUS") != null ?
				allAppStateTokens.findByName("DHIS-LOCATIONS-JOB-STATUS").get(0) : null;

		dhis2LocationsImportSummary.setLastPageSynced(Integer.parseInt((String) dhisLastSyncPageToken.getValue()));
		dhis2LocationsImportSummary.setNumberOfRowsProcessed(Integer.parseInt((String) dhisRowsProcessed.getValue()));
		dhis2LocationsImportSummary.setDhisPageCount(Integer.parseInt((String) pageCountToken.getValue()));
		dhis2LocationsImportSummary.setDhisLocationsCount(Integer.parseInt((String) totalLocationsToken.getValue()));
		dhis2LocationsImportSummary.setDhisImportLocationsJobStatus(dhisLocationJobStatus.getValue() != null ?
				DHISImportLocationsJobStatus.get((String) dhisLocationJobStatus.getValue()) : null);

		return dhis2LocationsImportSummary;

	}
}
