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
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.URL;
import java.nio.file.Files;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.io.DicomOutputStream;
import org.dcm4che3.json.JSONReader;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.dataset.model.DatasetExpressionFormat;
import org.shanoir.ng.dataset.service.DatasetService;
import org.shanoir.ng.dataset.service.DatasetUtils;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.dicom.WADOURLHandler;
import org.shanoir.ng.shared.exception.PacsException;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import io.netty.handler.timeout.ReadTimeoutHandler;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import jakarta.annotation.PostConstruct;
import jakarta.json.Json;
import jakarta.json.stream.JsonParser;
import jakarta.mail.BodyPart;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.util.ByteArrayDataSource;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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

    /** Number of PACS responses fetched in advance, while the current one is written into the zip. */
    @Value("${dcm4chee-arc.dicom.wado.prefetch:10}")
    private int wadoPrefetch;

    @Autowired
    private WebClient webClient;

    @Autowired
    private WADOURLHandler wadoURLHandler;

    @Autowired
    @Lazy
    private DatasetService datasetService;

    /** One PACS response, or the error that replaced it, tied to the URL it was requested for. */
    private record PacsResponse(String url, byte[] body, Throwable error) { }

    @PostConstruct
    public void initWebClient() {
        ConnectionProvider provider = ConnectionProvider.builder("pacs-wado")
                .maxConnections(500)
                .maxIdleTime(Duration.ofSeconds(15))
                .maxLifeTime(Duration.ofMinutes(5))
                .evictInBackground(Duration.ofSeconds(30))
                .build();

        HttpClient httpClient = HttpClient.create(provider)
                .responseTimeout(Duration.ofSeconds(30))
                .doOnConnected(conn -> conn.addHandlerLast(
                        new ReadTimeoutHandler(Duration.ofSeconds(30).toSeconds(), TimeUnit.SECONDS)));

        this.webClient = WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(1024 * 1024 * 500))
                .build();
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
        List<String> files = new ArrayList<>();
        String namePrefix = buildFileNamePrefix(subjectName, dataset, datasetFilePath);
        String anonymizedSubjectName = dataset.getSource() != null ? subjectName : null;

        List<String> urlsToDownload = urls.stream().map(URL::toString).distinct().toList();

        PacsTransferStats stats = PacsTransferStats.current();

        // Flux allows to download asynchronously (up to 4,w hich is the first iteration of the wadoPrefetch)
        try (Stream<PacsResponse> responses = Flux.fromIterable(urlsToDownload)
                .flatMap(url -> {
                    long startNanos = System.nanoTime();
                    return downloadFileFromPACSAsync(url)
                            .map(body -> new PacsResponse(url, body, null))
                            .onErrorResume(e -> Mono.just(new PacsResponse(url, null, e)))
                            .doOnNext(r -> {
                                if (stats != null) {
                                    stats.record(r.body() != null ? r.body().length : 0, System.nanoTime() - startNanos);
                                }
                            });
                },
                        wadoPrefetch)
                .toStream(wadoPrefetch)) {
            // Then we put each file one by one in the zip
            Iterator<PacsResponse> iterator = responses.iterator();
            while (iterator.hasNext()) {
                PacsResponse response = iterator.next();
                String name = namePrefix + sanitize(wadoURLHandler.extractUIDs(response.url())[2]);
                try {
                    files.add(writeFileInZip(response, zipOutputStream, name, anonymizedSubjectName));
                    zipOutputStream.flush();
                } catch (IOException e) {
                    LOG.error("Could not flush dataset [{}] to the client", dataset.getId(), e);
                    downloadResult.update("Could not flush dataset [" + dataset.getId() + "] to the client : " + e.getMessage(), DatasetDownloadError.PARTIAL_FAILURE);
                } catch (ZipPacsFileException e) {
                    LOG.error("Could not download dataset [{}] as dicom", dataset.getId(), e);
                    downloadResult.update("Could not download dataset [" + dataset.getId() + "] as dicom : " + e.getMessage(), DatasetDownloadError.PARTIAL_FAILURE);
                }
            }
        }
        return files;
    }

    private String buildFileNamePrefix(String subjectName, Dataset dataset, String datasetFilePath) {
        String serieDescription = dataset.getUpdatedMetadata().getName();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("YYYYMMdd");
        String examDate = datasetService.getFirstRealInput(dataset)
                .getDatasetAcquisition().getExamination().getExaminationDate().format(formatter);
        String prefix = sanitize(subjectName + "_" + examDate + "_" + serieDescription + "_");
        // add folder logic if necessary
        return datasetFilePath != null ? datasetFilePath + File.separator + prefix : prefix;
    }

    private String sanitize(String name) {
        return name.replaceAll("[^a-zA-Z0-9\\.\\-]", "_");
    }

    /**
     * Writes the PACS response for one file into zipOutputStream, using name + .DCM as filename.
     * @param response the PACS response, or the error that replaced it
     * @param zipOutputStream
     * @param name the filename without extension
     * @return the added file name
     * @throws ZipPacsFileException when the download failed or could not be written into the stream
     */
    private String writeFileInZip(PacsResponse response, ZipOutputStream zipOutputStream, String name, String subjectName) throws ZipPacsFileException {
        if (response.error() != null) {
            if (response.error() instanceof WebClientResponseException e) {
                throw new ZipPacsFileException("Received " + e.getStatusCode() + " from PACS", e);
            }
            throw new ZipPacsFileException("Download failed: " + response.error().getMessage(), response.error());
        }
        try {
            extractDICOMZipFromMHTMLFile(response.body(), name, zipOutputStream, response.url().contains(WADO_REQUEST_TYPE_WADO_RS), subjectName);
            return name + DCM;
        } catch (IOException | MessagingException e) {
            LOG.error("Error in downloading/writing file [{}] from pacs to zip", name, e);
            throw new ZipPacsFileException(e);
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
                    if (dataset.getSource() != null) {
                        extractDICOMFilesFromMHTMLFile(responseBody, sopInstanceUID, workFolder, subjectName);
                    } else {
                        extractDICOMFilesFromMHTMLFile(responseBody, sopInstanceUID, workFolder, null);
                    }
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
            throw new PacsException("Could not get dataset [" + dataset.getId() + "] dicom attributes from pacs", e);
        }
        return null;
    }

    @Transactional(readOnly = true)
    public AcquisitionAttributes<Long> getDicomAttributesForAcquisition(DatasetAcquisition acquisition) throws PacsException {
        long ts = new Date().getTime();
        List<Dataset> datasets = new ArrayList<>();
        if (acquisition.getDatasets() != null) {
            for (Dataset dataset : acquisition.getDatasets()) {
                datasets.add(dataset);
            }
        }
        AcquisitionAttributes<Long> dAcquisitionAttributes = new AcquisitionAttributes<>();
        for (Dataset dataset : datasets) {
            dAcquisitionAttributes.addDatasetAttributes(dataset.getId(), getDicomAttributesForDataset(dataset));
        }
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
     * This method contacts the PACS with a WADO-RS url and does the actual
     * download. Uses async WebClient internally but maintains synchronous
     * method signature.
     *
     * @param url
     * @return
     * @throws IOException
     */
    private byte[] downloadFileFromPACS(final String url) throws IOException, HttpClientErrorException {
        try {
            return downloadFileFromPACSAsync(url).block();
        } catch (WebClientResponseException e) {
            throw new HttpClientErrorException(e.getStatusCode(),
                    "Download failed: " + e.getMessage());
        } catch (Exception e) {
            throw new IOException("Download failed: " + e.getMessage(), e);
        }
    }

    private String downloadMetadataFromPACS(final String url) throws IOException {
        try {
            return downloadMetadataFromPACSAsync(url)
                    .block(); // Block at the end to convert Mono to sync result
        } catch (WebClientResponseException e) {
            throw new IOException("Download failed: " + e.getStatusCode() + " - " + e.getMessage(), e);
        } catch (Exception e) {
            throw new IOException("Download failed: " + e.getMessage(), e);
        }
    }

    // Internal async methods for potential reuse
    private Mono<byte[]> downloadFileFromPACSAsync(final String url) {
        return webClient.get()
                .uri(url)
                .header(HttpHeaders.ACCEPT, CONTENT_TYPE_MULTIPART + "; type=" + CONTENT_TYPE_DICOM + ";")
                .retrieve()
                .onStatus(HttpStatusCode::isError,
                        response -> Mono.error(new IOException("Download did not work: wrong status code received.")))
                .bodyToMono(byte[].class)
                .timeout(Duration.ofMinutes(5));
    }

    private Mono<String> downloadMetadataFromPACSAsync(final String url) {
        return webClient.get()
                .uri(url)
                .header(HttpHeaders.ACCEPT, CONTENT_TYPE_DICOM_JSON)
                .retrieve()
                .onStatus(HttpStatusCode::isError,
                        response -> Mono.error(new IOException("Download did not work: wrong status code received.")))
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(30));
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
    private void extractDICOMFilesFromMHTMLFile(final byte[] responseBody, final String instanceUID, final File workFolder, String subjectName)
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
                if (subjectName != null && !subjectName.trim().isEmpty()) {
                    modifyAndSaveDicomFile(bodyPart.getInputStream(), extractedDicomFile, subjectName);
                } else {
                    Files.copy(bodyPart.getInputStream(), extractedDicomFile.toPath());
                }
            }
        }
    }

    private void modifyAndSaveDicomFile(InputStream inputStream, File outputFile, String subjectName)
            throws IOException {
        try (DicomInputStream dis = new DicomInputStream(inputStream)) {
            Attributes attributes = dis.readDataset();
            attributes.setString(Tag.PatientName, VR.PN, subjectName);
            attributes.setString(Tag.PatientID, VR.LO, subjectName);
            try (DicomOutputStream dos = new DicomOutputStream(outputFile)) {
                dos.writeDataset(dis.getFileMetaInformation(), attributes);
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
    private void extractDICOMZipFromMHTMLFile(final byte[] responseBody, String name, ZipOutputStream zipOutputStream,
            boolean isMultipart, String subjectName)
            throws IOException, MessagingException {
        try (ByteArrayInputStream bIS = new ByteArrayInputStream(responseBody)) {
            // Not multipart
            if (!isMultipart) {
                ZipEntry entry = new ZipEntry(name + DCM);
                zipOutputStream.putNextEntry(entry);
                if (subjectName != null && !subjectName.trim().isEmpty()) {
                    modifyAndWriteDicomToStream(bIS, zipOutputStream, subjectName);
                } else {
                    bIS.transferTo(zipOutputStream);
                }
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
                addEntry(zipOutputStream, subjectName, bodyPart, entry);
                return;
            }
            // Multipart with multiple parts
            for (int i = 0; i < count; i++) {
                BodyPart bodyPart = multipart.getBodyPart(i);
                if (isNotOnlyDicom(bodyPart)) {
                    throw new IOException("Answer file from PACS contains other content-type than DICOM, stop here.");
                }
                ZipEntry entry = new ZipEntry(name + UNDER_SCORE + i + DCM);
                addEntry(zipOutputStream, subjectName, bodyPart, entry);
            }
        }
    }

    private void addEntry(ZipOutputStream zipOutputStream, String subjectName, BodyPart bodyPart, ZipEntry entry)
            throws IOException, MessagingException {
        zipOutputStream.putNextEntry(entry);
        if (subjectName != null && !subjectName.trim().isEmpty()) {
            modifyAndWriteDicomToStream(bodyPart.getInputStream(), zipOutputStream, subjectName);
        } else {
            bodyPart.getInputStream().transferTo(zipOutputStream);
        }
        zipOutputStream.closeEntry();
    }

    private void modifyAndWriteDicomToStream(InputStream inputStream, OutputStream outputStream, String subjectName)
            throws IOException {
        try (DicomInputStream dis = new DicomInputStream(inputStream)) {
            Attributes attributes = dis.readDataset();
            attributes.setString(Tag.PatientName, VR.PN, subjectName);
            attributes.setString(Tag.PatientID, VR.LO, subjectName);
            @SuppressWarnings("resource")
            DicomOutputStream dos = new DicomOutputStream(outputStream, "1.2.840.10008.1.2.1");
            dos.writeDataset(dis.getFileMetaInformation(), attributes);
            dos.flush();
        }
    }

}
