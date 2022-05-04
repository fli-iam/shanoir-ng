package org.shanoir.ng.shared.service;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpHeaders;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class GenericDicomService {

	/** Logger. */
	private static final Logger LOG = LoggerFactory.getLogger(StowRsDicomService.class);

	/** Mime type */
	private static final String CONTENT_TYPE_JSON = "application/json";

	private static final String CONTENT_TYPE = "&contentType";
	private static final String REJECT_SUFFIX = "/reject/113001%5EDCM";
	
	public void deleteDicomFilesFromPacs(String url) throws ShanoirException {
		String instanceId;
		String studyId;
		String serieId;
		String rejectURL;
		String deleteUrl;

		if (url.contains("requestType=WADO")) {
			instanceId = this.extractInstanceUID(url, null);
			studyId = this.extractStudyUID(url, null);
			serieId = this.extractSeriesUIDUID(url, null);

			// http://localhost:8081/dcm4chee-arc/aets/DCM4CHEE/rs/studies//series//instances//reject/113001%5EDCM
			rejectURL = url.substring(0, url.indexOf("wado?")) + "rs/studies/" + studyId + "/series/" + serieId
					+ "/instances/" + instanceId + REJECT_SUFFIX;

			deleteUrl = url.substring(0, url.indexOf("/aets/")) + REJECT_SUFFIX;
		} else {
			// /studies/{study}/series/{series}/instances/{instance}/rendered
			Pattern p  = Pattern.compile(".*//studies//(.*)//series//(.*)//instances//(.*)");
			Matcher m = p.matcher(url);
			if (m.find()) {
				studyId = m.group(1);
				serieId = m.group(2);
				instanceId = m.group(3);
			}

			rejectURL = url + REJECT_SUFFIX;
			deleteUrl = url.substring(0, url.indexOf("/aets/")) + REJECT_SUFFIX;
		}

		try(CloseableHttpClient httpClient = HttpClients.createDefault()) {
			HttpPost post = new HttpPost(rejectURL);
			post.setHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_JSON);
			CloseableHttpResponse response = httpClient.execute(post);
			if (response.getStatusLine().getStatusCode() == HttpStatus.NO_CONTENT.value()) {
				LOG.info("Rejected from PACS: " + url);
			} else {
				LOG.error(response.getStatusLine().getStatusCode() + ": Could not reject instance from PACS: "
						+ response.getStatusLine().getReasonPhrase());
				throw new ShanoirException("Could not reject instance from PACS: " + rejectURL);
			}
			// STEP 2: Delete from the PACS
			HttpDelete delete = new HttpDelete(deleteUrl);
			delete.setHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_JSON);
			response = httpClient.execute(delete);
			if (response.getStatusLine().getStatusCode() == HttpStatus.OK.value()) {
				LOG.info("Deleted from PACS: " + url);
			} else {
				LOG.error(response.getStatusLine().getStatusCode() + ": Could not delete instance from PACS: "
						+ response.getStatusLine().getReasonPhrase());
				throw new ShanoirException("Could not delete instance from PACS: " + deleteUrl);
			}
		} catch (ClientProtocolException e) {
			throw new ShanoirException("ClientProtocolException during delete from pacs: " + url, e);
		} catch (IOException e) {
			throw new ShanoirException("IOException during delete from pacs: " + url, e);
		}
	}

	/**
	 * The instanceUID (== objectUID) is inside the URL string
	 * and has to be extracted to be used.
	 * 
	 * @param url
	 * @param instanceUID
	 * @return
	 */
	private String extractInstanceUID(String url, String instanceUID) {
		return extractUidPattern(url, "objectUID", url.indexOf(CONTENT_TYPE) != -1 ? CONTENT_TYPE : null, instanceUID);
	}

	/**
	 * The studyID is inside the URL string
	 * and has to be extracted to be used.
	 * 
	 * @param url
	 * @param defaultUID
	 * @return the studyUID
	 */
	private String extractStudyUID(String url, String studyUID) {
		return extractUidPattern(url, "studyUID", "&seriesUID", studyUID);
	}

	/**
	 * The series UID is inside the URL string
	 * and has to be extracted to be used.
	 * 
	 * @param url
	 * @param defaultUID
	 * @return the seriesUID
	 */
	private String extractSeriesUIDUID(String url, String seriesUID) {
		return extractUidPattern(url, "seriesUID", "&objectUID", seriesUID);
	}

	private String extractUidPattern(String url, String uidName, String endPattern, String defaultValue) {
		Pattern p  = Pattern.compile(".*" + uidName + "=(.*)" + (endPattern != null ? endPattern + ".*" : ""));
		Matcher m = p.matcher(url);
		if (m.find()) {
			defaultValue = m.group(1);
		}
		return defaultValue;
	}
}
