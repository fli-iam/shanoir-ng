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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 * WADO-RS URLs are supported: http://dicom.nema.org/DICOM/2013/output/chtml/part18/sect_6.5.html
 * WADO-URI URLs are supported: http://dicom.nema.org/DICOM/2013/output/chtml/part18/sect_6.2.html
 * 
 * WADO-RS: http://dcm4chee-arc:8081/dcm4chee-arc/aets/DCM4CHEE/rs/studies/1.4.9.12.22.1.8447.5189520782175635475761938816300281982444
 * /series/1.4.9.12.22.1.3337.609981376830290333333439326036686033499
 * /instances/1.4.9.12.22.1.3327.13131999371192661094333587030092502791578
 * 
 * As the responses are encoded as multipart/related messages,
 * this class extracts as well the files contained in the response to
 * the file system.
 * 
 * WADO-URI: http://dcm4chee-arc:8081/dcm4chee-arc/aets/DCM4CHEE/wado?requestType=WADO
 * &studyUID=1.4.9.12.22.1.8444.518952078217568647576155668816300281982444
 * &seriesUID=1.4.9.12.22.1.8444.60998137683029030014444439326036686033499
 * &objectUID=1.4.9.12.22.1.8444.1313199937119266109555587030092502791578
 * &contentType=application/dicom
 * 
 * WADO-URI Web Service Endpoint URL in dcm4chee arc light 5:
 * http[s]://<host>:<port>/dcm4chee-arc/aets/{AETitle}/wado
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

	/** Logger. */
	private static final Logger LOG = LoggerFactory.getLogger(WADODownloaderService.class);

	private static final String WADO_REQUEST_TYPE_WADO_RS = "/instances/";

	private static final String WADO_REQUEST_TYPE_WADO_URI = "objectUID=";

	private static final String DCM = ".dcm";

	private static final String UNDER_SCORE = "_";
	
	/** Mime type */
	private static final String CONTENT_TYPE_MULTIPART = "multipart/related";
	
	private static final String CONTENT_TYPE_DICOM = "application/dicom";

	private static final String CONTENT_TYPE = "&contentType";
	
	@Autowired
	private RestTemplate restTemplate;
	
	@PostConstruct
	public void initRestTemplate() {
		restTemplate.getMessageConverters().add(new ByteArrayHttpMessageConverter());
	}

	/**
	 * This method receives a list of URLs containing WADO-RS or WADO-URI urls and downloads
	 * their received dicom files to a folder named workFolder.
	 * 
	 * @param urls
	 * @param workFolder
	 * @param subjectName
	 * @throws IOException
	 * @throws MessagingException
	 */
	public void downloadDicomFilesForURLs(final List<URL> urls, final File workFolder, String subjectName) throws IOException, MessagingException {
		for (Iterator iterator = urls.iterator(); iterator.hasNext();) {
			String url = ((URL) iterator.next()).toString();
			String instanceUID = null;
			// handle and check at first for WADO-RS URLs by "/instances/"
			int indexInstanceUID = url.lastIndexOf(WADO_REQUEST_TYPE_WADO_RS);
			if (indexInstanceUID > 0) {
				instanceUID = url.substring(indexInstanceUID + WADO_REQUEST_TYPE_WADO_RS.length());
				byte[] responseBody = downloadFileFromPACS(url);
				extractDICOMFilesFromMHTMLFile(responseBody, instanceUID, workFolder);
			} else {
				// handle and check secondly for WADO-URI URLs by "objectUID="
				// instanceUID == objectUID
				indexInstanceUID = url.lastIndexOf(WADO_REQUEST_TYPE_WADO_URI);
				if (indexInstanceUID > 0) {
					instanceUID = extractInstanceUID(url, instanceUID);
					byte[] responseBody = downloadFileFromPACS(url);
					String name = subjectName + "_" + instanceUID;
					File extractedDicomFile = new File(workFolder.getPath() + File.separator + name + DCM);
					ByteArrayInputStream bIS = null;
					try {
						bIS = new ByteArrayInputStream(responseBody);
						Files.copy(bIS, extractedDicomFile.toPath());
					} finally {
						if (bIS != null) {
							bIS.close();
						}
					}
				} else {
					throw new IOException("URL for download is neither in WADO-RS nor in WADO-URI format. Please verify database contents.");
				}
			}
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
		Pattern p = null;
		if (url.indexOf(CONTENT_TYPE) != -1) {
			p = Pattern.compile("objectUID=(\\S+)&contentType");
		} else {
			p = Pattern.compile("objectUID=(\\S+)");
		}
		Matcher m = p.matcher(url);
		if (m.find()) {
			instanceUID = m.group(1);
		}
		return instanceUID;
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
		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.ACCEPT, CONTENT_TYPE_MULTIPART + "; type=" + CONTENT_TYPE_DICOM + ";");
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
				if (bodyPart.isMimeType(CONTENT_TYPE_DICOM)) {
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
