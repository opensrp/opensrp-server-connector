package org.opensrp.connector.dhis2;

import static org.apache.commons.lang.exception.ExceptionUtils.getFullStackTrace;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.opensrp.api.domain.Location;
import org.opensrp.connector.openmrs.service.OpenmrsLocationService;
import org.opensrp.domain.AppStateToken;
import org.opensrp.domain.Hia2Indicator;
import org.opensrp.domain.Report;
import org.opensrp.service.ConfigService;
import org.opensrp.service.PhysicalLocationService;
import org.opensrp.service.ReportService;
import org.smartregister.domain.LocationProperty;
import org.smartregister.domain.PhysicalLocation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

enum DhisSchedulerConfig {
    dhis2_syncer_sync_report_by_date_updated,
    dhis2_syncer_sync_report_by_date_voided
}

@Component
public class DHIS2DatasetPush extends DHIS2Service {

    public static final String SCHEDULER_DHIS2_DATA_PUSH_SUBJECT = "DHIS2 Report Pusher";
    public static final String DATAVALUESET_ENDPOINT = "dataValueSets";
    public static final String DATASET_ENDPOINT = "dataSets";
    private static final ReentrantLock lock = new ReentrantLock();
    private static Logger logger = LogManager.getLogger(DHIS2DatasetPush.class.toString());
    protected final String EXTERNAL_ID = "externalId";
    private final String DHIS_ORGANIZATION_UNIT_ID = "dhis_ou_id";
    @Autowired
    protected ReportService reportService;
    @Autowired
    protected ConfigService config;
    @Autowired
    protected OpenmrsLocationService openmrsLocationService;
    @Autowired
    protected PhysicalLocationService physicalLocationService;
    protected Dhis2HttpUtils dhis2HttpUtils;
    @Value("#{opensrp['dhis2.opensrp.location'] ?: false }")
    private boolean usesOpensrpLocation;

    @Autowired
    public DHIS2DatasetPush(@Value("#{opensrp['dhis2.url']}") String dhis2Url,
                            @Value("#{opensrp['dhis2.username']}") String user, @Value("#{opensrp['dhis2.password']}") String password) {
        super(dhis2Url, user, password);
        dhis2HttpUtils = new Dhis2HttpUtils(dhis2Url, user, password);
    }

    public String getDHIS2ReportId(String reportName) throws JSONException {
        String reportId = "";
        JSONObject response = dhis2HttpUtils.get("dataSets.json?paging=false", "");

        if (!response.has("dataSets")) {
            throw new JSONException("Required dataSets key is absent");
        }

        JSONArray dataSets = response.getJSONArray("dataSets");

        for (int i = 0; i < dataSets.length(); i++) {
            JSONObject dataSet = dataSets.getJSONObject(i);
            String datasetId = dataSet.getString("id");
            String datasetName = dataSet.getString("displayName");

            if (datasetName.equals(reportName)) {
                reportId = datasetId;
                break;
            }
        }
        return reportId;
    }

    public JSONObject createDHIS2Dataset(Report report) throws JSONException {
        final String DATASET_KEY = "dataSet";
        final String COMPLETE_DATA_KEY = "completeDate";
        final String PERIOD_KEY = "period";
        final String ORG_UNIT_KEY = "orgUnit";
        final String DATA_VALUES_KEY = "dataValues";

        // prepare report data
        String reportId = this.getDHIS2ReportId(report.getReportType());
        String locationId = report.getLocationId();

        if (StringUtils.isBlank(reportId)) {
            logger.error("Dhis2 dataset with id for {} Not found. Add the dataset in dhis2.", reportId);
            return null;
        }

        if (StringUtils.isBlank(locationId)) {
            logger.error("HIA2 report does not have a location tag. Please add it. Report id: {}", report.getId());
            return null;
        }

        DateTime completeDataDate = report.getReportDate();
        String formatedCompleteDataDate = new SimpleDateFormat("yyyy-MM-dd").format(completeDataDate.toDate());
        String periodDate = new SimpleDateFormat("yyyyMM").format(completeDataDate.toDate());

        String dhis2OrgUnitId = "";
        if (!usesOpensrpLocation) {
            // get openmrs location and retrieve dhis2 org unit Id
            Location openmrsLocation = openmrsLocationService.getLocation(locationId);
            logger.info("[OpenmrsLocation]: {}", openmrsLocation);
            dhis2OrgUnitId =
                    openmrsLocation.getAttributes() != null ? (String) openmrsLocation.getAttribute(
                            DHIS_ORGANIZATION_UNIT_ID)
                            : null;
        } else {
            PhysicalLocation physicalLocation = physicalLocationService.getLocation(locationId, false, false);
            if (physicalLocation != null && physicalLocation.getProperties() != null) {
                LocationProperty locationProperty = physicalLocation.getProperties();
                if (locationProperty != null && locationProperty.getCustomProperties() != null) {
                    Map<String, String> customProperties = locationProperty.getCustomProperties();
                    dhis2OrgUnitId = customProperties.get(EXTERNAL_ID);
                }
            }
        }

        if (StringUtils.isBlank(dhis2OrgUnitId)) {
            logger.error(
                    "Dhis2 Organization unit for {} Not found. Check if the data attribute exists.", locationId);
            return null;
        }

        List<String> availableIndicators = availableIndicators(reportId);

        // get indicator data
        List<Hia2Indicator> indicators = report.getHia2Indicators();

        // generate the dhis2Dataset here
        JSONObject dhis2Dataset = new JSONObject();
        dhis2Dataset.put(DATASET_KEY, reportId);
        dhis2Dataset.put(COMPLETE_DATA_KEY, formatedCompleteDataDate);
        dhis2Dataset.put(PERIOD_KEY, periodDate);
        // completed date and period
        dhis2Dataset.put(ORG_UNIT_KEY, dhis2OrgUnitId);

        JSONArray dataValues = new JSONArray();

        for (Hia2Indicator indicator : indicators) {
            if (!("unknown").equals(indicator.getDhisId())) {
                if (StringUtils.isNotBlank(indicator.getDhisId()) && availableIndicators.contains(indicator.getDhisId())) {
                    JSONObject dataValue = new JSONObject();
                    dataValue.put("categoryOptionCombo", indicator.getCategoryOptionCombo());
                    dataValue.put("dataElement", indicator.getDhisId());
                    dataValue.put("value", indicator.getValue());
                    dataValues.put(dataValue);
                }

            }
        }

        dhis2Dataset.put(DATA_VALUES_KEY, dataValues);

        return dhis2Dataset;
    }

