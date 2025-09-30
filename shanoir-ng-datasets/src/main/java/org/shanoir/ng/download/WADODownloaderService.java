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
import java.io.StringReader;
import java.net.URL;
import java.nio.file.Files;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.json.JSONReader;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.dataset.model.DatasetExpressionFormat;
import org.shanoir.ng.dataset.service.DatasetUtils;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.dicom.WADOURLHandler;
import org.shanoir.ng.shared.exception.PacsException;
import org.shanoir.ng.shared.exception.RestServiceException;
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
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.PostConstruct;
import jakarta.json.Json;
import jakarta.json.stream.JsonParser;
import jakarta.mail.BodyPart;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.util.ByteArrayDataSource;

/**
 * This class is used to download files on using WADO URLs:
 *
 * WADO-RS URLs are supported: http://dicom.nema.org/DICOM/2013/output/chtml/part18/sect_6.5.html
 * WADO-URI URLs are supported: http://dicom.nema.org/DICOM/2013/output/chtml/part18/sect_6.2.html
 *
 * WADO-RS: http://dcm4chee-arc:8081/dcm4chee-arc/aets/AS_RECEIVED/rs/studies/1.4.9.12.22.1.8447.5189520782175635475761938816300281982444
 * /series/1.4.9.12.22.1.3337.609981376830290333333439326036686033499
 * /instances/1.4.9.12.22.1.3327.13131999371192661094333587030092502791578
 *
 * As the responses are encoded as multipart/related messages,
 * this class extracts as well the files contained in the response to
 * the file system.
 *
 * WADO-URI: http://dcm4chee-arc:8081/dcm4chee-arc/aets/AS_RECEIVED/wado?requestType=WADO
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

	private static final Logger LOG = LoggerFactory.getLogger(WADODownloaderService.class);

	private static final String WADO_REQUEST_TYPE_WADO_RS = "/instances/";

	private static final String WADO_REQUEST_TYPE_WADO_URI = "objectUID=";

	private static final String WADO_REQUEST_STUDY_WADO_URI = "studyUID=";

	private static final String DCM = ".dcm";

	private static final String UNDER_SCORE = "_";

	/** Mime type */
	private static final String CONTENT_TYPE_MULTIPART = "multipart/related";

	private static final String CONTENT_TYPE_DICOM = "application/dicom";

	private static final String CONTENT_TYPE_DICOM_XML = "application/dicom+xml";

	private static final String CONTENT_TYPE_DICOM_JSON = "application/json";

	private static final String CONTENT_TYPE = "&contentType";

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private WADOURLHandler wadoURLHandler;

	@PostConstruct
	public void initRestTemplate() {
		restTemplate.getMessageConverters().add(new ByteArrayHttpMessageConverter());
	}

	/**
	 * This method receives a list of URLs containing WADO-RS or WADO-URI urls and downloads
	 * their received dicom files to a folder named workFolder.
	 * Return the list of downloaded files
	 *
	 * @param urls
	 * @param subjectName
	 * @param dataset
	 * @param datasetFilePath
	 * @throws IOException
	 * @throws MessagingException
	 * @return
	 * @throws RestServiceException
	 *
	 */
	public List<String> downloadDicomFilesForURLsAsZip(final List<URL> urls, final ZipOutputStream zipOutputStream, String subjectName, Dataset dataset, String datasetFilePath, DatasetDownloadError downloadResult) {
		int i = 0;
		List<String> files = new ArrayList<>();
		Set<String> zippedUrls = new HashSet<>();
		long duplicates = 0;
		for (Iterator<URL> iterator = urls.iterator(); iterator.hasNext(); i++) {
			String url = iterator.next().toString();
			if (!zippedUrls.contains(url)) {
				zippedUrls.add(url);
				String sopInstanceUID = wadoURLHandler.extractUIDs(url)[2];
				// Build name
				String name = buildFileName(subjectName, dataset, datasetFilePath, sopInstanceUID);
				// Download and zip
				try {
					String zipedFile = downloadAndWriteFileInZip(url, zipOutputStream, name);
					if (zipedFile != null) {
						files.add(zipedFile);
					}
				} catch (ZipPacsFileException e) {
					LOG.error("Could not download dataset [{}] as dicom", dataset.getId(), e);
					downloadResult.update("Could not download dataset [" + dataset.getId() + "] as dicom : " + e.getMessage(), DatasetDownloadError.PARTIAL_FAILURE);
				}
			} else {
				duplicates++;
			}
		}
		if (duplicates > 0) {
			LOG.error("There were [" + duplicates + "] duplicate dataset_files when zipping dataset [" + dataset.getId() + "], they were ignored.");
		}
		return files;
	}

	private String buildFileName(String subjectName, Dataset dataset, String datasetFilePath, String instanceUID) {
		String serieDescription = dataset.getUpdatedMetadata().getName();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("YYYYMMdd");
		String examDate = dataset.getDatasetAcquisition().getExamination().getExaminationDate().format(formatter);
		String name = subjectName + "_" + examDate + "_" + serieDescription + "_" + instanceUID;
		// Replace all forbidden characters.
		name = name.replaceAll("[^a-zA-Z0-9\\.\\-]", "_");
		// add folder logic if necessary
		if (datasetFilePath != null) {
			name = datasetFilePath + File.separator + name;
		}
		return name;
	}

	/**
	 * Downloads and writes the file specified by url into zipOutputStream, using name + .DCM as filename.
	 * If the downloading fails, a text file is added instead and null is returned.
	 * @param url
	 * @param zipOutputStream
	 * @param name the filename without extension
	 * @return the added file name, null if failed
	 * @throws ZipPacsFileException
	 * @throws IOException when couldn't write into the stream
	 */
	private String downloadAndWriteFileInZip(String url, ZipOutputStream zipOutputStream, String name) throws ZipPacsFileException {
		byte[] responseBody = null;
		try {
			responseBody = downloadFileFromPACS(url);
			this.extractDICOMZipFromMHTMLFile(responseBody,  name, zipOutputStream, url.contains(WADO_REQUEST_TYPE_WADO_RS));
			return name + DCM;
		} catch (IOException | MessagingException e) {
			LOG.error("Error in downloading/writing file [{}] from pacs to zip", name, e);
			throw new ZipPacsFileException(e);
		} catch (HttpClientErrorException e) {
			throw new ZipPacsFileException("Received " + e.getStatusCode() + " from PACS", e);
		}
	}

	/**
	 * This method receives a list of URLs containing WADO-RS or WADO-URI urls and downloads
	 * their received dicom files to a folder named workFolder.
	 * Return the list of downloaded files
	 *
	 * @param urls
	 * @param workFolder
	 * @param subjectName
	 * @param dataset
	 * @throws IOException
	 * @throws MessagingException
	 * @return
	 *
	 */
	public List<File> downloadDicomFilesForURLs(final List<URL> urls, final File workFolder, String subjectName, Dataset dataset, DatasetDownloadError downloadResult) {
		List<File> files = new ArrayList<>();
		for (Iterator<URL> iterator = urls.iterator(); iterator.hasNext();) {
			try {
				String url = ((URL) iterator.next()).toString();
				String sopInstanceUID = null;
				// handle and check at first for WADO-RS URLs by "/instances/"
				int indexInstanceUID = url.lastIndexOf(WADO_REQUEST_TYPE_WADO_RS);
				if (indexInstanceUID > 0) {
					sopInstanceUID = url.substring(indexInstanceUID + WADO_REQUEST_TYPE_WADO_RS.length());
					byte[] responseBody = downloadFileFromPACS(url);
					extractDICOMFilesFromMHTMLFile(responseBody, sopInstanceUID, workFolder);
				} else {
					// handle and check secondly for WADO-URI URLs by "objectUID="
					// instanceUID == objectUID
					indexInstanceUID = url.lastIndexOf(WADO_REQUEST_TYPE_WADO_URI);
					if (indexInstanceUID > 0) {
						sopInstanceUID = wadoURLHandler.extractUIDs(url)[2];

						String serieDescription = dataset.getUpdatedMetadata().getName();
						DateTimeFormatter formatter = DateTimeFormatter.ofPattern("YYYYMMdd");
						String examDate = dataset.getDatasetAcquisition().getExamination().getExaminationDate().format(formatter);
						String name = subjectName + "_" + examDate + "_" + serieDescription + "_" + sopInstanceUID;

						// Replace all forbidden characters.
						name = name.replaceAll("[^a-zA-Z0-9\\.\\-]", "_");

						File extractedDicomFile = new File(workFolder.getPath() + File.separator + name + DCM);

						byte[] responseBody = downloadFileFromPACS(url);
						try (ByteArrayInputStream bIS = new ByteArrayInputStream(responseBody)) {
							Files.copy(bIS, extractedDicomFile.toPath());
							files.add(extractedDicomFile);
						}
					} else {
						downloadResult.update("URL for download of dataset [" + dataset.getId() + "] is neither in WADO-RS nor in WADO-URI format", DatasetDownloadError.PARTIAL_FAILURE);
					}
				}
			} catch (Exception e) {
				LOG.error("A dicom file of dataset [{}] could not be downloaded from the pacs", dataset.getId(), e);
				downloadResult.update("A dicom file of [" + dataset.getId() + "] could not be downloaded from the pacs :" + e.getMessage(), DatasetDownloadError.PARTIAL_FAILURE);
			}
		}
		return files;
	}

	public String downloadDicomMetadataForURL(final URL url) throws IOException, MessagingException, RestClientException {
		if (url != null) {
			String urlStr = url.toString();
			if (urlStr.contains(WADO_REQUEST_STUDY_WADO_URI)) urlStr = wadoURLHandler.convertWadoUriToWadoRs(urlStr);
			urlStr = urlStr.split(CONTENT_TYPE)[0].concat("/metadata/");
			return downloadMetadataFromPACS(urlStr);
		} else {
			return null;
		}
	}

	public Attributes getDicomAttributesForDataset(Dataset dataset) throws PacsException {
		List<URL> urls = new ArrayList<>();
		try {
			DatasetUtils.getDatasetFilePathURLs(dataset, urls, DatasetExpressionFormat.DICOM);
			if (!urls.isEmpty()) {
				String jsonMetadataStr = downloadDicomMetadataForURL(urls.get(0));
				JsonParser parser = Json.createParser(new StringReader(jsonMetadataStr));
				Attributes dicomAttributes = new JSONReader(parser).readDataset(null);
				if (dicomAttributes != null) {
					return dicomAttributes;
				} else {
					LOG.error("Could not find dicom attributes for dataset [{}]", dataset.getId());
				}
			} else {
				LOG.error("Could not find dicom attributes for dataset [{}] : no pacs url for this dataset", dataset.getId());
			}
		} catch (IOException | MessagingException | RestClientException e) {
			throw new PacsException("Can not get dicom attributes for dataset [" + dataset.getId() + "]", e);
		}
		return null;
	}

	public AcquisitionAttributes<Long> getDicomAttributesForAcquisition(DatasetAcquisition acquisition) throws PacsException {
		long ts = new Date().getTime();
		List<Dataset> datasets = new ArrayList<>();
		if (acquisition.getDatasets() != null) {
			for (Dataset dataset : acquisition.getDatasets()) {
				datasets.add(dataset);
			}
		}
		AcquisitionAttributes<Long> dAcquisitionAttributes = new AcquisitionAttributes<>();
		// remove this ?
		datasets.forEach(dataset -> {
			try {
				dAcquisitionAttributes.addDatasetAttributes(dataset.getId(), getDicomAttributesForDataset(dataset));
			} catch (PacsException e) {
				throw new RuntimeException("Could not get dataset [" + dataset.getId() + "] dicom attributes from pacs", e);
			}
		});
		LOG.debug("get DICOM attributes for acquisition [" + acquisition.getId() + "] : " + (new Date().getTime() - ts) + " ms");
		return dAcquisitionAttributes;
	}

	public WADOURLHandler getWadoURLHandler() {
		return wadoURLHandler;
	}

	public void setWadoURLHandler(WADOURLHandler wadoURLHandler) {
		this.wadoURLHandler = wadoURLHandler;
	}

	/**
	 * This method contacts the PACS with a WADO-RS url and does the actual download.
	 *
	 * @param url
	 * @return
	 * @throws IOException
	 */
	private byte[] downloadFileFromPACS(final String url) throws IOException, HttpClientErrorException {
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

	private String downloadMetadataFromPACS(final String url) throws IOException {
		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.ACCEPT, CONTENT_TYPE_DICOM_JSON);
		HttpEntity<String> entity = new HttpEntity<>(headers);
		LOG.debug("Download metadata from pacs, url : " + url);
		ResponseEntity<String> response = restTemplate.exchange(url,
				HttpMethod.GET, entity, String.class, "1");
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
		try (ByteArrayInputStream bIS = new ByteArrayInputStream(responseBody)) {
			ByteArrayDataSource datasource = new ByteArrayDataSource(bIS, CONTENT_TYPE_MULTIPART);
			MimeMultipart multipart = new MimeMultipart(datasource);
			int count = multipart.getCount();
			for (int i = 0; i < count; i++) {
				BodyPart bodyPart = multipart.getBodyPart(i);
				if (isNotOnlyDicom(bodyPart)) {
					throw new IOException("Answer file from PACS contains other content-type than DICOM, stop here.");
				}
				File extractedDicomFile = null;
				if (count == 1) {
					extractedDicomFile = new File(workFolder.getPath() + File.separator + instanceUID + DCM);
				} else {
					extractedDicomFile = new File(workFolder.getPath() + File.separator + instanceUID + UNDER_SCORE + i + DCM);
				}
				Files.copy(bodyPart.getInputStream(), extractedDicomFile.toPath());
			}
		}
	}

	private boolean isNotOnlyDicom(BodyPart bodyPart) throws MessagingException {
		return !bodyPart.isMimeType(CONTENT_TYPE_DICOM) && !bodyPart.isMimeType(CONTENT_TYPE_DICOM_XML);
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
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws MessagingException
	 */
	private void extractDICOMZipFromMHTMLFile(final byte[] responseBody, String name, ZipOutputStream zipOutputStream, boolean isMultipart)
			throws IOException, MessagingException {
		try (ByteArrayInputStream bIS = new ByteArrayInputStream(responseBody)) {
			// Not multipart
			if (!isMultipart) {
				ZipEntry entry = new ZipEntry(name + DCM);
				zipOutputStream.putNextEntry(entry);
				bIS.transferTo(zipOutputStream);
				zipOutputStream.closeEntry();
				return;
			}
			ByteArrayDataSource datasource = new ByteArrayDataSource(bIS, CONTENT_TYPE_MULTIPART);
			MimeMultipart multipart = new MimeMultipart(datasource);
			int count = multipart.getCount();
			// Multipart but with a single body part
			if (count == 1) {
				BodyPart bodyPart = multipart.getBodyPart(0);
				if (isNotOnlyDicom(bodyPart)) {
					throw new IOException("Answer file from PACS contains other content-type than DICOM, stop here.");
				}
				ZipEntry entry = new ZipEntry(name + DCM);
				zipOutputStream.putNextEntry(entry);
				bodyPart.getInputStream().transferTo(zipOutputStream);
				zipOutputStream.closeEntry();
				return;
			}
			// Multipart with multiple parts
			for (int i = 0; i < count; i++) {
				BodyPart bodyPart = multipart.getBodyPart(i);
				if (isNotOnlyDicom(bodyPart)) {
					throw new IOException("Answer file from PACS contains other content-type than DICOM, stop here.");
				}
				ZipEntry entry = new ZipEntry(name + UNDER_SCORE + i + DCM);
				zipOutputStream.putNextEntry(entry);
				bodyPart.getInputStream().transferTo(zipOutputStream);
				zipOutputStream.closeEntry();
			}
		}
	}

}
