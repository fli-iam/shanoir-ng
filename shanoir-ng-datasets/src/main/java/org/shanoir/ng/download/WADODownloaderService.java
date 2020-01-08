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

package org.shanoir.ng.download;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.Iterator;
import java.util.List;

import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * This class is used to download files on using WADO URLs:
 * 
 * First version: WADO RS URLs are supported: http://dicom.nema.org/DICOM/2013/output/chtml/part18/sect_6.5.html
 * Next version: WADO URI URLs will be supported: http://dicom.nema.org/DICOM/2013/output/chtml/part18/sect_6.2.html
 * 
 * WADO-RS: as the responses are encoded as multipart/related messages,
 * this class extracts as well the files contained in the response to
 * the file system.
 *
 * This Spring service component uses the scope singleton, that is there by default,
 * as one instance should be reused for all other instances, that require usage.
 * No need to create multiple.
 * 
 * @author mkain
 *
 */
@Service
public class WADODownloaderService {

	private static final String INSTANCES = "/instances/";

	private static final String DCM = ".dcm";

	private static final String UNDER_SCORE = "_";
	
	/** Mime type */
	private static final String CONTENT_TYPE_MULTIPART = "multipart/related";
	
	private static final String CONTENT_TYPE = "application/dicom";
	
	@Autowired
	private RestTemplate restTemplate;
	
	/**
	 * This method receives a list of URLs containing WADO-RS urls and downloads
	 * their received dicom files to a folder named workFolder.
	 * @param urls
	 * @param workFolder
	 * @throws IOException
	 * @throws MessagingException
	 */
	public void downloadDicomFilesForURLs(final List<URL> urls, final File workFolder) throws IOException, MessagingException {
		for (Iterator<URL> iterator = urls.iterator(); iterator.hasNext();) {
			String url = iterator.next().toString();
			int indexInstanceUID = url.lastIndexOf(INSTANCES);
			if (indexInstanceUID < 0) {
				throw new IOException("URL is not in WADO-RS instance URL format. WADO-URI is not yet implemented.");
			}
			String instanceUID = url.substring(indexInstanceUID + INSTANCES.length());
			byte[] responseBody = downloadFileFromPACS(url);
			extractDICOMFilesFromMHTMLFile(responseBody, instanceUID, workFolder);
		}
	}

	/**
	 * This method contacts the PACS with a WADO-RS url and does the actual download.
	 * 
	 * @param url
	 * @param targetFile
	 * @return
	 * @throws IOException
	 */
	private byte[] downloadFileFromPACS(final String url) throws IOException {
		restTemplate.getMessageConverters().add(new ByteArrayHttpMessageConverter());
		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.ACCEPT, CONTENT_TYPE_MULTIPART + "; type=" + CONTENT_TYPE + ";");
		HttpEntity<String> entity = new HttpEntity<>(headers);
		ResponseEntity<byte[]> response = restTemplate.exchange(url,
				HttpMethod.GET, entity, byte[].class, "1");
		if (response.getStatusCode() == HttpStatus.OK) {
			return response.getBody();
		} else {
			throw new IOException("Download did not work: wrong status code received.");
		}
	}

	/**
	 * This method reads in a file in format MHTML, one representation of a multipart/related response, that is given from
	 * a PACS server, that supports WADO-RS requests.
	 * 
	 * MHTML, short for MIME Encapsulation of Aggregate HTML Documents, is a web page archive format used to combine in a single document
	 * the HTML code and its companion resources that are otherwise represented by external links (such as images, Flash animations, Java applets,
	 * and audio files). The content of an MHTML file is encoded as if it were an HTML e-mail message, using the MIME type multipart/related.
	 * 
	 * @param responseBody
	 * @param instanceUID
	 * @param workFolder
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws MessagingException
	 */
	private void extractDICOMFilesFromMHTMLFile(final byte[] responseBody, final String instanceUID, final File workFolder)
			throws IOException, MessagingException {
		try(ByteArrayInputStream bIS = new ByteArrayInputStream(responseBody)) {
			ByteArrayDataSource datasource = new ByteArrayDataSource(bIS, CONTENT_TYPE_MULTIPART);
			MimeMultipart multipart = new MimeMultipart(datasource);
			int count = multipart.getCount();
			for (int i = 0; i < count; i++) {
				BodyPart bodyPart = multipart.getBodyPart(i);
				if (bodyPart.isMimeType(CONTENT_TYPE)) {
					File extractedDicomFile = null;
					if (count == 1) {
						extractedDicomFile = new File(workFolder.getPath() + File.separator + instanceUID + DCM);
					} else {
						extractedDicomFile = new File(workFolder.getPath() + File.separator + instanceUID + UNDER_SCORE + count + DCM);
					}
					Files.copy(bodyPart.getInputStream(), extractedDicomFile.toPath());
				} else {
					throw new IOException("Answer file from PACS contains other content-type than DICOM, stop here.");
				}
			}
		}
	}

}
