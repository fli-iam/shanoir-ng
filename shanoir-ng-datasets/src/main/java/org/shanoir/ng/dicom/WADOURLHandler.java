/**
 * Shanoir NG - Import, manage and share neuroimaging data
 * Copyright (C) 2009-2019 Inria - https://www.inria.fr/
 * Contact us on https://project.inria.fr/shanoir/
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/gpl-3.0.html
 */

package org.shanoir.ng.dicom;

import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class manages WADO-URLs, either WADO-RS or WADO-URI.
 *
 * @author mkain
 *
 */
@Component
public class WADOURLHandler {

    private static final String WADO_URI = "requestType=WADO";

    private static final String CONTENT_TYPE = "&contentType";

    public boolean isWadoUri(String url) {
        return url.contains(WADO_URI);
    }

    public String convertWadoUriToWadoRs(String url) {
        String[] patterns = {
                "wado\\?requestType=WADO", "rs",
                "&studyUID=", "/studies/",
                "&seriesUID=", "/series/",
                "&objectUID=", "/instances/",
                "&contentType=application/dicom", ""
        };
        for (int i = 0; i < patterns.length; i += 2) {
            Pattern pattern = Pattern.compile(patterns[i]);
            Matcher matcher = pattern.matcher(url);
            url = matcher.replaceAll(patterns[i + 1]);
        }
        return url;
    }

    /**
     * This method extracts all 3 UIDs from any WADO-RS or WADO-URI URL/link.
     * It returns a String array with 3 values:
     * [0] StudyInstanceUID, [1] SeriesInstanceUID, [2] SOPInstanceUID
     * @param url
     * @return
     */
    public String[] extractUIDs(String url) {
        String studyInstanceUID = null;
        String seriesInstanceUID = null;
        String sopInstanceUID = null;
        if (isWadoUri(url)) {
            studyInstanceUID = extractUIDPattern(url, "studyUID", "&seriesUID");
            seriesInstanceUID = extractUIDPattern(url, "seriesUID", "&objectUID");
            sopInstanceUID = extractUIDPattern(url, "objectUID", url.contains(CONTENT_TYPE) ? CONTENT_TYPE : null);
        } else {
            Pattern p = Pattern.compile(".*/studies/(.*)/series/(.*)/instances/(.*)");
            Matcher m = p.matcher(url);
            if (m.find()) {
                studyInstanceUID = m.group(1);
                seriesInstanceUID = m.group(2);
                sopInstanceUID = m.group(3);
            }
        }
        if (studyInstanceUID == null || seriesInstanceUID == null || sopInstanceUID == null)
            throw new IllegalArgumentException(String.format("DICOM UIDs can not be null: %s, %s, %s", studyInstanceUID,
                    seriesInstanceUID, sopInstanceUID));
        String[] uids = new String[] {studyInstanceUID, seriesInstanceUID, sopInstanceUID};
        return uids;
    }

    private String extractUIDPattern(String url, String uidName, String endPattern) {
        Pattern p = Pattern.compile(".*" + uidName + "=(.*)" + (endPattern != null ? endPattern + ".*" : ""));
        Matcher m = p.matcher(url);
        return m.find() ? m.group(1) : null;
    }

}
