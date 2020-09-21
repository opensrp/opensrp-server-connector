package org.opensrp.connector.dhis2.location;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.opensrp.connector.openmrs.service.TestResourceLoader;
import org.opensrp.domain.AppStateToken;
import org.opensrp.repository.AppStateTokensRepository;
import org.opensrp.service.LocationTagService;
import org.opensrp.service.PhysicalLocationService;
import org.smartregister.domain.LocationTag;
import org.smartregister.domain.PhysicalLocation;

import java.io.IOException;
import java.util.ArrayList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doNothing;
import static org.mockito.MockitoAnnotations.initMocks;

public class DHIS2ImportOrganizationUnitsTest extends TestResourceLoader {

	@InjectMocks
	private DHIS2ImportOrganizationUnits dhis2ImportOrganizationUnits;

	@Mock
	private AppStateTokensRepository allAppStateTokens;

	@Mock
	private PhysicalLocationService physicalLocationService;

	@Mock
	private LocationTagService locationTagService;

	@Mock
	private Dhis2EndPoints dhis2EndPoints;

	public DHIS2ImportOrganizationUnitsTest() throws IOException {
		super();
	}


	@Before
	public void setup() {
		initMocks(this);
	}

	@Test
	public void testImportOrganizationUnits() {
		JSONObject apiResponse = createOrganisationUnitObject();

		PhysicalLocation physicalLocation = createPhysicalLocation();
	    JSONObject organisationUnitDetails = createOrganisationUnitDetailsObject();

		JSONObject organisationUnitGroupObject = new JSONObject();
		organisationUnitGroupObject.put("name", "org-unit-group");

		LocationTag locationTag = new LocationTag();
		locationTag.setName("test-loc-tag");
		ArrayList<LocationTag> locationTags = new ArrayList<>();
		locationTags.add(locationTag);

		when(dhis2EndPoints.getOrganisationalUnitList(anyInt())).thenReturn(apiResponse);
		when(physicalLocationService.getLocation(anyString(),anyBoolean())).thenReturn(physicalLocation);
		when(dhis2EndPoints.getOrganisationalUnit(anyString())).thenReturn(organisationUnitDetails);
		when(dhis2EndPoints.getOrganisationalUnitGroup(anyString())).thenReturn(organisationUnitGroupObject);
		doNothing().when(allAppStateTokens).add(any(AppStateToken.class));
		when(locationTagService.getAllLocationTags()).thenReturn(locationTags);
		when(locationTagService.addOrUpdateLocationTag(any(LocationTag.class))).thenReturn(locationTag);
		dhis2ImportOrganizationUnits.importOrganizationUnits("1");
		verify(physicalLocationService).addOrUpdate(any(PhysicalLocation.class));
	}

	private PhysicalLocation createPhysicalLocation() {
		PhysicalLocation physicalLocation = new PhysicalLocation();
		physicalLocation.setJurisdiction(true);
		return physicalLocation;
	}

	private JSONObject createOrganisationUnitDetailsObject() {
		JSONObject organisationUnitDetails = new JSONObject();
		JSONArray organisationUnitGroups = new JSONArray();
		JSONObject organisationUnitGroup = new JSONObject();
		organisationUnitGroup.put("id", "ORG-GROUP");

		organisationUnitGroups.put(organisationUnitGroup);
		organisationUnitDetails.put("organisationUnitGroups", organisationUnitGroups);
		organisationUnitDetails.put("code", "code");
		organisationUnitDetails.put("name", "Zambia");
		organisationUnitDetails.put("level", "2");
		JSONObject parentObject = new JSONObject();
		parentObject.put("id", "parentId");
		organisationUnitDetails.put("parent", parentObject);
		organisationUnitDetails.put("openingDate","1970-01-01T00:00:00.000");
		JSONArray ancestors = new JSONArray();
		JSONObject ancestor = new JSONObject();
		ancestor.put("id", "ancestor-1");
		ancestors.put(ancestor);
		organisationUnitDetails.put("ancestors", ancestors);
		organisationUnitDetails.put("id", "org-unit-1");
		JSONObject geometry = new JSONObject();
		geometry.put("type", "MultiPolygon");
		JSONArray cordinates = new JSONArray();
		cordinates.put(1123);
		geometry.put("coordinates",cordinates);
		organisationUnitDetails.put("geometry", geometry);
		return organisationUnitDetails;
	}

	private JSONObject createOrganisationUnitObject() {
		JSONObject apiResponse = new JSONObject();
		JSONObject pager = new JSONObject();

		pager.put("page", 1);
		pager.put("pageCount", 1);
		pager.put("total", 1);
		pager.put("pageSize", 50);

		JSONArray organisationUnits = new JSONArray();

		JSONObject hia2Report = new JSONObject();

		hia2Report.put("id", 1);
		hia2Report.put("displayName", "HIA2");

		organisationUnits.put(hia2Report);

		apiResponse.put("pager", pager);
		apiResponse.put("organisationUnits", organisationUnits);
		return apiResponse;
	}
}