    public void pushToDHIS2() {
        if (!lock.tryLock()) {
            logger.warn("Not pushing records to dhis2. It is already in progress.");
            return;
        }

        try {
            // retrieve all the reports
            logger.info("RUNNING Push to DHIS2 at " + DateTime.now());

            AppStateToken lastsync = config
                    .getAppStateTokenByName(DhisSchedulerConfig.dhis2_syncer_sync_report_by_date_updated);
            if (lastsync == null) {
                config.registerAppStateToken(DhisSchedulerConfig.dhis2_syncer_sync_report_by_date_updated, 0,
                        "ScheduleTracker token to keep track of reports synced with DHIS2", true);
            }

            Long start = lastsync == null || lastsync.getValue() == null ? 0 : lastsync.longValue();

            List<Report> reports = reportService.findByServerVersion(start);
            logger.info("Report list size " + reports.size() + " [start]" + start);

            // process all reports and sync them to DHIS2
            for (Report report : reports) {
                try {
                    JSONObject dhis2DatasetToPush = this.createDHIS2Dataset(report);
                    if (dhis2DatasetToPush == null) {
                        continue;
                    }

                    JSONObject response = dhis2HttpUtils.post(DATAVALUESET_ENDPOINT, "", dhis2DatasetToPush.toString());
                    String status = response.optString("status");
                    if (DHIS2ResponseStatus.ERROR.name().equalsIgnoreCase(status)) {
                        logger.error(response);
                    } else if (DHIS2ResponseStatus.WARNING.name().equalsIgnoreCase(status)) {
                        logger.warn(response);
                    } else {
                        logger.info(response);
                    }
                    report.setStatus(status);
                    reportService.updateReport(report);
                    config.updateAppStateToken(DhisSchedulerConfig.dhis2_syncer_sync_report_by_date_updated,
                            report.getServerVersion());
                } catch (JSONException e) {
                    logger.error(e);
                }

            }
            logger.info("PUSH TO DHIS2 FINISHED AT " + DateTime.now());
        } catch (Exception e) {
            logger.error(
                    MessageFormat.format("{0} occurred while trying to push records to dhis2. Message: {1} with stack trace {2}",
                            e.toString(), e.getMessage(), getFullStackTrace(e)));
        } finally {
            lock.unlock();
        }
    }

    private List<String> availableIndicators(String reportId) {
        List<String> availableIds = new ArrayList<>();

        try {

            JSONObject response = dhis2HttpUtils.get(DATASET_ENDPOINT + "/" + reportId + ".json", "");
            if (response.has("dataSetElements")) {
                JSONArray dataSetElements = response.getJSONArray("dataSetElements");
                for (int i = 0; i < dataSetElements.length(); i++) {
                    JSONObject dataElementObject = dataSetElements.getJSONObject(i);
                    if (dataElementObject.has("dataElement")) {
                        JSONObject dataElement = dataElementObject.getJSONObject("dataElement");
                        if (dataElement != null && dataElement.has("id")) {
                            availableIds.add(dataElement.getString("id"));
                        }
                    }
                }

            }
        } catch (JSONException e) {
            logger.error("", e);
        }
        return availableIds;
    }
}
