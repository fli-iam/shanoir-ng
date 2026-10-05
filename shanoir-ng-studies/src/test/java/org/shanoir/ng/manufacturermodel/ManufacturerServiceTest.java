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

package org.shanoir.ng.manufacturermodel;

import static org.mockito.BDDMockito.given;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.shanoir.ng.acquisitionequipment.repository.AcquisitionEquipmentRepository;
import org.shanoir.ng.manufacturermodel.model.Manufacturer;
import org.shanoir.ng.manufacturermodel.repository.ManufacturerModelRepository;
import org.shanoir.ng.manufacturermodel.repository.ManufacturerRepository;
import org.shanoir.ng.manufacturermodel.service.ManufacturerServiceImpl;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
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
 * Manufacturer service test.
 *
 * @author msimon
 *
 */
@SpringBootTest
@ActiveProfiles("test")
public class ManufacturerServiceTest {

    private static final Long MANUFACTURER_ID = 1L;

    private static final String UPDATED_MANUFACTURER_NAME = "test";

    @MockitoBean
    private ManufacturerRepository repository;

    @MockitoBean
    private RabbitTemplate rabbitTemplate;

    @MockitoBean
    private ManufacturerModelRepository manufacturerModelRepository;

    @MockitoBean
    private AcquisitionEquipmentRepository acquisitionEquipmentRepository;

    @MockitoBean
    private JsonMapper jsonMapper;

    @MockitoBean
    private ConnectionFactory connectionFactory;

    @Autowired
    private ManufacturerServiceImpl manufacturerService;

    @BeforeEach
    public void setup() {
        given(repository.findAll()).willReturn(Arrays.asList(ModelsUtil.createManufacturer()));
        given(repository.findById(MANUFACTURER_ID)).willReturn(Optional.of(ModelsUtil.createManufacturer()));
        given(repository.save(Mockito.any(Manufacturer.class))).willReturn(createManufacturer());
    }


    @Test
    @WithMockKeycloakUser(id = 3, username = "jlouis", authorities = { "ROLE_EXPERT" })
    public void findAllTest() {
        final List<Manufacturer> manufacturers = manufacturerService.findAll();
        Assertions.assertNotNull(manufacturers);
        Assertions.assertTrue(manufacturers.size() == 1);
        Mockito.verify(repository, Mockito.times(1)).findAll();
    }

    @Test
    @WithMockKeycloakUser(id = 3, username = "jlouis", authorities = { "ROLE_EXPERT" })
    public void findByIdTest() {
        final Manufacturer manufacturer = manufacturerService.findById(MANUFACTURER_ID).orElseThrow();
        Assertions.assertNotNull(manufacturer);
        Assertions.assertTrue(ModelsUtil.MANUFACTURER_NAME.equals(manufacturer.getName()));

        Mockito.verify(repository, Mockito.times(1)).findById(Mockito.anyLong());
    }

    @Test
    @WithMockKeycloakUser(id = 3, username = "jlouis", authorities = { "ROLE_EXPERT" })
    public void saveTest() {
        Manufacturer manufacturer = createManufacturer();
        manufacturer.setId(null);
        manufacturerService.create(manufacturer);
        Mockito.verify(repository, Mockito.times(1)).save(Mockito.any(Manufacturer.class));
    }

    @Test
    @WithMockKeycloakUser(id = 3, username = "jlouis", authorities = { "ROLE_EXPERT" })
    public void updateTest() throws EntityNotFoundException {
        final Manufacturer manufacturer = createManufacturer();
        final Manufacturer updatedManufacturer = manufacturerService.update(createManufacturer());
        Assertions.assertNotNull(updatedManufacturer);
        Assertions.assertTrue(UPDATED_MANUFACTURER_NAME.equals(updatedManufacturer.getName()));
        Mockito.verify(repository, Mockito.times(1)).save(Mockito.any(Manufacturer.class));
    }

    private Manufacturer createManufacturer() {
        final Manufacturer manufacturer = new Manufacturer();
        manufacturer.setId(MANUFACTURER_ID);
        manufacturer.setName(UPDATED_MANUFACTURER_NAME);
        return manufacturer;
    }
}
