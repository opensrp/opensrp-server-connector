package org.opensrp.connector.dhis2;

public class DHIS2LocationsImportSummary {

	private Integer lastPageSynced;

	private Integer numberOfRowsProcessed;

	private Integer DHISPageCount;

	private Integer DHISLocationsCount;

	private DHISImportLocationsJobStatus dhisImportLocationsJobStatus;

	public Integer getLastPageSynced() {
		return lastPageSynced;
	}

	public void setLastPageSynced(Integer lastPageSynced) {
		this.lastPageSynced = lastPageSynced;
	}

	public Integer getNumberOfRowsProcessed() {
		return numberOfRowsProcessed;
	}

	public void setNumberOfRowsProcessed(Integer numberOfRowsProcessed) {
		this.numberOfRowsProcessed = numberOfRowsProcessed;
	}

	public Integer getDHISPageCount() {
		return DHISPageCount;
	}

	public void setDHISPageCount(Integer DHISPageCount) {
		this.DHISPageCount = DHISPageCount;
	}

	public Integer getDHISLocationsCount() {
		return DHISLocationsCount;
	}

	public void setDHISLocationsCount(Integer DHISLocationsCount) {
		this.DHISLocationsCount = DHISLocationsCount;
	}

	public DHISImportLocationsJobStatus getDhisImportLocationsJobStatus() {
		return dhisImportLocationsJobStatus;
	}

	public void setDhisImportLocationsJobStatus(DHISImportLocationsJobStatus dhisImportLocationsJobStatus) {
		this.dhisImportLocationsJobStatus = dhisImportLocationsJobStatus;
	}
}
