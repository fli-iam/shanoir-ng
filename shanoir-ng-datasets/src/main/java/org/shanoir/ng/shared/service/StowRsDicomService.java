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

package org.shanoir.ng.shared.service;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 * This class sends dicom files from a directory to a PACS on using stow-rs REST.
 * 
 * @author mkain
 *
 */
@Component(value = "stowrs")
public class StowRsDicomService implements DicomServiceApi {

	/** Logger. */
	private static final Logger LOG = LoggerFactory.getLogger(StowRsDicomService.class);

	/** Mime type */
	private static final String CONTENT_TYPE_MULTIPART = "multipart/related";
	private static final String CONTENT_TYPE_DICOM = "application/dicom";
	private static final String CONTENT_TYPE_JSON = "application/json";

	private static final String BOUNDARY = "--import_dicom_shanoir--";
	private static final String CONTENT_TYPE = "&contentType";
	private static final String REJECT_SUFFIX = "/reject/113001%5EDCM";
	
	@Value("${dcm4chee-arc.protocol}")
	private String dcm4cheeProtocol;

	@Value("${dcm4chee-arc.host}")
	private String dcm4cheeHost;

	@Value("${dcm4chee-arc.port.web}")
	private String dcm4cheePortWeb;

	@Value("${dcm4chee-arc.dicom.web.rs}")
	private String dicomWebRS;

	@Override
	public void sendDicomFilesToPacs(File directoryWithDicomFiles) throws Exception {
		if (directoryWithDicomFiles == null || directoryWithDicomFiles.exists() || directoryWithDicomFiles.isDirectory()) {
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
			HttpPost httpPost = new HttpPost(dcm4cheeProtocol + dcm4cheeHost + ":" + dcm4cheePortWeb + dicomWebRS);
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

	@Override
	public void deleteDicomFilesFromPacs(String url) throws Exception {
		String instanceId = this.extractInstanceUID(url, null);
		String studyId = this.extractStudyUID(url, null);
		String serieId = this.extractSeriesUIDUID(url, null);
		
		try(CloseableHttpClient httpClient = HttpClients.createDefault()) {
			// Step one, reject from the PACS
			// http://localhost:8081/dcm4chee-arc/aets/DCM4CHEE/rs/studies//series//instances//reject/113001%5EDCM
			String rejectURL = url.substring(0, url.indexOf("wado?")) + "rs/studies/" + studyId + "/series/" + serieId
					+ "/instances/" + instanceId + REJECT_SUFFIX;
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
			String deleteUrl = url.substring(0, url.indexOf("/aets/")) + REJECT_SUFFIX;
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
			LOG.error("ClientProtocolException during delete from pacs: " + url, e);
			throw e;
		} catch (IOException e) {
			LOG.error("IOException during upload into pacs: " + url, e);
			throw e;
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
