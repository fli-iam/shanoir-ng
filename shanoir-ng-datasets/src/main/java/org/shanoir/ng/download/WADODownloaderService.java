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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
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
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import io.netty.channel.ChannelOption;
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

    /** How many response buffers may sit queued between netty and the writing thread. */
    private static final int STREAM_PREFETCH = 4;

    /** Retries for a connection the PACS closed before sending any of the body. */
    private static final int PREMATURE_CLOSE_RETRIES = 2;

    private static final String DCM = ".dcm";

    private static final String UNDER_SCORE = "_";

    /** Mime type */
    private static final String CONTENT_TYPE_MULTIPART = "multipart/related";

    private static final String CONTENT_TYPE_DICOM = "application/dicom";

    private static final String CONTENT_TYPE_DICOM_XML = "application/dicom+xml";

    private static final String CONTENT_TYPE_DICOM_JSON = "application/json";

    private static final String CONTENT_TYPE = "&contentType";

    @Autowired
    private WebClient.Builder webClientBuilder;

    @Autowired
    private WADOURLHandler wadoURLHandler;

    private WebClient webClient;

    @Autowired
    @Lazy
    private DatasetService datasetService;

    /** PACS HTTP connection pool size */
    @Value("${dcm4chee-arc.wado.pool.max-connections:1000}")
    private int wadoMaxConnections;

    /** PACS HTTP connection pool size */
    @Value("${dcm4chee-arc.wado.pool.pending-acquire-max-count:10000}")
    private int wadoPendingAcquireMaxCount;

    /** How long a caller waits in the pending queue for a connection */
    @Value("${dcm4chee-arc.wado.pool.pending-acquire-timeout-seconds:10}")
    private long wadoPendingAcquireTimeoutSeconds;

    /** How long a connection remains unused in the pool before getting destroyed and free resources. It must remains below the equivalent PACS value, otherwise Shanoir logs would be flooded with "Connection closed prematurely" */
    @Value("${dcm4chee-arc.wado.pool.max-idle-seconds:15}")
    private long wadoMaxIdleSeconds;

    /** How long a connection can live at maximum (otherwise the data sent might be wrong or uncontrolled) */
    @Value("${dcm4chee-arc.wado.pool.max-life-seconds:300}")
    private long wadoMaxLifeSeconds;

    /** How long a Shanoir thread try to create an HTTP connection with the PACS before time-out */
    @Value("${dcm4chee-arc.wado.connect-timeout-ms:5000}")
    private int wadoConnectTimeoutMs;

    /** How long a connection is used before returning to the pool if no byte is sent */
    @Value("${dcm4chee-arc.wado.response-timeout-seconds:60}")
    private long wadoResponseTimeoutSeconds;

    /**
     * Buffer cap for responses that cannot be streamed (multipart/related WADO-RS, metadata).
     *
     * The common WADO-URI instance retrieve is streamed straight into the zip and never buffered,
     * which is what makes ~1000 concurrent downloads survivable on a normal heap.
     */
    @Value("${dcm4chee-arc.wado.max-in-memory-bytes:67108864}")
    private int wadoMaxInMemoryBytes;

    /**
     * Thrown when a PACS download must be abandoned as a whole rather than continued file by
     * file - typically because the HTTP client has gone away (VIP gives up after 30s and
     * retries, and finishing a download nobody reads just steals PACS capacity from live ones).
     */
    public static class PacsDownloadAbortedException extends RuntimeException {
        public PacsDownloadAbortedException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    @PostConstruct
    public void initWebClient() {
        ConnectionProvider provider = ConnectionProvider.builder("pacs-wado")
                .maxConnections(wadoMaxConnections)
                .pendingAcquireMaxCount(wadoPendingAcquireMaxCount)
                .pendingAcquireTimeout(Duration.ofSeconds(wadoPendingAcquireTimeoutSeconds))
                .maxIdleTime(Duration.ofSeconds(wadoMaxIdleSeconds))
                .maxLifeTime(Duration.ofSeconds(wadoMaxLifeSeconds))
                .evictInBackground(Duration.ofSeconds(30))
                .fifo()
                .build();

        HttpClient httpClient = HttpClient.create(provider)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, wadoConnectTimeoutMs)
                .responseTimeout(Duration.ofSeconds(wadoResponseTimeoutSeconds))
                .keepAlive(true)
                .doOnConnected(conn -> conn.addHandlerLast(
                        new ReadTimeoutHandler(wadoResponseTimeoutSeconds, TimeUnit.SECONDS)));

        this.webClient = webClientBuilder
                .clone() // do not mutate the shared WebClient.Builder bean
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .codecs(configurer -> configurer
                        .defaultCodecs()
                        .maxInMemorySize(wadoMaxInMemoryBytes))
                .build();

        LOG.info("PACS WADO client ready: max-connections={}, pending-acquire={} ({}s), "
                        + "connect-timeout={}ms, response-timeout={}s",
                wadoMaxConnections, wadoPendingAcquireMaxCount, wadoPendingAcquireTimeoutSeconds,
                wadoConnectTimeoutMs, wadoResponseTimeoutSeconds);
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
        Set<String> zippedUrls = new HashSet<>();
        long duplicates = 0;
        String fileNamePrefix = buildFileNamePrefix(subjectName, dataset, datasetFilePath);
        for (Iterator<URL> iterator = urls.iterator(); iterator.hasNext();) {
            String url = iterator.next().toString();
            if (!zippedUrls.contains(url)) {
                zippedUrls.add(url);
                String sopInstanceUID = wadoURLHandler.extractUIDs(url)[2];
                // Build name
                String name = fileNamePrefix + sanitizeForFileName(sopInstanceUID);
                // Download and zip
                try {
                    String zipedFile = null;
                    if (dataset.getSource() != null) {
                        zipedFile = downloadAndWriteFileInZip(url, zipOutputStream, name, subjectName);
                    } else {
                        zipedFile = downloadAndWriteFileInZip(url, zipOutputStream, name, null);
                    }
                    if (zipedFile != null) {
                        files.add(zipedFile);
                    }
                } catch (ZipPacsFileException e) {
                    HttpStatusCode pacsClientError = pacsClientError(e);
                    if (pacsClientError != null) {
                        // The PACS says this object is not retrievable (typically 404): every other
                        // instance of the series will fail identically. Stop now instead of emitting
                        // one failed request + one stack trace per slice.
                        LOG.warn("Dataset [{}] not retrievable from PACS ({}) - skipping its remaining files. First failing URL: {}",
                                dataset.getId(), pacsClientError, url);
                        downloadResult.update("Dataset [" + dataset.getId() + "] not retrievable from PACS (" + pacsClientError + ")",
                                DatasetDownloadError.ERROR);
                        break;
                    }
                    LOG.error("Could not download a file of dataset [{}] as dicom: {}", dataset.getId(), e.getMessage());
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

    /**
     * Walks the cause chain for a 4xx response from the PACS (e.g. 404 - object missing / stale
     * WADO URL). Returns the status if found, else null. A 4xx means retrying other files of the
     * same dataset is pointless; a 5xx / IO / timeout may be transient and is handled per file.
     */
    private static HttpStatusCode pacsClientError(Throwable e) {
        for (Throwable t = e; t != null; t = t.getCause()) {
            if (t instanceof HttpClientErrorException hce && hce.getStatusCode().is4xxClientError()) {
                return hce.getStatusCode();
            }
        }
        return null;
    }

    private String buildFileNamePrefix(String subjectName, Dataset dataset, String datasetFilePath) {
        String serieDescription = dataset.getUpdatedMetadata().getName();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("YYYYMMdd");
        Dataset realInput = datasetService.getFirstRealInput(dataset);
        String examDate = realInput.getDatasetAcquisition().getExamination().getExaminationDate().format(formatter);
        String prefix = sanitizeForFileName(subjectName + "_" + examDate + "_" + serieDescription + "_");
        // add folder logic if necessary (datasetFilePath is already sanitised by its caller)
        return datasetFilePath != null ? datasetFilePath + File.separator + prefix : prefix;
    }

    private static String sanitizeForFileName(String value) {
        return value.replaceAll("[^a-zA-Z0-9\\.\\-]", "_");
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
    private String downloadAndWriteFileInZip(String url, ZipOutputStream zipOutputStream, String name, String subjectName) throws ZipPacsFileException {
        try {
            if (!url.contains(WADO_REQUEST_TYPE_WADO_RS) && (subjectName == null || subjectName.trim().isEmpty())) {
                // Plain WADO-URI instance retrieve, no patient-name rewriting: pipe the response
                // straight from the socket into the zip entry. Nothing is buffered, so heap use
                // stays flat no matter how many downloads run at once - the whole point at
                // ~1000 concurrent requests.
                streamFileFromPACSIntoZip(url, zipOutputStream, name);
            } else {
                // Multipart WADO-RS, or an anonymised copy whose DICOM tags must be rewritten:
                // both need the whole object in memory before it can be written out.
                byte[] responseBody = downloadFileFromPACS(url);
                extractDICOMZipFromMHTMLFile(responseBody, name, zipOutputStream, url.contains(WADO_REQUEST_TYPE_WADO_RS), subjectName);
            }
            return name + DCM;
        } catch (IOException | MessagingException e) {
            if (isClientDisconnected(e)) {
                throw new PacsDownloadAbortedException("HTTP client disconnected during download", e);
            }
            LOG.error("Error in downloading/writing file [{}] from pacs to zip", name, e);
            throw new ZipPacsFileException(e);
        } catch (HttpClientErrorException e) {
            throw new ZipPacsFileException("Received " + e.getStatusCode() + " from PACS", e);
        }
    }

    /**
     * Copies a WADO-URI instance retrieve from the PACS into a zip entry without ever holding the
     * whole object in memory: reactor-netty hands over small buffers as they arrive off the
     * socket and the calling (request) thread drains them into the zip.
     */
    private void streamFileFromPACSIntoZip(String url, ZipOutputStream zipOutputStream, String name)
            throws IOException, HttpClientErrorException {
        ZipEntry entry = new ZipEntry(name + DCM);
        zipOutputStream.putNextEntry(entry);
        try {
            for (int attempt = 0; ; attempt++) {
                try {
                    copyResponseIntoZip(url, zipOutputStream);
                    return;
                } catch (PrematureCloseRetryable e) {
                    // The PACS closed the connection before handing over a single byte, so
                    // nothing has been written into the entry yet and retrying is safe. This is
                    // almost always a keep-alive race: we picked an idle pooled connection at the
                    // very moment dcm4chee closed it. Retrying costs one round-trip; failing the
                    // file costs the whole dataset.
                    if (attempt >= PREMATURE_CLOSE_RETRIES) {
                        throw new IOException("Download failed after " + (attempt + 1)
                                + " attempts: " + e.getMessage(), e.getCause());
                    }
                    LOG.debug("Retrying [{}] after a premature close from the PACS (attempt {})", url, attempt + 1);
                }
            }
        } finally {
            zipOutputStream.closeEntry();
        }
    }

    /**
     * One attempt at copying the response body into the (already opened) zip entry.
     * {@code toStream()} is closeable: closing it cancels the subscription, and {@code doOnDiscard}
     * releases any buffer still queued at that point. Without both, aborting a download part-way
     * (client gone, PACS error) would leak pooled netty buffers on every failure.
     */
    private void copyResponseIntoZip(String url, ZipOutputStream zipOutputStream)
            throws IOException, HttpClientErrorException, PrematureCloseRetryable {
        boolean anyByteWritten = false;
        try (Stream<DataBuffer> body = responseBodyStream(url).toStream(STREAM_PREFETCH)) {
            Iterator<DataBuffer> buffers = body.iterator();
            while (buffers.hasNext()) {
                DataBuffer buffer = buffers.next();
                try (InputStream in = buffer.asInputStream()) {
                    anyByteWritten |= in.transferTo(zipOutputStream) > 0;
                } finally {
                    DataBufferUtils.release(buffer);
                }
            }
        } catch (WebClientResponseException e) {
            throw new HttpClientErrorException(e.getStatusCode(), "Download failed: " + e.getMessage());
        } catch (RuntimeException e) {
            if (e.getCause() instanceof IOException ioe) {
                throw ioe;
            }
            if (e instanceof WebClientRequestException wcre && isPacsPoolSaturated(wcre)) {
                throw new HttpClientErrorException(HttpStatus.SERVICE_UNAVAILABLE,
                        "PACS connection pool saturated: " + e.getMessage());
            }
            // Only retryable while the entry is still empty - once bytes are in the zip we cannot
            // start the body over without duplicating them.
            if (!anyByteWritten && isPrematureClose(e)) {
                throw new PrematureCloseRetryable(e);
            }
            throw new IOException("Download failed: " + e.getMessage(), e);
        }
    }

    private static boolean isPrematureClose(Throwable e) {
        for (Throwable t = e; t != null; t = t.getCause()) {
            if (t.getClass().getSimpleName().equals("PrematureCloseException")) {
                return true;
            }
        }
        return false;
    }

    /** Internal marker: the response died before any byte reached the zip, so it can be retried. */
    private static class PrematureCloseRetryable extends Exception {
        PrematureCloseRetryable(Throwable cause) {
            super(cause.getMessage(), cause);
        }
    }

    private Flux<DataBuffer> responseBodyStream(final String url) {
        return webClient.get()
                .uri(url)
                .header(HttpHeaders.ACCEPT, CONTENT_TYPE_MULTIPART + "; type=" + CONTENT_TYPE_DICOM + ";")
                .retrieve()
                .bodyToFlux(DataBuffer.class)
                .doOnDiscard(DataBuffer.class, DataBufferUtils::release);
    }

    private static boolean isClientDisconnected(Throwable e) {
        for (Throwable t = e; t != null; t = t.getCause()) {
            if (t.getClass().getName().endsWith("ClientAbortException")) {
                return true;
            }
            String msg = t.getMessage();
            if (msg != null && (msg.contains("Broken pipe")
                    || msg.contains("Connection reset")
                    || msg.contains("connection was aborted")
                    || msg.contains("An existing connection was forcibly closed"))) {
                return true;
            }
        }
        return false;
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
            String url = iterator.next().toString();
            try {
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
            } catch (PacsDownloadAbortedException e) {
                throw e;
            } catch (Exception e) {
                HttpStatusCode pacsClientError = pacsClientError(e);
                if (pacsClientError != null) {
                    LOG.warn("Dataset [{}] not retrievable from PACS ({}) - skipping its remaining files. First failing URL: {}",
                            dataset.getId(), pacsClientError, url);
                    downloadResult.update("Dataset [" + dataset.getId() + "] not retrievable from PACS (" + pacsClientError + ")",
                            DatasetDownloadError.ERROR);
                    break;
                }
                LOG.error("A dicom file of dataset [{}] could not be downloaded from the pacs: {}", dataset.getId(), e.getMessage());
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
            return downloadFileFromPACSAsync(url)
                    .block(); // Block at the end to convert Mono to sync result
        } catch (WebClientResponseException e) {
            throw new HttpClientErrorException(e.getStatusCode(),
                    "Download failed: " + e.getMessage());
        } catch (WebClientRequestException e) {
            if (isPacsPoolSaturated(e)) {
                LOG.error("PACS connection pool [pacs-wado] saturated while downloading [{}] "
                        + "(max-connections={}, pending-acquire-max-count={}). "
                        + "Consider raising dcm4chee-arc.wado.pool.* or reducing download concurrency.",
                        url, wadoMaxConnections, wadoPendingAcquireMaxCount);
                throw new HttpClientErrorException(HttpStatus.SERVICE_UNAVAILABLE,
                        "PACS connection pool saturated: " + e.getMessage());
            }
            throw new IOException("Download failed: " + e.getMessage(), e);
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
        } catch (WebClientRequestException e) {
            if (isPacsPoolSaturated(e)) {
                LOG.error("PACS connection pool [pacs-wado] saturated while fetching metadata [{}]", url);
            }
            throw new IOException("Download failed: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new IOException("Download failed: " + e.getMessage(), e);
        }
    }

    private boolean isPacsPoolSaturated(WebClientRequestException e) {
        for (Throwable t = e; t != null; t = t.getCause()) {
            String name = t.getClass().getSimpleName();
            if ("PoolAcquirePendingLimitException".equals(name)
                    || "PoolAcquireTimeoutException".equals(name)) {
                return true;
            }
            String msg = t.getMessage();
            if (msg != null && msg.contains("Pending acquire queue has reached its maximum size")) {
                return true;
            }
        }
        return false;
    }

    private Mono<byte[]> downloadFileFromPACSAsync(final String url) {
        return webClient.get()
                .uri(url)
                .header(HttpHeaders.ACCEPT, CONTENT_TYPE_MULTIPART + "; type=" + CONTENT_TYPE_DICOM + ";")
                .retrieve()
                .bodyToMono(byte[].class)
                .timeout(Duration.ofSeconds(wadoResponseTimeoutSeconds));
    }

    private Mono<String> downloadMetadataFromPACSAsync(final String url) {
        return webClient.get()
                .uri(url)
                .header(HttpHeaders.ACCEPT, CONTENT_TYPE_DICOM_JSON)
                .retrieve()
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
