package org.opensrp.connector.dhis2.location;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.opensrp.connector.openmrs.service.TestResourceLoader;
import org.opensrp.domain.AppStateToken;
import org.opensrp.repository.AppStateTokensRepository;

import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.opensrp.connector.dhis2.location.DHIS2Constants.DHIS2_LAST_PAGE_SYNC_TOKEN_NAME;
import static org.opensrp.connector.dhis2.location.DHIS2Constants.DHIS_LOCATION_ROWS_PROCESSED_TOKEN_NAME;
import static org.opensrp.connector.dhis2.location.DHIS2Constants.DHIS_PAGE_COUNT_TOKEN_NAME;
import static org.opensrp.connector.dhis2.location.DHIS2Constants.TOTAL_LOCATIONS_TOKEN_NAME;
import static org.opensrp.connector.dhis2.location.DHIS2Constants.DHIS_LOCATION_JOB_STATUS_TOKEN_NAME;

public class DHIS2ImportLocationStatusServiceTest extends TestResourceLoader {

	@Mock
	private AppStateTokensRepository allAppStateTokens;

	@InjectMocks
	private DHIS2ImportLocationsStatusService dhis2ImportLocationsStatusService;

	public DHIS2ImportLocationStatusServiceTest() throws IOException {
		super();
	}

	@Before
	public void setup() {
		initMocks(this);
	}

	@Test
	public void testGetSummaryOfDHISImportsFromAppStateTokens() {
		List<AppStateToken> lastPageSyncToken = Collections.singletonList(new AppStateToken(DHIS2_LAST_PAGE_SYNC_TOKEN_NAME, "1", new Date().getTime()));
		List<AppStateToken> dhisRowsProcessed = Collections.singletonList(new AppStateToken(DHIS_LOCATION_ROWS_PROCESSED_TOKEN_NAME, "120", new Date().getTime()));
		List<AppStateToken> pageCountToken = Collections.singletonList(new AppStateToken(DHIS_PAGE_COUNT_TOKEN_NAME, "2", new Date().getTime()));
		List<AppStateToken> totalLocationsToken = Collections.singletonList(new AppStateToken(TOTAL_LOCATIONS_TOKEN_NAME, "1000", new Date().getTime()));
		List<AppStateToken> dhisLocationJobStatus = Collections.singletonList(new AppStateToken(DHIS_LOCATION_JOB_STATUS_TOKEN_NAME, DHISImportLocationsJobStatus.RUNNING.name(), new Date().getTime()));


		when(allAppStateTokens.findByName(DHIS2_LAST_PAGE_SYNC_TOKEN_NAME)).thenReturn(lastPageSyncToken);
		when(allAppStateTokens.findByName(DHIS_LOCATION_ROWS_PROCESSED_TOKEN_NAME)).thenReturn(dhisRowsProcessed);
		when(allAppStateTokens.findByName(DHIS_PAGE_COUNT_TOKEN_NAME)).thenReturn(pageCountToken);
		when(allAppStateTokens.findByName(TOTAL_LOCATIONS_TOKEN_NAME)).thenReturn(totalLocationsToken);
		when(allAppStateTokens.findByName(DHIS_LOCATION_JOB_STATUS_TOKEN_NAME)).thenReturn(dhisLocationJobStatus);
		DHIS2LocationsImportSummary dhis2LocationsImportSummary =
				dhis2ImportLocationsStatusService.getSummaryOfDHISImportsFromAppStateTokens();
		assertNotNull(dhis2LocationsImportSummary);
		assertEquals(new Integer(1),dhis2LocationsImportSummary.getLastPageSynced());
		assertEquals(new Integer(120),dhis2LocationsImportSummary.getNumberOfRowsProcessed());
		assertEquals(new Integer(2),dhis2LocationsImportSummary.getDhisPageCount());
		assertEquals(new Integer(1000),dhis2LocationsImportSummary.getDhisLocationsCount());
		assertEquals(DHISImportLocationsJobStatus.RUNNING,dhis2LocationsImportSummary.getDhisImportLocationsJobStatus());
	}

}
