package org.opensrp.connector.dhis2.location;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DHISUtils {

    public static Date parseDhisDate(String date) throws ParseException {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String updatedDate = date.replaceFirst("T", " ");
        try {
            return sdf.parse(updatedDate);
        } catch (ParseException e) {
            sdf.applyPattern("yyyy-MM-dd HH:mm:ss");
            return sdf.parse(date);
        }
    }
}
