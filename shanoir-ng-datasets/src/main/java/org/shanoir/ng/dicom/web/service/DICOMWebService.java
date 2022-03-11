package org.shanoir.ng.dicom.web.service;

import javax.annotation.PostConstruct;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
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

	private CloseableHttpClient httpClient;

	private String serverURL;

	@Value("${dcm4chee-arc.protocol}")
	private String dcm4cheeProtocol;

	@Value("${dcm4chee-arc.host}")
	private String dcm4cheeHost;

	@Value("${dcm4chee-arc.dicom.web.rs}")
	private String dicomWebRS;

	@PostConstruct
	public void init() {
		this.serverURL = dcm4cheeProtocol + dcm4cheeHost + dicomWebRS;
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

}
