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

import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
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
	private static final String CONTENT_TYPE = "application/dicom";
	private static final String BOUNDARY = "--import_dicom_shanoir--";
	
	@Value("${dcm4chee-arc.protocol}")
	private String dcm4cheeProtocol;
	
	@Value("${dcm4chee-arc.host}")
	private String dcm4cheeHost;

	@Value("${dcm4chee-arc.port.web}")
	private String dcm4cheePortWeb;
	
	@Value("${dcm4chee-arc.dicom.web.rs}")
	private String dicomWebRS;
	
	@Override
	public void sendDicomFilesToPacs(File directoryWithDicomFiles) throws ClientProtocolException, IOException, ShanoirException {
		if (directoryWithDicomFiles != null && directoryWithDicomFiles.exists() && directoryWithDicomFiles.isDirectory()) {
			File[] dicomFiles = directoryWithDicomFiles.listFiles();
			LOG.info("Start: STOW-RS sending " + dicomFiles.length + " dicom files to PACS from folder: " + directoryWithDicomFiles.getAbsolutePath());
			CloseableHttpClient httpClient = HttpClients.createDefault();
			try {
				MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create().setBoundary(BOUNDARY);
				for (File dicomFile : dicomFiles) {
					multipartEntityBuilder.addBinaryBody("dcm_upload", dicomFile, ContentType.create(CONTENT_TYPE), "filename");
				}
				HttpEntity entity = multipartEntityBuilder.build();
				HttpPost httpPost = new HttpPost(dcm4cheeProtocol + dcm4cheeHost + ":" + dcm4cheePortWeb + dicomWebRS);
				httpPost.setHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_MULTIPART+";type="+CONTENT_TYPE+";boundary="+BOUNDARY);
				httpPost.setEntity(entity);
				CloseableHttpResponse response = httpClient.execute(httpPost);
				response.getEntity();
				response.close();
			} finally {
				httpClient.close();
			}
			LOG.info("Finished: STOW-RS sending " + dicomFiles.length + " dicom files to PACS from folder: " + directoryWithDicomFiles.getAbsolutePath());
		} else {
			throw new ShanoirException("sendDicomFilesToPacs called with null, or file: not existing or not a directory.");
		}
	}

}
