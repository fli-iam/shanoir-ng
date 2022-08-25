package org.shanoir.ng.dicom.web.service;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;

import org.apache.hc.client5.http.ClientProtocolException;
import org.apache.hc.client5.http.classic.methods.HttpDelete;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class DICOMWebService {

	private static final Logger LOG = LoggerFactory.getLogger(DICOMWebService.class);

	private static final String CONTENT_TYPE_MULTIPART = "multipart/related";
	
	private static final String CONTENT_TYPE_DICOM = "application/dicom";

	private static final String CONTENT_TYPE_JSON = "application/json";

	private static final String BOUNDARY = "--import_dicom_shanoir--";

	private static final String CONTENT_TYPE = "&contentType";

	private static final String REJECT_SUFFIX = "/reject/113001%5EDCM";

	private CloseableHttpClient httpClient;

	private String serverURL;

	@Value("${dcm4chee-arc.protocol}")
	private String dcm4cheeProtocol;

	@Value("${dcm4chee-arc.host}")
	private String dcm4cheeHost;

	@Value("${dcm4chee-arc.port.web}")
	private String dcm4cheePort;
	
	@Value("${dcm4chee-arc.dicom.web.rs}")
	private String dicomWebRS;

	@PostConstruct
	public void init() {
		this.serverURL = dcm4cheeProtocol + dcm4cheeHost + ":" + dcm4cheePort + dicomWebRS;
		try {
			httpClient = HttpClients.createDefault();
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
	}

	public String findStudy(String studyInstanceUID) {
		try {
			HttpGet httpGet = new HttpGet(this.serverURL + "?StudyInstanceUID=" + studyInstanceUID);
			CloseableHttpResponse response = httpClient.execute(httpGet);
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				return EntityUtils.toString(entity);
			}
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
		return null;
	}

	public String findSeriesOfStudy(String studyInstanceUID) {
		try {
			String url = this.serverURL + "/" + studyInstanceUID + "/series";
			HttpGet httpGet = new HttpGet(url);
			CloseableHttpResponse response = httpClient.execute(httpGet);
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				return EntityUtils.toString(entity);
			}
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
		return null;
	}

	public String findSerieMetadataOfStudy(String studyInstanceUID, String serieInstanceUID) {
		try {
			String url = this.serverURL + "/" + studyInstanceUID + "/series/" + serieInstanceUID + "/metadata";
			HttpGet httpGet = new HttpGet(url);
			CloseableHttpResponse response = httpClient.execute(httpGet);
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				return EntityUtils.toString(entity);
			}
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
		return null;
	}

	public ResponseEntity findFrameOfStudyOfSerieOfInstance(String studyInstanceUID, String serieInstanceUID,
			String sopInstanceUID, String frame) {
		try {
			String url = this.serverURL + "/" + studyInstanceUID + "/series/" + serieInstanceUID + "/instances/"
					+ sopInstanceUID + "/frames/" + frame;
			HttpGet httpGet = new HttpGet(url);
			CloseableHttpResponse response = httpClient.execute(httpGet);
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				InputStreamResource inputStreamResource = new InputStreamResource(entity.getContent());
				HttpHeaders responseHeaders = new HttpHeaders();
				responseHeaders.setContentLength(entity.getContentLength());
				return new ResponseEntity(inputStreamResource, responseHeaders, HttpStatus.OK);
			}
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
		return null;
	}

	public void sendDicomFilesToPacs(File directoryWithDicomFiles) throws Exception {
		if (directoryWithDicomFiles == null || !directoryWithDicomFiles.exists() || !directoryWithDicomFiles.isDirectory()) {
			throw new ShanoirException("sendDicomFilesToPacs called with null, or file: not existing or not a directory.");
		}
		File[] dicomFiles = directoryWithDicomFiles.listFiles();
		LOG.info("Start: STOW-RS sending " + dicomFiles.length + " dicom files to PACS from folder: " + directoryWithDicomFiles.getAbsolutePath());

		try(CloseableHttpClient httpClient = HttpClients.createDefault()) {
			MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create().setBoundary(BOUNDARY);
			for (File dicomFile : dicomFiles) {
				multipartEntityBuilder.addBinaryBody("dcm_upload", dicomFile, ContentType.create(CONTENT_TYPE_DICOM), "filename");
			}
			HttpEntity entity = multipartEntityBuilder.build();
			HttpPost httpPost = new HttpPost(dcm4cheeProtocol + dcm4cheeHost + ":" + dcm4cheePort + dicomWebRS);
			httpPost.setHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_MULTIPART+";type="+CONTENT_TYPE_DICOM+";boundary="+BOUNDARY);
			httpPost.setEntity(entity);
			CloseableHttpResponse response = httpClient.execute(httpPost);
			response.getEntity();
			response.close();
		} catch (ClientProtocolException e) {
			LOG.error("ClientProtocolException during upload into pacs",e);
			throw e;
		} catch (IOException e) {
			LOG.error("IOException during upload into pacs",e);
			throw e;
		}
		LOG.info("Finished: STOW-RS sending " + dicomFiles.length + " dicom files to PACS from folder: " + directoryWithDicomFiles.getAbsolutePath());
	}

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
			// STEP 1: Reject from the PACS
			HttpPost post = new HttpPost(rejectURL);
			post.setHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_JSON);
			CloseableHttpResponse response = httpClient.execute(post);
			if (response.getCode() == HttpStatus.NO_CONTENT.value()) {
				LOG.info("Rejected from PACS: " + url);
			} else {
				LOG.error(response.getCode() + ": Could not reject instance from PACS: " + response.getReasonPhrase());
				throw new ShanoirException("Could not reject instance from PACS: " + rejectURL);
			}
			// STEP 2: Delete from the PACS
			HttpDelete delete = new HttpDelete(deleteUrl);
			delete.setHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_JSON);
			response = httpClient.execute(delete);
			if (response.getCode() == HttpStatus.OK.value()) {
				LOG.info("Deleted from PACS: " + url);
			} else {
				LOG.error(response.getCode() + ": Could not delete instance from PACS: " + response.getReasonPhrase());
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
