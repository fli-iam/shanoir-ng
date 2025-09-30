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

package org.shanoir.ng.preclinical.subjects;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.shanoir.ng.ShanoirPreclinicalApplication;
import org.shanoir.ng.preclinical.pathologies.subject_pathologies.SubjectPathologyService;
import org.shanoir.ng.preclinical.references.RefsService;
import org.shanoir.ng.preclinical.subjects.controller.AnimalSubjectApiController;
import org.shanoir.ng.preclinical.subjects.dto.AnimalSubjectDto;
import org.shanoir.ng.preclinical.subjects.dto.PreclinicalSubjectDto;
import org.shanoir.ng.preclinical.subjects.dto.PreclinicalSubjectDtoService;
import org.shanoir.ng.preclinical.subjects.dto.SubjectDto;
import org.shanoir.ng.preclinical.subjects.model.AnimalSubject;
import org.shanoir.ng.preclinical.subjects.service.AnimalSubjectEditableByManager;
import org.shanoir.ng.preclinical.subjects.service.AnimalSubjectService;
import org.shanoir.ng.preclinical.subjects.service.AnimalSubjectUniqueValidator;
import org.shanoir.ng.preclinical.therapies.subject_therapies.SubjectTherapyService;
import org.shanoir.ng.shared.error.FieldErrorMap;
import org.shanoir.ng.shared.event.ShanoirEventService;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.shared.jackson.JacksonUtils;
import org.shanoir.ng.utils.AnimalSubjectModelUtil;
import org.shanoir.ng.utils.usermock.WithMockKeycloakUser;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * Unit tests for subjects controller.
 *
 * @author sloury
 *
 */

@WebMvcTest(controllers = AnimalSubjectApiController.class)
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(classes = ShanoirPreclinicalApplication.class)
@ActiveProfiles("test")
public class AnimalSubjectApiControllerTest {

    private static final String REQUEST_PATH = "/subject";
    private static final String REQUEST_PATH_FIND = REQUEST_PATH + "/find";
    private static final String REQUEST_PATH_WITH_ID = REQUEST_PATH + "/2";

    @Autowired
    private MockMvc mvc;

    @MockBean
    private AnimalSubjectService subjectsServiceMock;

    @MockBean
    private RefsService refsServiceMock;

    @MockBean
    private SubjectPathologyService subjectPathologiesServiceMock;

    @MockBean
    private SubjectTherapyService subjectTherapiesServiceMock;

    @MockBean
    private ShanoirEventService eventService;

    @MockBean
    private AnimalSubjectUniqueValidator uniqueValidator;

    @MockBean
    private AnimalSubjectEditableByManager editableOnlyValidator;

    @MockBean
    private RabbitTemplate rabbitTemplate;

    @MockBean
    private PreclinicalSubjectDtoService dtoServiceMock;

    @BeforeEach
    public void setup() throws ShanoirException, JsonProcessingException {
        doNothing().when(subjectsServiceMock).deleteBySubjectId(1L);
        given(subjectsServiceMock.findAll()).willReturn(Arrays.asList(new AnimalSubject()));
        given(subjectsServiceMock.getBySubjectId(AnimalSubjectModelUtil.SUBJECT_ID)).willReturn(new AnimalSubject());
        given(subjectsServiceMock.createSubject(Mockito.any(SubjectDto.class))).willReturn(AnimalSubjectModelUtil.ID);
        given(subjectsServiceMock.isSubjectNameAlreadyUsedInStudy(AnimalSubjectModelUtil.SUBJECT_NAME, 1L)).willReturn(false);
        given(subjectsServiceMock.getBySubjectId(AnimalSubjectModelUtil.SUBJECT_ID)).willReturn(new AnimalSubject());
        PreclinicalSubjectDto dto = new PreclinicalSubjectDto();
        dto.setAnimalSubject(new AnimalSubjectDto());
        given(dtoServiceMock.getPreclinicalDtoFromAnimalSubject(Mockito.any(AnimalSubject.class))).willReturn(dto);
        given(dtoServiceMock.getAnimalSubjectDtoFromAnimalSubject(Mockito.any(AnimalSubject.class))).willReturn(dto.getAnimalSubject());
        given(dtoServiceMock.getAnimalSubjectDtoListFromAnimalSubjectList(Mockito.anyList())).willReturn(Arrays.asList(dto.getAnimalSubject()));
        given(dtoServiceMock.getAnimalSubjectFromAnimalSubjectDto(Mockito.any(AnimalSubjectDto.class))).willReturn(AnimalSubjectModelUtil.createAnimalSubject());
        given(dtoServiceMock.getAnimalSubjectFromPreclinicalDto(Mockito.any(PreclinicalSubjectDto.class))).willReturn(AnimalSubjectModelUtil.createAnimalSubject());
        AnimalSubject subject = new AnimalSubject();
        given(subjectsServiceMock.getBySubjectId(AnimalSubjectModelUtil.SUBJECT_ID)).willReturn(subject);
        AnimalSubject anSubj = new AnimalSubject();
        anSubj.setId(1L);
        given(subjectsServiceMock.save(Mockito.any(AnimalSubject.class))).willReturn(anSubj );
        given(uniqueValidator.validate(Mockito.any(AnimalSubject.class))).willReturn(new FieldErrorMap());
        given(editableOnlyValidator.validate(Mockito.any(AnimalSubject.class))).willReturn(new FieldErrorMap());
    }

    @Test
    public void findSubjectByIdTest() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get(REQUEST_PATH_WITH_ID).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void findSubjectsTest() throws Exception {
        mvc.perform(MockMvcRequestBuilders.post(REQUEST_PATH_FIND).param("subjectIds", "1,2,3"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockKeycloakUser(id = 12, username = "test", authorities = { "ROLE_ADMIN" })
    public void saveNewSubjectTest() throws Exception {
        mvc.perform(MockMvcRequestBuilders.post(REQUEST_PATH).accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JacksonUtils.serialize(AnimalSubjectModelUtil.createPreclinicalSubjectDto())))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockKeycloakUser(id = 12, username = "test", authorities = { "ROLE_ADMIN" })
    public void updateSubjectTest() throws Exception {
        mvc.perform(MockMvcRequestBuilders.put(REQUEST_PATH_WITH_ID).accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JacksonUtils.serialize(AnimalSubjectModelUtil.createAnimalSubjectDto())))
                .andExpect(status().isOk());
    }

}
