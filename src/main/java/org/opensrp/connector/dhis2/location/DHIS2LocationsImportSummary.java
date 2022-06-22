package org.opensrp.connector.dhis2.location;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DHIS2LocationsImportSummary {

    private Integer lastPageSynced;

    private Integer numberOfRowsProcessed;

    private Integer dhisPageCount;

    private Integer dhisLocationsCount;

    private DHISImportLocationsJobStatus dhisImportLocationsJobStatus;

}
