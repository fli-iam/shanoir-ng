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

package org.shanoir.ng.dicom.web.service;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.hc.client5.http.classic.methods.HttpDelete;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.mime.ContentBody;
import org.apache.hc.client5.http.entity.mime.FileBody;
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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

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

    public String findStudyByDicomPatientId(String patientId) {
        try {
            String url = this.serverURL + "?PatientID=" + patientId;
            HttpGet httpGet = new HttpGet(url);
            httpGet.setHeader("Accept-Charset", "UTF-8");
            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    return EntityUtils.toString(entity, "UTF-8");
                } else {
                    LOG.error("DICOMWeb: findStudy: empty response entity for Patient ID: " + patientId);
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
                    return EntityUtils.toString(entity, "UTF-8");
                } else {
                    LOG.error("DICOMWeb: findSeriesOfStudy: empty response entity for studyInstanceUID: "
                            + studyInstanceUID);
                }
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * With DICOMWeb for viewer OHIF, no need to transfer private tags,
     * so we exclude it here. Fix for Github issue #2805.
     *
     * @param studyInstanceUID
     * @param serieInstanceUID
     * @return
     */
    public String findSerieMetadataOfStudy(String studyInstanceUID, String serieInstanceUID) {
        try {
            String url = this.serverURL + "/" + studyInstanceUID + "/series/" + serieInstanceUID
                    + "/metadata?excludeprivate=false";
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

    /**
     * This method is used by the viewer OHIF to display the actual images.
     * The raw pixel data are searched in dcm4chee arc light behind Shanoir
     * and send as byte array to OHIF.
     *
     * @param studyInstanceUID
     * @param serieInstanceUID
     * @param sopInstanceUID
     * @param frame
     * @return
     */
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

    /**
     * This method is used by OHIF viewer, double-click on DICOM SEG.
     * ShanoirUploader is calling it to get a DICOM instance,
     * when running its job to check examination consistency.
     *
     * @param studyInstanceUID
     * @param serieInstanceUID
     * @param sopInstanceUID
     * @return
     */
    public ResponseEntity findInstance(String studyInstanceUID, String serieInstanceUID, String sopInstanceUID) {
        try {
            String url = this.serverURL + "/" + studyInstanceUID + "/series/" + serieInstanceUID + "/instances/"
                    + sopInstanceUID;
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
        if (directoryWithDicomFiles == null || !directoryWithDicomFiles.exists()
                || !directoryWithDicomFiles.isDirectory()) {
            LOG.error("sendDicomFilesToPacs called with null, or file: not existing or not a directory.");
            throw new ShanoirException(
                    "sendDicomFilesToPacs called with null, or file: not existing or not a directory.");
        }
        File[] dicomFiles = directoryWithDicomFiles.listFiles();
        LOG.info("Start: STOW-RS sending " + dicomFiles.length + " dicom files to PACS from folder: "
                + directoryWithDicomFiles.getAbsolutePath());
        MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();
        multipartEntityBuilder.setBoundary(BOUNDARY);
        multipartEntityBuilder.setMimeSubtype(RELATED);
        // create one multipart part for each file
        for (File dicomFile : dicomFiles) {
            addFileToMultipart(dicomFile, multipartEntityBuilder);
        }
        HttpEntity entity = multipartEntityBuilder.build();
        sendMultipartRequest(entity);
        LOG.info("Finished: STOW-RS sending " + dicomFiles.length + " dicom files to PACS from folder: "
                + directoryWithDicomFiles.getAbsolutePath());
    }

    public void sendDicomFileToPacs(File dicomFile) throws ShanoirException {
        if (dicomFile == null || !dicomFile.exists()) {
            LOG.error("sendDicomFileToPacs called with null, or file: not existing.");
            throw new ShanoirException("sendDicomFileToPacs called with null, or file: not existing.");
        }
        LOG.info("Start: STOW-RS sending one dicom file to PACS: " + dicomFile.getAbsolutePath());
        MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();
        multipartEntityBuilder.setBoundary(BOUNDARY);
        multipartEntityBuilder.setMimeSubtype(RELATED);
        addFileToMultipart(dicomFile, multipartEntityBuilder);
        HttpEntity entity = multipartEntityBuilder.build();
        sendMultipartRequest(entity);
        LOG.info("Finished: STOW-RS sending one dicom file to PACS: " + dicomFile.getAbsolutePath());
    }

    private void addFileToMultipart(File dicomFile, MultipartEntityBuilder multipartEntityBuilder)
            throws ShanoirException {
        try {
            FileBody fileBody = new FileBody(
                    dicomFile,
                    ContentType.create(CONTENT_TYPE_DICOM));
            MultipartPartBuilder partBuilder = MultipartPartBuilder.create()
                    .addHeader(CONTENT_TYPE, CONTENT_TYPE_DICOM)
                    .setBody(fileBody);
            multipartEntityBuilder.addPart(partBuilder.build());
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            throw new ShanoirException(e.getMessage());
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
    public void sendDicomInputStreamToPacs(InputStream inputStream) throws ShanoirException {
        LOG.debug("Start: STOW-RS sending dicom file input stream to PACS.");
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
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            throw new ShanoirException(e.getMessage());
        }
        LOG.debug("Finished: STOW-RS sending dicom file input stream to PACS.");
    }

    private DICOMWebSTOWRSResult sendMultipartRequest(HttpEntity entity) throws ShanoirException {
        HttpPost httpPost = new HttpPost(dcm4cheeProtocol + dcm4cheeHost + ":" + dcm4cheePort + dicomWebRSUpload);
        httpPost.setHeader(HttpHeaders.CONTENT_TYPE,
                CONTENT_TYPE_MULTIPART + ";type=" + CONTENT_TYPE_DICOM + ";boundary=" + BOUNDARY);
        httpPost.setHeader(HttpHeaders.ACCEPT, "application/dicom+json");
        httpPost.setEntity(entity);
        try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
            int code = response.getCode();
            if (code != HttpStatus.OK.value() && code != HttpStatus.ACCEPTED.value()) {
                LOG.error("DICOMWeb: sendMultipartRequest: response code not 200, but: " + code);
                LOG.error("Associated message: " + EntityUtils.toString(response.getEntity()));
                throw new ShanoirException("DICOMWeb: sendMultipartRequest: response code not 200, but: " + code);
            }
            String responseBody = EntityUtils.toString(response.getEntity());
            DICOMWebSTOWRSResult result = parseJsonResponse(responseBody);
            long duplicateCount = result.getInstances().stream()
                    .filter(DICOMWebSTOWRSResultInstance::isDuplicate)
                    .count();
            if (duplicateCount > 0) {
                LOG.warn("DICOMWeb: {} duplicate instance(s) detected and ignored", duplicateCount);
                result.getInstances().stream()
                        .filter(DICOMWebSTOWRSResultInstance::isDuplicate)
                        .forEach(inst -> LOG.debug("Duplicate instance: {} (reason: 0x{} - {})",
                                inst.getSopInstanceUID(),
                                Integer.toHexString(inst.getFailureReason()).toUpperCase(),
                                inst.getFailureReasonText()));
                throw new ShanoirException("DICOMWeb: " + duplicateCount + " duplicate instance(s) detected and ignored");
            }
            if (result.getFailureCount() > 0) {
                LOG.warn("DICOMWeb: {} instance(s) failed to store", result.getFailureCount());
                throw new ShanoirException("DICOMWeb: " + result.getFailureCount() + " instance(s) failed to store");
            }
            LOG.debug("DICOMWeb STOW-RS result: {} success, {} warnings, {} failures",
                    result.getSuccessCount(), result.getWarningCount(), result.getFailureCount());
            return result;
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            throw new ShanoirException(e.getMessage());
        }
    }

    private DICOMWebSTOWRSResult parseJsonResponse(String responseBody) {
        DICOMWebSTOWRSResult result = new DICOMWebSTOWRSResult();
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(responseBody);
            // Parse Referenced SOP Sequence (00081199) - successful instances
            JsonNode referencedSopSeq = findDicomAttribute(root, "00081199");
            if (referencedSopSeq != null && referencedSopSeq.has("Value")) {
                JsonNode items = referencedSopSeq.get("Value");
                for (JsonNode item : items) {
                    DICOMWebSTOWRSResultInstance instResult = parseInstanceFromJson(item, true);
                    if (instResult != null) {
                        result.addInstance(instResult);
                        result.incrementSuccess();
                    }
                }
            }
            // Parse Failed SOP Sequence (00081198) - failed/warned instances
            JsonNode failedSopSeq = findDicomAttribute(root, "00081198");
            if (failedSopSeq != null && failedSopSeq.has("Value")) {
                JsonNode items = failedSopSeq.get("Value");
                for (JsonNode item : items) {
                    DICOMWebSTOWRSResultInstance instResult = parseInstanceFromJson(item, false);
                    if (instResult != null) {
                        result.addInstance(instResult);
                        if (instResult.isDuplicate()) {
                            result.incrementWarning();
                        } else {
                            result.incrementFailure();
                        }
                    }
                }
            }
            // If no sequences found, assume success
            if (result.getInstances().isEmpty()) {
                result.incrementSuccess();
            }
        } catch (Exception e) {
            LOG.error("Error parsing JSON STOW-RS response: {}", e.getMessage(), e);
            // Treat as success if we can't parse (fail-safe)
            result.incrementSuccess();
        }
        return result;
    }

    private JsonNode findDicomAttribute(JsonNode root, String tag) {
        // DICOM JSON format can be array or object
        if (root.isArray()) {
            for (JsonNode item : root) {
                if (item.has(tag)) {
                    return item.get(tag);
                }
            }
        } else if (root.isObject() && root.has(tag)) {
            return root.get(tag);
        }
        return null;
    }

    private DICOMWebSTOWRSResultInstance parseInstanceFromJson(JsonNode item, boolean isSuccess) {
        DICOMWebSTOWRSResultInstance instResult = new DICOMWebSTOWRSResultInstance();
        try {
            // Referenced SOP Instance UID (00081155)
            JsonNode sopInstanceUID = item.get("00081155");
            if (sopInstanceUID != null && sopInstanceUID.has("Value")) {
                instResult.setSopInstanceUID(sopInstanceUID.get("Value").get(0).asText());
            }
            // Referenced SOP Class UID (00081150)
            JsonNode sopClassUID = item.get("00081150");
            if (sopClassUID != null && sopClassUID.has("Value")) {
                instResult.setSopClassUID(sopClassUID.get("Value").get(0).asText());
            }
            // Failure Reason (00081197)
            JsonNode failureReason = item.get("00081197");
            if (failureReason != null && failureReason.has("Value")) {
                int reasonCode = failureReason.get("Value").get(0).asInt();
                instResult.setFailureReason(reasonCode);
                instResult.setFailureReasonText(getFailureReasonText(reasonCode));
            }
            // Set status
            if (isSuccess && instResult.getFailureReason() == null) {
                instResult.setStatus("SUCCESS");
            } else if (instResult.getFailureReason() != null) {
                if (instResult.isDuplicate()) {
                    instResult.setStatus("WARNING");
                } else {
                    instResult.setStatus("FAILURE");
                }
            }
            return instResult;
        } catch (Exception e) {
            LOG.error("Error parsing instance from JSON: {}", e.getMessage(), e);
            return null;
        }
    }

    private String getFailureReasonText(Integer code) {
        if (code == null)
            return "Unknown";
        switch (code) {
            case 0xB306: // 45830
                return "Instance already stored (duplicate)";
            case 0xB305: // 45829
                return "Instance received but not stored (duplicate)";
            case 0x0110: // 272
                return "Processing failure";
            case 0x0122: // 290
                return "Referenced SOP Class not supported";
            case 0xA700: // 42752
                return "Out of resources";
            case 0xA900: // 43264
                return "Data set does not match SOP Class";
            case 0xC000: // 49152
                return "Cannot understand";
            default:
                return String.format("Failure code: 0x%04X", code);
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
        if (wadoURLHandler.isWadoUri(url)) {
            rejectURL = wadoURLHandler.convertWadoUriToWadoRs(url) + REJECT_SUFFIX;
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

    @Scheduled(cron = "0 */30 * * * *", zone = "Europe/Paris")
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
