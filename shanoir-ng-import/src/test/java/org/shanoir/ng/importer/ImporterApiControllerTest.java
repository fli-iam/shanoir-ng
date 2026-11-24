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

package org.shanoir.ng.importer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.shanoir.ng.importer.dicom.DicomDirGeneratorService;
import org.shanoir.ng.importer.dicom.DicomDirToModelService;
import org.shanoir.ng.importer.dicom.ImagesCreatorAndDicomFileAnalyzerService;
import org.shanoir.ng.importer.dicom.query.QueryPACSService;
import org.shanoir.ng.importer.model.EegDataset;
import org.shanoir.ng.importer.model.EegImportJob;
import org.shanoir.ng.shared.event.ShanoirEventService;
import org.shanoir.ng.shared.controller.GlobalExceptionHandler;
import org.shanoir.ng.utils.ImportUtils;
import org.shanoir.ng.utils.usermock.WithMockKeycloakUser;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.client.RestTemplate;

import tools.jackson.databind.json.JsonMapper;


/**
 * Unit tests for importer controller.
 *
 * @author atouboul
 *
 */
@WebMvcTest(controllers = ImporterApiController.class,
        excludeAutoConfiguration = {
                org.springframework.boot.security.oauth2.server.resource.autoconfigure.servlet.OAuth2ResourceServerAutoConfiguration.class
        })
