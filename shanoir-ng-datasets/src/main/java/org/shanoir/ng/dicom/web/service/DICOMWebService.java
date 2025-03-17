package org.shanoir.ng.dicom.web.service;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

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
import org.shanoir.ng.dicom.WADOURLHandler;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

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
	
	@Value("${dcm4chee-arc.dicom.web.rs.upload}")
	private String dicomWebRSUpload;
	
	@Value("${dcm4chee-arc.dicom.web.http.client.max.total}")
	private int dicomWebHttpClientMaxTotal;

	@Value("${dcm4chee-arc.dicom.web.http.client.max.per.route}")
	private int dicomWebHttpClientMaxPerRoute;
	
	@Autowired
	private WADOURLHandler wadoURLHandler;

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

	public String findStudy(String studyInstanceUID, String includeField) {
		try {
			String url = this.serverURL + "?StudyInstanceUID=" + studyInstanceUID + "&includefield=" + includeField;
			HttpGet httpGet = new HttpGet(url);
			httpGet.setHeader("Accept-Charset", "UTF-8");
			try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
				HttpEntity entity = response.getEntity();
				if (entity != null) {
					return EntityUtils.toString(entity, "UTF-8");
				} else {
					LOG.error("DICOMWeb: findStudy: empty response entity for studyInstanceUID: " + studyInstanceUID);					
				}
			}
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
		return null;
	}

	public String findSeriesOfStudy(String studyInstanceUID, String includefield, String seriesInstanceUID) {
		try {
			String url = this.serverURL + "/" + studyInstanceUID + "/series";
			boolean isFirstQueryParam = true;
			if (includefield != null && !includefield.isEmpty()) {
				url += "?includefield=" + includefield;
				isFirstQueryParam = false;
			}
			if (seriesInstanceUID != null && !seriesInstanceUID.isEmpty()) {
				if (isFirstQueryParam) {
					url += "?SeriesInstanceUID=" + seriesInstanceUID;
				} else {
					url += "&SeriesInstanceUID=" + seriesInstanceUID;
				}
			}
			HttpGet httpGet = new HttpGet(url);
			httpGet.setHeader("Accept-Charset", "UTF-8");
			try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
				HttpEntity entity = response.getEntity();
				if (entity != null) {
					return  EntityUtils.toString(entity, "UTF-8");
				} else {
					LOG.error("DICOMWeb: findSeriesOfStudy: empty response entity for studyInstanceUID: " + studyInstanceUID);		
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
			httpGet.setHeader("Accept-Charset", "UTF-8");
			try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
				HttpEntity entity = response.getEntity();
				if (entity != null) {
					return EntityUtils.toString(entity, "UTF-8");
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
					if (!entity.isChunked() && entity.getContentLength() >= 0) {
						responseHeaders.setContentLength(entity.getContentLength());
					}					
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
					if (!entity.isChunked() && entity.getContentLength() >= 0) {
						responseHeaders.setContentLength(entity.getContentLength());
					}
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
			try(
				FileInputStream fileIS = new FileInputStream(dicomFile);
				ByteArrayInputStream byteArrIS = new ByteArrayInputStream(fileIS.readAllBytes());
			) {
				ContentBody contentBody = new InputStreamBody(byteArrIS, ContentType.create(CONTENT_TYPE_DICOM));
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
		HttpPost httpPost = new HttpPost(dcm4cheeProtocol + dcm4cheeHost + ":" + dcm4cheePort + dicomWebRSUpload);
		httpPost.setHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_MULTIPART+";type="+CONTENT_TYPE_DICOM+";boundary="+BOUNDARY);
		httpPost.setEntity(entity);
		try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
			int code = response.getCode();
			if (code != HttpStatus.OK.value() && code != HttpStatus.ACCEPTED.value()) {
				LOG.error("DICOMWeb: sendMultipartRequest: response code not 200, but: " + code);
				LOG.error("Associated message: " +  EntityUtils.toString(response.getEntity()));
				throw new ShanoirException("DICOMWeb: sendMultipartRequest: response code not 200, but: " + code);
			}
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
			throw new ShanoirException(e.getMessage());
		}
	}

	public void rejectExaminationFromPacs(String studyInstanceUID) throws ShanoirException {
		String rejectURL = this.serverURL + "/" + studyInstanceUID + REJECT_SUFFIX;
		rejectURLFromPacs(rejectURL);
	}

	public void rejectAcquisitionFromPacs(String studyInstanceUID, String seriesInstanceUID) throws ShanoirException {
		String rejectURL = this.serverURL + "/" + studyInstanceUID + "/series/" + seriesInstanceUID + REJECT_SUFFIX;
		rejectURLFromPacs(rejectURL);
	}

	public void rejectDatasetFromPacs(String url) throws ShanoirException {
		String rejectURL;
		if (wadoURLHandler.isWADO_URI(url)) {
			rejectURL = wadoURLHandler.convertWADO_URI_TO_WADO_RS(url) + REJECT_SUFFIX;
		} else {
			rejectURL = url + REJECT_SUFFIX;
		}
		rejectURLFromPacs(rejectURL);
	}

	private void rejectURLFromPacs(String url) throws ShanoirException {
		// STEP 1: Reject from the PACS
		HttpPost post = new HttpPost(url);
		post.setHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_JSON);
		try (CloseableHttpResponse response = httpClient.execute(post)) {
			if (HttpStatus.OK.value() == response.getCode()) {
				LOG.debug("Rejected from PACS: " + post);
			} else {
				LOG.error(response.getCode() + ": Could not reject instance from PACS: " + response.getReasonPhrase()
						+ " for rejectURL: " + url);
				// in case one URL is Not Found (no DICOM instance present), we continue with deletion
				if (response.getCode() == 404 && response.getReasonPhrase().startsWith("Not Found")) {
					return;
				} else {
					throw new ShanoirException(response.getCode() + ": Could not reject instance from PACS: " + response.getReasonPhrase()
							+ " for rejectURL: " + url);
				}
			}
		} catch (IOException e) {
			LOG.error("Could not reject instance from PACS: for rejectURL: " + url, e);
			throw new ShanoirException("Could not reject instance from PACS: for rejectURL: " + url, e);
		}
	}

	@Scheduled(cron = "0 */30 * * * *", zone="Europe/Paris")
	public void deleteDicomFilesFromPacs() throws ShanoirException {
		// Doc : https://smart-api.info/ui/be87344696148a41f577aca202ce84df#/IOCM-RS/deleteRejectedInstancesPermanently
		LOG.info("Scheduled call to delete all rejected instances from pacs.");
		String url = this.serverURL.substring(0, this.serverURL.indexOf("/aets/")) + REJECT_SUFFIX;
		HttpDelete httpDelete = new HttpDelete(url);
		httpDelete.setHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_JSON);
		try (CloseableHttpResponse response = httpClient.execute(httpDelete)) {
			if (response.getCode() == HttpStatus.OK.value()) {
				LOG.info("Deleted from PACS: " + url);
			} else {
				LOG.error(response.getCode() + ": Could not delete instance from PACS: " + response.getReasonPhrase()
						+ "for deleteURL: " + url);
				throw new ShanoirException(response.getCode() + ": Could not delete instance from PACS: " + response.getReasonPhrase()
						+ "for deleteURL: " + url);
			}
		} catch (IOException e) {
			LOG.error(e.getMessage(), e);
			throw new ShanoirException(e.getMessage());
		}
	}
	
}
