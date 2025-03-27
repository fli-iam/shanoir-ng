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

package org.shanoir.ng.center;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.shanoir.ng.center.controler.CenterApiController;
import org.shanoir.ng.center.dto.CenterDTO;
import org.shanoir.ng.center.dto.mapper.CenterMapper;
import org.shanoir.ng.center.model.Center;
import org.shanoir.ng.center.security.CenterFieldEditionSecurityManager;
import org.shanoir.ng.center.service.CenterService;
import org.shanoir.ng.center.service.CenterUniqueConstraintManager;
import org.shanoir.ng.shared.core.model.IdName;
import org.shanoir.ng.shared.error.FieldErrorMap;
import org.shanoir.ng.shared.event.ShanoirEventService;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.jackson.JacksonUtils;
import org.shanoir.ng.shared.security.ControlerSecurityService;
import org.shanoir.ng.study.service.StudyService;
import org.shanoir.ng.utils.ModelsUtil;
import org.shanoir.ng.utils.usermock.WithMockKeycloakUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

/**
 * Unit tests for center controller.
 *
 * @author msimon
 *
 */

@WebMvcTest(CenterApiController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
public class CenterApiControllerTest {

    private static final String REQUEST_PATH = "/centers";
    private static final String REQUEST_PATH_FOR_NAMES = REQUEST_PATH + "/names";
    private static final String REQUEST_PATH_WITH_ID = REQUEST_PATH + "/1";
    
    @Autowired
    private MockMvc mvc;

    @MockBean
    private CenterMapper centerMapperMock;

    @MockBean
    private CenterService centerServiceMock;

    @MockBean
    private StudyService studyServiceMock;
    
    @MockBean
    private CenterFieldEditionSecurityManager fieldEditionSecurityManager;
    
    @MockBean
    private CenterUniqueConstraintManager uniqueConstraintManager;

    @MockBean
    private ShanoirEventService eventService;
    
    @MockBean(name = "controlerSecurityService")
    private ControlerSecurityService controlerSecurityService;

    @BeforeEach
    public void setup() throws EntityNotFoundException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException  {
        given(centerMapperMock.centersToCenterDTOsFlat(Mockito.anyList()))
                .willReturn(Arrays.asList(new CenterDTO()));
        Center center = new Center();
        center.setId(Long.valueOf(123));
        IdName idNameCenter = new IdName(1L, "naIme");
        given(centerMapperMock.centerToCenterDTOFlat(Mockito.any(Center.class))).willReturn(new CenterDTO());
        doNothing().when(centerServiceMock).deleteById(1L);
        given(centerServiceMock.findAll()).willReturn(Arrays.asList(center));
        given(centerServiceMock.findById(1L)).willReturn(Optional.of(center));
        given(centerServiceMock.findIdsAndNames()).willReturn(Arrays.asList(idNameCenter));
        given(centerServiceMock.create(Mockito.any(Center.class))).willReturn(center);
        given(fieldEditionSecurityManager.validate(Mockito.any(Center.class))).willReturn(new FieldErrorMap());
        given(uniqueConstraintManager.validate(Mockito.any(Center.class))).willReturn(new FieldErrorMap());
        given(controlerSecurityService.idMatches(Mockito.anyLong(), Mockito.any(Center.class))).willReturn(true);
    }

    @Test
    @WithMockKeycloakUser(id = 12, username = "test", authorities = { "ROLE_ADMIN" })
    public void deleteCenterTest() throws Exception {
        mvc.perform(MockMvcRequestBuilders.delete(REQUEST_PATH_WITH_ID).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockKeycloakUser(id = 12, username = "test", authorities = { "ROLE_ADMIN" })
    public void deleteUnknownCenterTest() throws Exception {
        mvc.perform(MockMvcRequestBuilders.delete(REQUEST_PATH + "/0").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    public void findCenterByIdTest() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get(REQUEST_PATH_WITH_ID).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    public void findCentersTest() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get(REQUEST_PATH).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    public void findCentersNamesTest() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get(REQUEST_PATH_FOR_NAMES).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
    
    @Test
    @WithMockUser
    public void findCentersNamesByStudyIdTest() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get(REQUEST_PATH_FOR_NAMES + "/1").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockKeycloakUser(id = 12, username = "test", authorities = { "ROLE_ADMIN" })
    public void saveNewCenterTest() throws Exception {
        mvc.perform(MockMvcRequestBuilders.post(REQUEST_PATH).accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON).content(JacksonUtils.serialize(ModelsUtil.createCenter())))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockKeycloakUser(id = 12, username = "test", authorities = { "ROLE_ADMIN" })
    public void updateCenterTest() throws Exception {
        Center existingCenter = ModelsUtil.createCenter();
        existingCenter.setId(1L);
        mvc.perform(MockMvcRequestBuilders.put(REQUEST_PATH_WITH_ID).accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON).content(JacksonUtils.serialize(existingCenter)))
                .andExpect(status().isNoContent());
    }

}