@Import(GlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
public class ImporterApiControllerTest {

    private static final String START_EEG_JOB_PATH = "/importer/start_import_eeg_job/";

    private static final String GET_DICOM = "/importer/get_dicom/";

    private static final String UPLOAD_DICOM = "/importer/upload_dicom/";

    private static final String UPLOAD_MULTIPLE_DICOM =
            "/importer/upload_multiple_dicom/study/1/studyName/test/studyCard/0/center/1/equipment/1/";

    private static Path importDirectory;

    @DynamicPropertySource
    static void configureImportDirectory(DynamicPropertyRegistry registry) throws IOException {
        importDirectory = Files.createTempDirectory("shanoir-import-upload-test");
        registry.add("shanoir.import.directory", () -> importDirectory.toString());
    }

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private RestTemplate restTemplate;

    @MockitoBean
    private DicomDirToModelService dicomDirToModel;

    @MockitoBean
    private ImagesCreatorAndDicomFileAnalyzerService imagesCreatorAndDicomFileAnalyzer;

    @MockitoBean
    private ImporterManagerService importerManagerService;

    @MockitoBean
    private QueryPACSService queryPACSService;

    @MockitoBean
    private RabbitTemplate rabbitTemplate;

    @MockitoBean
    private DicomDirGeneratorService dicomDirGeneratorService;

    @MockitoBean
    private ShanoirEventService shanoirEventService;

    @MockitoBean
    private ImportJobStatusService importJobStatusService;

    @Autowired
    private JsonMapper mapper;

    @BeforeEach
    void cleanUserImportDir() throws IOException {
        File userImportDir = importDirectory.resolve("3").toFile();
        if (userImportDir.exists()) {
            FileUtils.deleteDirectory(userImportDir);
        }
    }

    public MockMultipartFile createFile(boolean withParticipants, boolean studyDescription,
            boolean sourceData, boolean importJson) throws IOException {
        File importDir = new File("/tmp/test-import-as-bids");
        importDir.mkdirs();
        if (withParticipants) {
            File participants = new File(importDir.getAbsolutePath() + "/" + "participants.tsv");
            participants.createNewFile();
        }
        if (studyDescription) {
            File studyDesc = new File(importDir.getAbsolutePath() + "/" + "dataset_description.json");
            studyDesc.createNewFile();
        }
        if (sourceData) {
            File sourceDataFile = new File(importDir.getAbsolutePath() + "/" + "sourcedata");
            File subjectFile = new File(sourceDataFile.getAbsolutePath() + "/sub-name");
            sourceDataFile.mkdir();
            subjectFile.mkdir();
            if (importJson) {
                File importJsonFile = new File(subjectFile.getAbsolutePath() + "/shanoir-import.json");
                importJsonFile.createNewFile();
                FileUtils.write(importJsonFile, "{\"studyId\": 1,\"studyCardId\": \"1\",\"patients\": [{\"patientID\":\"BidsCreated\",\"studies\" : [ {\"series\": [{\"images\": [{\"path\":\"pathToDicomImage\"}]}]}]}]}", StandardCharsets.UTF_8);
            }
        }
        File importZip = new File("/tmp/test-import-as-bids.zip");

        FileOutputStream outStream = new FileOutputStream(importZip.getAbsolutePath());
        ZipOutputStream zipout = new ZipOutputStream(outStream);
        ImportUtils.zipFile(importDir, importDir.getName(), zipout, true);
        zipout.close();
        outStream.close();
        return new MockMultipartFile("file", "test-import-as-bids.zip", "application/zip", new FileInputStream(importZip.getAbsolutePath()));
    }

    @Test
    @WithMockKeycloakUser(id = 3, username = "jlouis", authorities = { "ROLE_ADMIN" })
    public void testStartImportEEGJob() throws Exception {
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);

        EegImportJob importJob = new EegImportJob();
        importJob.setWorkFolder("/tmp/2/12341234");
        EegDataset dataset = new EegDataset();
        importJob.setExaminationId(1L);
        importJob.setDatasets(Collections.singletonList(dataset));
        dataset.setName("Ceci est un nom bien particulier");

        mvc.perform(MockMvcRequestBuilders.post(START_EEG_JOB_PATH)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(importJob)));

        // Just check that the name is well transmitted and that the call is made
        verify(rabbitTemplate).convertSendAndReceive(Mockito.any(String.class), captor.capture());

        //verify(restTemplate).exchange(Mockito.any(String.class), Mockito.eq(HttpMethod.POST), captor.capture(), Mockito.eq(String.class));
        assertTrue(((String) captor.getValue()).contains(dataset.getName()));
    }

    @Test
    @WithMockKeycloakUser(id = 3, username = "jlouis", authorities = { "ROLE_ADMIN" })
    public void testStartImportEEGJobNoReply() throws Exception {
        EegImportJob importJob = new EegImportJob();
        importJob.setWorkFolder("/tmp/2/12341234");
        importJob.setExaminationId(1L);
        importJob.setDatasets(Collections.singletonList(new EegDataset()));

        // rabbitTemplate mock returns null, as on a reply timeout
        mvc.perform(MockMvcRequestBuilders.post(START_EEG_JOB_PATH)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(importJob)))
                .andExpect(status().isGatewayTimeout());
    }

    @Test
    @WithMockKeycloakUser(id = 3, username = "jlouis", authorities = { "ROLE_ADMIN" })
    public void testGetDicomImageNoPath() throws Exception {

        mvc.perform(MockMvcRequestBuilders.get(GET_DICOM)
                .param("path", ""))
                .andExpect(status().is(200));
    }

    @Test
    @WithMockKeycloakUser(id = 3, username = "jlouis", authorities = { "ROLE_ADMIN" })
    public void testUploadDicomZipFileDeletesUnzippedWorkDirOnFailure() throws Exception {
        doAnswer(invocation -> {
            File dicomDir = invocation.getArgument(0);
            if (!dicomDir.exists()) {
                dicomDir.getParentFile().mkdirs();
                dicomDir.createNewFile();
            }
            return null;
        }).when(dicomDirGeneratorService).generateDicomDirFromDirectory(any(File.class), any(File.class));
        when(dicomDirToModel.readDicomDirToPatients(any(File.class))).thenReturn(Collections.emptyList());

        Path zipFile = Files.createTempFile("dicom-upload", ".zip");
        try (ZipOutputStream zipout = new ZipOutputStream(Files.newOutputStream(zipFile))) {
            zipout.putNextEntry(new ZipEntry("README.txt"));
            zipout.write("not-a-dicom".getBytes(StandardCharsets.UTF_8));
            zipout.closeEntry();
        }
        MockMultipartFile upload = new MockMultipartFile("file", "dicom.zip", "application/zip",
                Files.readAllBytes(zipFile));
        Files.deleteIfExists(zipFile);

        mvc.perform(multipart(UPLOAD_DICOM).file(upload))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value(
                        "No DICOM patient found in the ZIP; the archive must contain at least one valid DICOM instance."));

        File userImportDir = importDirectory.resolve("3").toFile();
        if (userImportDir.exists()) {
            File[] leftovers = userImportDir.listFiles();
            if (leftovers != null) {
                for (File leftover : leftovers) {
                    if (leftover.isFile()) {
                        assertTrue(false, "Unexpected file left after upload failure: " + leftover.getName());
                    }
                    if (leftover.isDirectory()) {
                        String[] nested = leftover.list();
                        assertTrue(nested == null || nested.length == 0,
                                "Unzipped import job folder should be removed after upload failure");
                    }
                }
            }
        }
    }

    @Test
    @WithMockKeycloakUser(id = 3, username = "jlouis", authorities = { "ROLE_ADMIN" })
    public void testUploadDicomZipFileRejectsZipWithNoDicomPatient() throws Exception {
        doAnswer(invocation -> {
            File dicomDir = invocation.getArgument(0);
            if (!dicomDir.exists()) {
                dicomDir.getParentFile().mkdirs();
                dicomDir.createNewFile();
            }
            return null;
        }).when(dicomDirGeneratorService).generateDicomDirFromDirectory(any(File.class), any(File.class));
        when(dicomDirToModel.readDicomDirToPatients(any(File.class))).thenReturn(Collections.emptyList());

        Path zipFile = Files.createTempFile("no-dicom-patient", ".zip");
        try (ZipOutputStream zipout = new ZipOutputStream(Files.newOutputStream(zipFile))) {
            zipout.putNextEntry(new ZipEntry("README.txt"));
            zipout.write("not dicom".getBytes(StandardCharsets.UTF_8));
            zipout.closeEntry();
        }
        MockMultipartFile upload = new MockMultipartFile("file", "dicom.zip", "application/zip",
                Files.readAllBytes(zipFile));
        Files.deleteIfExists(zipFile);

        mvc.perform(multipart(UPLOAD_DICOM).file(upload))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value(
                        "No DICOM patient found in the ZIP; the archive must contain at least one valid DICOM instance."));
    }

    @Test
    @WithMockKeycloakUser(id = 3, username = "jlouis", authorities = { "ROLE_ADMIN" })
    public void testUploadMultipleDicomRejectsExamFolderWithNoDicomPatient() throws Exception {
        doAnswer(invocation -> {
            File dicomDir = invocation.getArgument(0);
            if (!dicomDir.exists()) {
                dicomDir.getParentFile().mkdirs();
                dicomDir.createNewFile();
            }
            return null;
        }).when(dicomDirGeneratorService).generateDicomDirFromDirectory(any(File.class), any(File.class));
        when(dicomDirToModel.readDicomDirToPatients(any(File.class))).thenReturn(Collections.emptyList());

        Path staging = Files.createTempDirectory("multi-no-dicom-src");
        Files.createDirectories(staging.resolve("sub-test").resolve("ses-01"));
        Files.writeString(staging.resolve("sub-test").resolve("ses-01").resolve("README.txt"), "not dicom");
        File multiZip = Files.createTempFile("multi-no-dicom", ".zip").toFile();
        try (FileOutputStream outStream = new FileOutputStream(multiZip);
                ZipOutputStream zipout = new ZipOutputStream(outStream)) {
            ImportUtils.zipFile(staging.toFile(), staging.getFileName().toString(), zipout, true);
        }
        MockMultipartFile upload = new MockMultipartFile("file", "multi.zip", "application/zip",
                Files.readAllBytes(multiZip.toPath()));
        FileUtils.deleteQuietly(multiZip);
        FileUtils.deleteQuietly(staging.toFile());

        mvc.perform(multipart(UPLOAD_MULTIPLE_DICOM).file(upload))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value(
                        "No DICOM patient found in the ZIP; the archive must contain at least one valid DICOM instance."));
    }

    @Test
    @WithMockKeycloakUser(id = 3, username = "jlouis", authorities = { "ROLE_ADMIN" })
    public void testUploadMultipleDicomRejectsEmptySubjectFolder() throws Exception {
        Path staging = Files.createTempDirectory("multi-empty-subject-src");
        Files.createDirectories(staging.resolve("sub-test"));
        File multiZip = Files.createTempFile("multi-empty-subject", ".zip").toFile();
        try (FileOutputStream outStream = new FileOutputStream(multiZip);
                ZipOutputStream zipout = new ZipOutputStream(outStream)) {
            ImportUtils.zipFile(staging.toFile(), staging.getFileName().toString(), zipout, true);
        }
        MockMultipartFile upload = new MockMultipartFile("file", "multi.zip", "application/zip",
                Files.readAllBytes(multiZip.toPath()));
        FileUtils.deleteQuietly(multiZip);
        FileUtils.deleteQuietly(staging.toFile());

        mvc.perform(multipart(UPLOAD_MULTIPLE_DICOM).file(upload))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message")
                        .value("The subject folder must contain at least one examination sub-folder."));
    }

    @Test
    @WithMockKeycloakUser(id = 3, username = "jlouis", authorities = { "ROLE_ADMIN" })
    public void testUploadFileStoresTheFileWithItsNameOnly() throws Exception {
        Path tempDir = Files.createDirectories(importDirectory.resolve("3").resolve("456"));
        MockMultipartFile file = new MockMultipartFile("file", "../../evil.dcm", "application/octet-stream", new byte[] {1});

        mvc.perform(multipart("/importer/456").file(file))
                .andExpect(status().isOk());

        assertTrue(Files.exists(tempDir.resolve("evil.dcm")));
        assertFalse(Files.exists(importDirectory.resolve("3").resolve("evil.dcm")));
        assertFalse(Files.exists(importDirectory.resolve("evil.dcm")));
    }

    @Test
    @WithMockKeycloakUser(id = 3, username = "jlouis", authorities = { "ROLE_ADMIN" })
    public void testUploadFileRefusesAFileWithoutName() throws Exception {
        Path tempDir = Files.createDirectories(importDirectory.resolve("3").resolve("456"));
        MockMultipartFile file = new MockMultipartFile("file", "..", "application/octet-stream", new byte[] {1});

        mvc.perform(multipart("/importer/456").file(file))
                .andExpect(status().isUnprocessableEntity());

        assertEquals(0, tempDir.toFile().list().length);
    }

    @Test
    @WithMockKeycloakUser(id = 3, username = "jlouis", authorities = { "ROLE_ADMIN" })
    public void testUploadFileRefusesATempDirOutsideOfTheUserFolder() throws Exception {
        // ".." (URL encoded to reach the method) is the import folder of all the users
        Files.createDirectories(importDirectory.resolve("3"));
        MockMultipartFile file = new MockMultipartFile("file", "1.dcm", "application/octet-stream", new byte[] {1});

        mvc.perform(multipart(URI.create("/importer/%2E%2E")).file(file))
                .andExpect(status().isUnprocessableEntity());

        assertFalse(Files.exists(importDirectory.resolve("1.dcm")));
    }

    @Test
    @WithMockKeycloakUser(id = 3, username = "jlouis", authorities = { "ROLE_ADMIN" })
    public void testUploadProcessedDatasetStoresTheFileWithItsNameOnly() throws Exception {
        MockMultipartFile image = new MockMultipartFile("image", "../../../brain.nii", "application/octet-stream", new byte[] {1});

        String path = mvc.perform(multipart("/importer/upload_processed_dataset/").file(image))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Path stored = Path.of(path).normalize();
        assertEquals("brain.nii", stored.getFileName().toString());
        assertTrue(stored.startsWith(importDirectory.resolve("3")), stored + " is outside of the import folder of the user");
        assertTrue(Files.exists(stored));
        assertFalse(Files.exists(importDirectory.getParent().resolve("brain.nii")));
    }

    @Test
    @WithMockKeycloakUser(id = 3, username = "jlouis", authorities = { "ROLE_ADMIN" })
    public void testUploadProcessedDatasetRefusesAFileWithoutName() throws Exception {
        MockMultipartFile image = new MockMultipartFile("image", "folder/", "application/octet-stream", new byte[] {1});

        mvc.perform(multipart("/importer/upload_processed_dataset/").file(image))
                .andExpect(status().isUnprocessableEntity());
    }

}
