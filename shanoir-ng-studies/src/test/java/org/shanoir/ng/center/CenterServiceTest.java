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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.shanoir.ng.center.dto.mapper.CenterMapper;
import org.shanoir.ng.center.model.Center;
import org.shanoir.ng.center.repository.CenterRepository;
import org.shanoir.ng.center.service.CenterServiceImpl;
import org.shanoir.ng.shared.core.model.IdName;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.studycenter.StudyCenter;
import org.shanoir.ng.utils.ModelsUtil;
import org.shanoir.ng.utils.usermock.WithMockKeycloakUser;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import tools.jackson.databind.json.JsonMapper;

/**
 * Center service test.
 *
 * @author msimon
 *
 */
@SpringBootTest
@ActiveProfiles("test")
public class CenterServiceTest {

    private static final Long CENTER_ID = 1L;

    private static final String UPDATED_CENTER_NAME = "test";

    @MockitoBean
    private CenterMapper centerMapper;

    @MockitoBean
    private CenterRepository centerRepository;

    @MockitoBean
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private CenterServiceImpl centerService;

    @MockitoBean
    private JsonMapper jsonMapper;

    @MockitoBean
    private ConnectionFactory connectionFactory;

    @BeforeEach
    public void setup() {
        given(centerRepository.findAll()).willReturn(Arrays.asList(ModelsUtil.createCenter()));
        given(centerRepository.findIdsAndNames()).willReturn(Arrays.asList(new IdName()));
        given(centerRepository.findById(CENTER_ID)).willReturn(Optional.of(ModelsUtil.createCenter()));
        given(centerRepository.save(Mockito.any(Center.class))).willReturn(createCenter());
    }

    @Test
    @WithMockKeycloakUser(id = 3, username = "jlouis", authorities = { "ROLE_ADMIN" })
    public void deleteByBadIdTest() throws EntityNotFoundException {
        assertThrows(EntityNotFoundException.class, () -> {
            centerService.deleteById(2L);
        });
    }

    @Test
    @WithMockKeycloakUser(id = 3, username = "jlouis", authorities = { "ROLE_ADMIN" })
    public void deleteByIdTest() throws EntityNotFoundException {
        centerService.deleteById(CENTER_ID);
        Mockito.verify(centerRepository, Mockito.times(1)).deleteById(Mockito.anyLong());
    }

    @Test
    @WithMockKeycloakUser(id = 3, username = "jlouis", authorities = { "ROLE_ADMIN" })
    public void deleteByIdWithAcquisitionEquipmentTest() throws EntityNotFoundException {
        final Center center = ModelsUtil.createCenter();
        center.getAcquisitionEquipments().add(ModelsUtil.createAcquisitionEquipment());
        given(centerRepository.findById(CENTER_ID)).willReturn(Optional.of(center));
        centerService.deleteById(CENTER_ID);
    }

    @Test
    @WithMockKeycloakUser(id = 3, username = "jlouis", authorities = { "ROLE_ADMIN" })
    public void deleteByIdWithStudyTest() throws EntityNotFoundException {
        final Center center = ModelsUtil.createCenter();
        center.getStudyCenterList().add(new StudyCenter());
        given(centerRepository.findById(CENTER_ID)).willReturn(Optional.of(center));
        centerService.deleteById(CENTER_ID);
    }

    @Test
    @WithMockKeycloakUser(id = 3, username = "jlouis", authorities = { "ROLE_ADMIN" })
    public void findAllTest() {
        final List<Center> centers = centerService.findAll();
        Assertions.assertNotNull(centers);
        Assertions.assertTrue(centers.size() == 1);
        Mockito.verify(centerRepository, Mockito.times(1)).findAll();
    }

    @Test
    @WithMockKeycloakUser(id = 3, username = "jlouis", authorities = { "ROLE_ADMIN" })
    public void findByIdTest() {
        final Center center = centerService.findById(CENTER_ID).orElse(null);
        Assertions.assertNotNull(center);
        Assertions.assertTrue(ModelsUtil.CENTER_NAME.equals(center.getName()));
        Mockito.verify(centerRepository, Mockito.times(1)).findById(Mockito.anyLong());
    }

    @Test
    @WithMockKeycloakUser(id = 3, username = "jlouis", authorities = { "ROLE_ADMIN" })
    public void findIdsAndNamesTest() {
        final List<IdName> centers = centerService.findIdsAndNames();
        Assertions.assertNotNull(centers);
        Assertions.assertTrue(centers.size() == 1);
        Mockito.verify(centerRepository, Mockito.times(1)).findIdsAndNames();
    }

    @Test
    @WithMockKeycloakUser(id = 3, username = "jlouis", authorities = { "ROLE_ADMIN" })
    public void saveTest() {
        Center center = createCenter();
        center.setId(null);
        centerService.create(center, true);
        Mockito.verify(centerRepository, Mockito.times(1)).save(Mockito.any(Center.class));
    }

    @Test
    @WithMockKeycloakUser(id = 3, username = "jlouis", authorities = { "ROLE_ADMIN" })
    public void updateTest() throws EntityNotFoundException {
        final Center updatedCenter = centerService.update(createCenter(), true);
        Assertions.assertNotNull(updatedCenter);
        Assertions.assertTrue(UPDATED_CENTER_NAME.equals(updatedCenter.getName()));

        Mockito.verify(centerRepository, Mockito.times(1)).save(Mockito.any(Center.class));
    }

    private Center createCenter() {
        final Center center = new Center();
        center.setId(CENTER_ID);
        center.setName(UPDATED_CENTER_NAME);
        return center;
    }

}
