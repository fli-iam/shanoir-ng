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

	public boolean isWADO_URI(String url) {
		return url.contains(WADO_URI);
	}
	
	public String convertWADO_URI_TO_WADO_RS(String url) {
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
		if (isWADO_URI(url)) {
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
		String[] uids = new String[] { studyInstanceUID, seriesInstanceUID, sopInstanceUID };
		return uids;
	}

	private String extractUIDPattern(String url, String uidName, String endPattern) {
	    Pattern p = Pattern.compile(".*" + uidName + "=(.*)" + (endPattern != null ? endPattern + ".*" : ""));
	    Matcher m = p.matcher(url);
	    return m.find() ? m.group(1) : null;
	}

}
