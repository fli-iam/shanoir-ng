package org.shanoir.ng.dicom;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

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
		if (url.contains(WADO_URI)) {
			return true;
		} else {
			return false;
		}
	}
	
	public String convertWADO_URI_TO_WADO_RS(String url) {
		return url
				.replace("wado?requestType=WADO", "rs")
				.replace("&studyUID=", "/studies/")
				.replace("&seriesUID=", "/series/")
				.replace("&objectUID=", "/instances/")
				.replace("&contentType=application/dicom", "");
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
			Pattern p = Pattern.compile(".*//studies//(.*)//series//(.*)//instances//(.*)");
			Matcher m = p.matcher(url);
			if (m.find()) {
				studyInstanceUID = m.group(1);
				seriesInstanceUID = m.group(2);
				sopInstanceUID = m.group(3);
			}
		}
		String[] uids = new String[]{studyInstanceUID, seriesInstanceUID, sopInstanceUID};
		return uids;
    }

	private String extractUIDPattern(String url, String uidName, String endPattern) {
	    Pattern p = Pattern.compile(".*" + uidName + "=(.*)" + (endPattern != null ? endPattern + ".*" : ""));
	    Matcher m = p.matcher(url);
	    return m.find() ? m.group(1) : null;
	}

}
