package org.shanoir.ng.dicom.web.service;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;

import org.apache.hc.client5.http.classic.methods.HttpDelete;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.mime.ContentBody;
import org.apache.hc.client5.http.entity.mime.InputStreamBody;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.entity.mime.MultipartPart;
import org.apache.hc.client5.http.entity.mime.MultipartPartBuilder;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

/**
 * This class handles all calls to the shanoir backup pacs using DICOMWeb.
 * For the exception handling: all query methods return null and log an error,
 * as the user will see an empty result list. For all storage calls, that are
 * important to arrive an exception is thrown back to the user to avoid silent
 * none storage.
 * 
 * @author mkain
 *
 */
@Component
public class DICOMWebService {

	private static final Logger LOG = LoggerFactory.getLogger(DICOMWebService.class);

	private static final String CONTENT_TYPE_MULTIPART = "multipart/related";
	
	private static final String RELATED = "related";

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
	
	@Value("${dcm4chee-arc.dicom.web.http.client.max.total}")
	private int dicomWebHttpClientMaxTotal;

	@Value("${dcm4chee-arc.dicom.web.http.client.max.per.route}")
	private int dicomWebHttpClientMaxPerRoute;

	@PostConstruct
	public void init() {
		this.serverURL = dcm4cheeProtocol + dcm4cheeHost + ":" + dcm4cheePort + dicomWebRS;
		try {
			final PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
	        cm.setMaxTotal(dicomWebHttpClientMaxTotal);
	        cm.setDefaultMaxPerRoute(dicomWebHttpClientMaxPerRoute);
			httpClient = HttpClients.custom().setConnectionManager(cm).build();
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
	}

	public String findStudy(String studyInstanceUID) {
		try {
			HttpGet httpGet = new HttpGet(this.serverURL + "?StudyInstanceUID=" + studyInstanceUID);
			try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
				HttpEntity entity = response.getEntity();
				if (entity != null) {
					return EntityUtils.toString(entity);
				} else {
					LOG.error("DICOMWeb: findStudy: empty response entity.");					
				}
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
			try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
				HttpEntity entity = response.getEntity();
				if (entity != null) {
					return EntityUtils.toString(entity);
				} else {
					LOG.error("DICOMWeb: findSeriesOfStudy: empty response entity.");				
				}
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
			try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
				HttpEntity entity = response.getEntity();
				if (entity != null) {
					return EntityUtils.toString(entity);
				} else {
					LOG.error("DICOMWeb: findSerieMetadataOfStudy: empty response entity.");				
				}
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
			try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
				HttpEntity entity = response.getEntity();
				if (entity != null) {
					ByteArrayResource byteArrayResource = new ByteArrayResource(EntityUtils.toByteArray(entity));
					HttpHeaders responseHeaders = new HttpHeaders();
					responseHeaders.setContentLength(entity.getContentLength());
					return new ResponseEntity(byteArrayResource, responseHeaders, HttpStatus.OK);
				} else {
					LOG.error("DICOMWeb: findFrameOfStudyOfSerieOfInstance: empty response entity.");				
				}
			}
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
		return null;
	}
	
	public ResponseEntity findInstance(String studyInstanceUID, String serieInstanceUID, String sopInstanceUID) {
		try {
			String url = this.serverURL + "/" + studyInstanceUID + "/series/" + serieInstanceUID + "/instances/" + sopInstanceUID;
			HttpGet httpGet = new HttpGet(url);
			try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
				HttpEntity entity = response.getEntity();
				if (entity != null) {
					ByteArrayResource byteArrayResource = new ByteArrayResource(EntityUtils.toByteArray(entity));
					HttpHeaders responseHeaders = new HttpHeaders();
					responseHeaders.setContentLength(entity.getContentLength());
					return new ResponseEntity(byteArrayResource, responseHeaders, HttpStatus.OK);
				} else {
					LOG.error("DICOMWeb: findInstance: empty response entity.");				
				}
			}
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
		return null;
	}

	public void sendDicomFilesToPacs(File directoryWithDicomFiles) throws ShanoirException {
		if (directoryWithDicomFiles == null || !directoryWithDicomFiles.exists() || !directoryWithDicomFiles.isDirectory()) {
			LOG.error("sendDicomFilesToPacs called with null, or file: not existing or not a directory.");
			throw new ShanoirException("sendDicomFilesToPacs called with null, or file: not existing or not a directory.");
		}
		File[] dicomFiles = directoryWithDicomFiles.listFiles();
		LOG.info("Start: STOW-RS sending " + dicomFiles.length + " dicom files to PACS from folder: " + directoryWithDicomFiles.getAbsolutePath());
		MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();
		multipartEntityBuilder.setBoundary(BOUNDARY);
		multipartEntityBuilder.setMimeSubtype(RELATED);
		// create one multipart part for each file
		for (File dicomFile : dicomFiles) {
			try(FileInputStream fileIS = new FileInputStream(dicomFile)) {
				ContentBody contentBody = new InputStreamBody(
						new ByteArrayInputStream(fileIS.readAllBytes()), ContentType.create(CONTENT_TYPE_DICOM));
				// build MultipartPart
				MultipartPartBuilder partBuilder = MultipartPartBuilder.create();
				partBuilder.addHeader(CONTENT_TYPE, CONTENT_TYPE_DICOM);
				partBuilder.setBody(contentBody);
				MultipartPart multipartPart = partBuilder.build();
				multipartEntityBuilder.addPart(multipartPart);
			} catch(Exception e) {
				LOG.error(e.getMessage(), e);
				throw new ShanoirException(e.getMessage());
			}
		}
		HttpEntity entity = multipartEntityBuilder.build();
		sendMultipartRequest(entity);
		LOG.info("Finished: STOW-RS sending " + dicomFiles.length + " dicom files to PACS from folder: " + directoryWithDicomFiles.getAbsolutePath());
	}

	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	public void sendDicomInputStreamToPacs(InputStream inputStream) throws ShanoirException {
		LOG.info("Start: STOW-RS sending dicom file input stream to PACS.");
		try {
			// create content body
			ContentBody contentBody = new InputStreamBody(
					new ByteArrayInputStream(inputStream.readAllBytes()), ContentType.create(CONTENT_TYPE_DICOM));
			// build MultipartPart
			MultipartPartBuilder partBuilder = MultipartPartBuilder.create();
			partBuilder.addHeader(CONTENT_TYPE, CONTENT_TYPE_DICOM);
			partBuilder.setBody(contentBody);
			MultipartPart multipartPart = partBuilder.build();
			// build MultipartEntity
			MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();
			multipartEntityBuilder.setBoundary(BOUNDARY);
			multipartEntityBuilder.setMimeSubtype(RELATED);
			multipartEntityBuilder.addPart(multipartPart);
			HttpEntity entity = multipartEntityBuilder.build();
			sendMultipartRequest(entity);
		} catch(Exception e) {
			LOG.error(e.getMessage(), e);
			throw new ShanoirException(e.getMessage());
		}
		LOG.info("Finished: STOW-RS sending dicom file input stream to PACS.");
	}

	private void sendMultipartRequest(HttpEntity entity) throws ShanoirException {
		HttpPost httpPost = new HttpPost(dcm4cheeProtocol + dcm4cheeHost + ":" + dcm4cheePort + dicomWebRS);
		httpPost.setHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_MULTIPART+";type="+CONTENT_TYPE_DICOM+";boundary="+BOUNDARY);
		httpPost.setEntity(entity);
		try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
			int code = response.getCode();
			if (code != HttpStatus.OK.value()) {
				LOG.error("DICOMWeb: sendMultipartRequest: response code not 200, but: " + code);
				throw new ShanoirException("DICOMWeb: sendMultipartRequest: response code not 200, but: " + code);
			}
		} catch (IOException e) {
			LOG.error(e.getMessage(), e);
			throw new ShanoirException(e.getMessage());
		}
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
			// http://localhost:8081/dcm4chee-arc/aets/AS_RECEIVED/rs/studies//series//instances//reject/113001%5EDCM
			rejectURL = url.substring(0, url.indexOf("wado?")) + "rs/studies/" + studyId + "/series/" + serieId
					+ "/instances/" + instanceId + REJECT_SUFFIX;
			deleteUrl = url.substring(0, url.indexOf("/aets/")) + REJECT_SUFFIX;
		} else {
			// /studies/{study}/series/{series}/instances/{instance}/rendered
			Pattern p = Pattern.compile(".*//studies//(.*)//series//(.*)//instances//(.*)");
			Matcher m = p.matcher(url);
			if (m.find()) {
				studyId = m.group(1);
				serieId = m.group(2);
				instanceId = m.group(3);
			}
			rejectURL = url + REJECT_SUFFIX;
			deleteUrl = url.substring(0, url.indexOf("/aets/")) + REJECT_SUFFIX;
		}
		// STEP 1: Reject from the PACS
		HttpPost post = new HttpPost(rejectURL);
		post.setHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_JSON);
		try (CloseableHttpResponse response = httpClient.execute(post)) {
			if (response.getCode() == HttpStatus.NO_CONTENT.value()) {
				LOG.info("Rejected from PACS: " + url);
			} else {
				LOG.error(response.getCode() + ": Could not reject instance from PACS: " + response.getReasonPhrase()
					+ "for rejectURL: " + rejectURL);
				throw new ShanoirException(response.getCode() + ": Could not reject instance from PACS: " + response.getReasonPhrase()
				+ "for rejectURL: " + rejectURL);
			}
		} catch (IOException e) {
			LOG.error(e.getMessage(), e);
			throw new ShanoirException(e.getMessage());
		}
		// STEP 2: Delete from the PACS
		HttpDelete delete = new HttpDelete(deleteUrl);
		delete.setHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_JSON);
		try (CloseableHttpResponse response = httpClient.execute(delete)) {
			if (response.getCode() == HttpStatus.OK.value()) {
				LOG.info("Deleted from PACS: " + url);
			} else {
				LOG.error(response.getCode() + ": Could not delete instance from PACS: " + response.getReasonPhrase()
					+ "for deleteURL: " + deleteUrl);
				throw new ShanoirException(response.getCode() + ": Could not delete instance from PACS: " + response.getReasonPhrase()
				+ "for deleteURL: " + deleteUrl);
			}
		} catch (IOException e) {
			LOG.error(e.getMessage(), e);
			throw new ShanoirException(e.getMessage());
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
