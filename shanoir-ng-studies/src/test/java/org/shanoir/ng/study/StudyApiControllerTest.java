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

package org.shanoir.ng.study;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;
import org.shanoir.ng.shared.core.model.IdName;
import org.shanoir.ng.shared.error.FieldErrorMap;
import org.shanoir.ng.shared.event.ShanoirEventService;
import org.shanoir.ng.shared.jackson.JacksonUtils;
import org.shanoir.ng.storage.StorageService;
import org.shanoir.ng.study.controler.StudyApiController;
import org.shanoir.ng.study.dto.StudyDTO;
import org.shanoir.ng.study.dto.mapper.StudyMapper;
import org.shanoir.ng.study.dua.DataUserAgreementService;
import org.shanoir.ng.study.model.Study;
import org.shanoir.ng.study.security.StudyFieldEditionSecurityManager;
import org.shanoir.ng.study.security.StudySecurityService;
import org.shanoir.ng.study.service.RelatedDatasetService;
import org.shanoir.ng.study.service.StudyService;
import org.shanoir.ng.study.service.StudyUniqueConstraintManager;
import org.shanoir.ng.study.service.StudyUserService;
import org.shanoir.ng.tag.model.StudyTagMapper;
import org.shanoir.ng.utils.ModelsUtil;
import org.shanoir.ng.utils.usermock.WithMockKeycloakUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

/**
 * Unit tests for study controller.
 *
 * @author msimon
 *
 */
@WebMvcTest(controllers = StudyApiController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
public class StudyApiControllerTest {

    private static final Logger LOG = LoggerFactory.getLogger(StudyApiControllerTest.class);

    private static final String REQUEST_PATH = "/studies";
    private static final String REQUEST_PATH_FOR_NAMES = REQUEST_PATH + "/names";
    private static final String REQUEST_PATH_WITH_ID = REQUEST_PATH + "/1";
    private static final String REQUEST_PATH_FOR_MEMBERS = REQUEST_PATH_WITH_ID + "/members";
    private static final String REQUEST_PATH_FOR_MEMBER_WITH_ID = REQUEST_PATH_FOR_MEMBERS + "/1";

    @Autowired
    private MockMvc mvc;

    @MockBean
    private StudyMapper studyMapperMock;

    @MockBean
    private StudyTagMapper studyTagMapperMock;

    @MockBean
    private StudyService studyServiceMock;

    @MockBean
    private StudyUserService studyUserServiceMock;

    @MockBean
    private DataUserAgreementService dataUserAgreementServiceMock;

    @MockBean
    private StudyFieldEditionSecurityManager fieldEditionSecurityManager;

    @MockBean
    private StudyUniqueConstraintManager uniqueConstraintManager;

    @MockBean(name = "studySecurityService")
    private StudySecurityService studySecurityService;

    @MockBean
    private ShanoirEventService eventService;

    @MockBean
    private RelatedDatasetService relatedDatasetService;

    @MockBean
    private StorageService storageService;

    @TempDir
    private static File tempFolder;

    private static Resource tempResource;

    private static String tempFolderPath;

    @BeforeAll
    public static void beforeAll() throws IOException {
        tempFolderPath = tempFolder.getAbsolutePath() + "/tmp/";
        System.setProperty("storage.file-system.studies-data", tempFolderPath);
        File tempFile = new File(tempFolder, "test-file.txt");
        Files.writeString(tempFile.toPath(), "test content");
        tempResource = new FileSystemResource(tempFile);
    }

    @BeforeEach
    public void setup() throws Exception {
        given(studyMapperMock.studiesToStudyDTOs(Mockito.anyList()))
                .willReturn(Arrays.asList(new StudyDTO()));
        given(studyMapperMock.studyToStudyDTO(Mockito.any(Study.class))).willReturn(new StudyDTO());
        doNothing().when(studyServiceMock).deleteById(1L);
        given(storageService.loadStudyData(1L, "file.pdf")).willReturn(tempResource);
        given(studyServiceMock.findAll()).willReturn(Arrays.asList(new Study()));
        given(studyServiceMock.findAllNames()).willReturn(Arrays.asList(new IdName()));
        given(studyServiceMock.create(Mockito.mock(Study.class))).willReturn(new Study());
        given(fieldEditionSecurityManager.validate(Mockito.any(Study.class))).willReturn(new FieldErrorMap());
        given(uniqueConstraintManager.validate(Mockito.any(Study.class))).willReturn(new FieldErrorMap());
        given(studySecurityService.hasRightOnStudy(Mockito.anyLong(), Mockito.anyString())).willReturn(true);
    }

    // @Test
    public void addMember() throws Exception {
        mvc.perform(MockMvcRequestBuilders.put(REQUEST_PATH_FOR_MEMBERS).accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON).content(JacksonUtils.serialize(ModelsUtil.createStudyUser())))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockKeycloakUser(id = 12, username = "test", authorities = { "ROLE_ADMIN" })
    public void deleteStudyTest() throws Exception {
        Mockito.when(studyServiceMock.findById(Mockito.any(Long.class))).thenReturn(null);
        mvc.perform(MockMvcRequestBuilders.delete(REQUEST_PATH_WITH_ID).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = { "ROLE_ADMIN" })
    public void findStudiesTest() throws Exception {

        mvc.perform(MockMvcRequestBuilders.get(REQUEST_PATH).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = { "ROLE_ADMIN" })
    public void findStudiesNamesTest() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get(REQUEST_PATH_FOR_NAMES).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockKeycloakUser(id = 12, username = "test", authorities = { "ROLE_ADMIN" })
    public void testUploadProtocolFile() throws IOException {
        File importZip = new File(tempFolderPath + "/test-import-extra-data.zip");
        File saved = new File(tempFolderPath + "study-1/test-import-extra-data.pdf");
        if (saved.exists()) {
            saved.delete();
        }
        try {
            new File(tempFolderPath).mkdirs();
            importZip.createNewFile();
            MockMultipartFile file = new MockMultipartFile("file", "test-import-extra-data.pdf", MediaType.MULTIPART_FORM_DATA_VALUE, new FileInputStream(importZip.getAbsolutePath()));
            // WHEN The file is added to the examination
            mvc.perform(MockMvcRequestBuilders.multipart(REQUEST_PATH + "/protocol-file-upload/1").file(file))
                    .andExpect(status().isOk());
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            fail();
        }
    }

    @Test
    @WithMockUser
    public void testDownloadProtocolFile() throws IOException {
        try {
            MvcResult result = mvc.perform(MockMvcRequestBuilders.get(REQUEST_PATH + "/protocol-file-download/1/file.pdf/"))
                    .andExpect(status().isOk())
                    .andReturn();
            // THEN the file is downloaded
            assertNotNull(result.getResponse().getContentAsString());
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            fail();
        }
    }

}
