package org.opensrp.connector.openmrs.schedule;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opensrp.connector.openmrs.EncounterAtomfeed;
import org.opensrp.connector.openmrs.PatientAtomfeed;
import org.opensrp.connector.openmrs.constants.OpenmrsConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.concurrent.locks.ReentrantLock;

@Profile("atomfeed")
@Component
public class OpenmrsAtomfeedListener {

    private static final ReentrantLock lock = new ReentrantLock();
    private Logger log = LogManager.getLogger(getClass().getName());
    private PatientAtomfeed patientAtomfeed;

    private EncounterAtomfeed encounterAtomfeed;

    @Autowired
    public OpenmrsAtomfeedListener(PatientAtomfeed patientAtomfeed, EncounterAtomfeed encounterAtomfeed) {
        this.patientAtomfeed = patientAtomfeed;
        this.encounterAtomfeed = encounterAtomfeed;
    }

    public void syncAtomfeeds() {
        if (!lock.tryLock()) {
            log.warn("Not fetching atom feed records. It is already in progress.");
            return;
        }

        try {
            log.info("Running " + OpenmrsConstants.SCHEDULER_OPENMRS_ATOMFEED_SYNCER_SUBJECT);

            patientAtomfeed.processEvents();

            encounterAtomfeed.processEvents();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }
}
