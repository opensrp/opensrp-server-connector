package org.opensrp.connector.dhis2;


import java.util.HashMap;
import java.util.Map;

public enum DHISImportLocationsJobStatus {

	RUNNING,
	COMPLETED,
	FAILED;

	private static final Map<String, DHISImportLocationsJobStatus> lookup = new HashMap<String, DHISImportLocationsJobStatus>();

	static {
		for (DHISImportLocationsJobStatus status : DHISImportLocationsJobStatus.values()) {
			lookup.put(status.name(), status);
		}
	}

	public static DHISImportLocationsJobStatus get(String status) {
		return lookup.get(status);
	}

}
