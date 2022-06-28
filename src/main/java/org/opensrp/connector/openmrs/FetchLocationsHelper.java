package org.opensrp.connector.openmrs;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.opensrp.api.domain.Location;
import org.opensrp.common.util.HttpUtil;
import org.opensrp.connector.HttpUtils;
import org.opensrp.connector.openmrs.constants.ConnectorConstants;
import org.opensrp.connector.openmrs.service.OpenmrsLocationService;
import org.opensrp.connector.openmrs.service.OpenmrsService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class FetchLocationsHelper extends OpenmrsService {

    private static final String LOCATIONS_CACHE_NAME = "getAllOpenMRSlocations";
    private static Logger logger = LogManager.getLogger(FetchLocationsHelper.class);

    @Cacheable(LOCATIONS_CACHE_NAME)
    public synchronized List<Location> getAllOpenMRSlocations() {
        List<Location> allLocationsList = new ArrayList<>();
        return getAllLocations(allLocationsList, 0);
    }

    @CacheEvict(value = LOCATIONS_CACHE_NAME, allEntries = true)
    public synchronized void clearAllOpenMRSlocationsCached() {
        logger.info(String.format("OpenMRS locations cache %s cleared...", LOCATIONS_CACHE_NAME));
    }

    public List<Location> getAllLocations(List<Location> locationList, int startIndex) throws JSONException {
        String response = HttpUtils.getURL(
                HttpUtil.removeEndingSlash(OPENMRS_BASE_URL) + "/" + OpenmrsLocationService.LOCATION_URL
                        + "?v=custom:(uuid,display,name,tags:(uuid,display),parentLocation:(uuid,display))&limit=100&startIndex="
                        + startIndex, OPENMRS_USER, OPENMRS_PWD);
        logger.debug("response received : {} ", response);

        logger.info(String.format("Location fetch starting at index %d ", startIndex));

        JSONObject jsonObject = new JSONObject(response);
        JSONArray links = jsonObject.getJSONArray("links");

        if (!StringUtils.isBlank(response) && jsonObject.has(ConnectorConstants.RESULTS)) {
            JSONArray results = new JSONObject(response).getJSONArray(ConnectorConstants.RESULTS);
            for (int i = 0; i < results.length(); i++) {
                locationList.add(makeLocation(results.getJSONObject(i)));
            }
            if (links.length() == 2 || links.getJSONObject(0).getString("rel").equals("next"))
                return getAllLocations(locationList, startIndex + 100);
        }
        return locationList;
    }

    public Location makeLocation(String locationJson) throws JSONException {
        logger.debug("makeLocation: " + locationJson);
        JSONObject locationsJsonObject = new JSONObject(locationJson);
        Location parentLocation = getParent(locationsJsonObject);
        Location location = new Location(locationsJsonObject.getString(ConnectorConstants.UUID),
                locationsJsonObject.getString(ConnectorConstants.NAME), null, null, parentLocation, null, null);
        JSONArray tags = locationsJsonObject.getJSONArray(ConnectorConstants.TAGS);

        for (int i = 0; i < tags.length(); i++) {
            location.addTag(tags.getJSONObject(i).getString(ConnectorConstants.DISPLAY));
        }

        if (locationsJsonObject.has(ConnectorConstants.ATTRIBUTES)) {
            JSONArray attributes = locationsJsonObject.getJSONArray(ConnectorConstants.ATTRIBUTES);
            for (int i = 0; i < attributes.length(); i++) {
                JSONObject attribute = attributes.getJSONObject(i);
                boolean voided = attribute.optBoolean(ConnectorConstants.VOIDED);
                if (!voided) {
                    String ad = attribute.getString(ConnectorConstants.DISPLAY);
                    location.addAttribute(ad.substring(0, ad.indexOf(":")), ad.substring(ad.indexOf(":") + 2));
                }
            }
        }

        logger.debug("location: " + ReflectionToStringBuilder.toString(location));
        return location;
    }

    public Location makeLocation(JSONObject location) throws JSONException {
        return makeLocation(location.toString());
    }

    public Location getParent(JSONObject locobj) throws JSONException {
        JSONObject parentL = (locobj.has(ConnectorConstants.PARENT_LOCATION)
                && !locobj.isNull(ConnectorConstants.PARENT_LOCATION))
                ? locobj.getJSONObject(ConnectorConstants.PARENT_LOCATION)
                : null;

        if (parentL != null) {
            return new Location(parentL.getString(ConnectorConstants.UUID), parentL.getString(ConnectorConstants.DISPLAY),
                    null, getParent(parentL));
        }
        return null;
    }


}
