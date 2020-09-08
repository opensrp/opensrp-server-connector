package org.opensrp.connector.dhis2;

public class DHIS2LocationsImportSummary {

	private Integer lastPageSynced;

	private Integer numberOfRowsProcessed;

	private Integer dhisPageCount;

	private Integer dhisLocationsCount;

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

	public Integer getDhisPageCount() {
		return dhisPageCount;
	}

	public void setDhisPageCount(Integer dhisPageCount) {
		this.dhisPageCount = dhisPageCount;
	}

	public Integer getDhisLocationsCount() {
		return dhisLocationsCount;
	}

	public void setDhisLocationsCount(Integer dhisLocationsCount) {
		this.dhisLocationsCount = dhisLocationsCount;
	}

	public DHISImportLocationsJobStatus getDhisImportLocationsJobStatus() {
		return dhisImportLocationsJobStatus;
	}

	public void setDhisImportLocationsJobStatus(DHISImportLocationsJobStatus dhisImportLocationsJobStatus) {
		this.dhisImportLocationsJobStatus = dhisImportLocationsJobStatus;
	}
}
