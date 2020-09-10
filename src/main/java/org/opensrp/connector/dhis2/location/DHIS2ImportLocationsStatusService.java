package org.opensrp.connector.dhis2.location;

import static org.opensrp.connector.dhis2.location.DHIS2Constants.DHIS2_LAST_PAGE_SYNC_TOKEN_NAME;
import static org.opensrp.connector.dhis2.location.DHIS2Constants.DHIS_LOCATION_JOB_STATUS_TOKEN_NAME;
import static org.opensrp.connector.dhis2.location.DHIS2Constants.DHIS_LOCATION_ROWS_PROCESSED_TOKEN_NAME;
import static org.opensrp.connector.dhis2.location.DHIS2Constants.DHIS_PAGE_COUNT_TOKEN_NAME;
import static org.opensrp.connector.dhis2.location.DHIS2Constants.TOTAL_LOCATIONS_TOKEN_NAME;

import org.opensrp.domain.AppStateToken;
import org.opensrp.repository.AppStateTokensRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class DHIS2ImportLocationsStatusService {

	private final AppStateTokensRepository allAppStateTokens;

	@Autowired
	public DHIS2ImportLocationsStatusService(AppStateTokensRepository allAppStateTokens) {
		this.allAppStateTokens = allAppStateTokens;
	}

	public DHIS2LocationsImportSummary getSummaryOfDHISImportsFromAppStateTokens() {
		DHIS2LocationsImportSummary dhis2LocationsImportSummary = new DHIS2LocationsImportSummary();

		AppStateToken dhisLastSyncPageToken = findByName(DHIS2_LAST_PAGE_SYNC_TOKEN_NAME);
		AppStateToken dhisRowsProcessed = findByName(DHIS_LOCATION_ROWS_PROCESSED_TOKEN_NAME);
		AppStateToken pageCountToken = findByName(DHIS_PAGE_COUNT_TOKEN_NAME);
		AppStateToken totalLocationsToken = findByName(TOTAL_LOCATIONS_TOKEN_NAME);
		AppStateToken dhisLocationJobStatus = findByName(DHIS_LOCATION_JOB_STATUS_TOKEN_NAME);

		dhis2LocationsImportSummary.setLastPageSynced(dhisLastSyncPageToken != null ? Integer.parseInt((String) dhisLastSyncPageToken.getValue()) : null);
		dhis2LocationsImportSummary.setNumberOfRowsProcessed(dhisRowsProcessed != null ? Integer.parseInt((String) dhisRowsProcessed.getValue()) : null);
		dhis2LocationsImportSummary.setDhisPageCount(pageCountToken != null ? Integer.parseInt((String) pageCountToken.getValue()) : null);
		dhis2LocationsImportSummary.setDhisLocationsCount(totalLocationsToken != null ? Integer.parseInt((String) totalLocationsToken.getValue()) : null);
		dhis2LocationsImportSummary.setDhisImportLocationsJobStatus(dhisLocationJobStatus != null ? (dhisLocationJobStatus.getValue() != null ?
				DHISImportLocationsJobStatus.valueOf((String) dhisLocationJobStatus.getValue()) : null) : null);

		return dhis2LocationsImportSummary;

	}

	private AppStateToken findByName(String tokenName) {
		return allAppStateTokens.findByName(tokenName) != null ?
				allAppStateTokens.findByName(tokenName).get(0) : null;
	}
}
