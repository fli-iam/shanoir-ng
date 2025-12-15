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

package org.shanoir.ng.acquisitionequipment;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.shanoir.ng.acquisitionequipment.model.AcquisitionEquipment;
import org.shanoir.ng.acquisitionequipment.repository.AcquisitionEquipmentRepository;
import org.shanoir.ng.acquisitionequipment.service.AcquisitionEquipmentServiceImpl;
import org.shanoir.ng.center.model.Center;
import org.shanoir.ng.manufacturermodel.model.Manufacturer;
import org.shanoir.ng.manufacturermodel.model.ManufacturerModel;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.utils.ModelsUtil;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

/**
 * Acquisition equipment service test.
 *
 * @author msimon
 *
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AcquisitionEquipmentServiceTest {

    private static final Long ACQ_EQPT_ID = 1L;
    private static final String UPDATED_ACQ_EQPT_SERIAL_NUMBER = "test";
    private static final String MANUFACTURER_MODEL_NAME = "test";
    private static final String MANUFACTURER_NAME = "test";

    @Mock
    private AcquisitionEquipmentRepository repository;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private AcquisitionEquipmentServiceImpl acquisitionEquipmentService;

    @BeforeEach
    public void setup() {
        given(repository.findAll()).willReturn(Arrays.asList(ModelsUtil.createAcquisitionEquipment()));
        given(repository.findById(ACQ_EQPT_ID)).willReturn(Optional.of(ModelsUtil.createAcquisitionEquipment()));
        given(repository.save(Mockito.any(AcquisitionEquipment.class))).willReturn(createAcquisitionEquipment());
    }

    @Test
    public void deleteByBadIdTest() throws EntityNotFoundException  {
        assertThrows(EntityNotFoundException.class, () -> {
            acquisitionEquipmentService.deleteById(2L);
        });
    }

    @Test
    public void deleteByIdTest() throws EntityNotFoundException {
        acquisitionEquipmentService.deleteById(ACQ_EQPT_ID);

        Mockito.verify(repository, Mockito.times(1)).deleteById(Mockito.anyLong());
    }

    @Test
    public void findAllTest() {
        final List<AcquisitionEquipment> equipments = acquisitionEquipmentService.findAll();

        Assertions.assertNotNull(equipments);
        Assertions.assertTrue(equipments.size() == 1);
        Mockito.verify(repository, Mockito.times(1)).findAll();
    }

    @Test
    public void findByIdTest() {
        final AcquisitionEquipment equipment = acquisitionEquipmentService.findById(ACQ_EQPT_ID).orElse(null);
        Assertions.assertNotNull(equipment);
        Assertions.assertTrue(ModelsUtil.ACQ_EQPT_SERIAL_NUMBER.equals(equipment.getSerialNumber()));
        Mockito.verify(repository, Mockito.times(1)).findById(Mockito.anyLong());
    }

    @Test
    public void saveTest() {
        acquisitionEquipmentService.create(createAcquisitionEquipment());

        Mockito.verify(repository, Mockito.times(1)).save(Mockito.any(AcquisitionEquipment.class));
    }

    @Test
    public void updateTest() throws EntityNotFoundException {
        final AcquisitionEquipment updatedEquipment = acquisitionEquipmentService.update(createAcquisitionEquipment());
        Assertions.assertNotNull(updatedEquipment);
        Assertions.assertTrue(UPDATED_ACQ_EQPT_SERIAL_NUMBER.equals(updatedEquipment.getSerialNumber()));
        Mockito.verify(repository, Mockito.times(1)).save(Mockito.any(AcquisitionEquipment.class));
    }

    private AcquisitionEquipment createAcquisitionEquipment() {
        final AcquisitionEquipment equipment = new AcquisitionEquipment();
        equipment.setId(ACQ_EQPT_ID);
        equipment.setSerialNumber(UPDATED_ACQ_EQPT_SERIAL_NUMBER);
        final ManufacturerModel model = new ManufacturerModel();
        model.setName(MANUFACTURER_MODEL_NAME);
        final Manufacturer manu = new Manufacturer();
        manu.setName(MANUFACTURER_NAME);
        model.setManufacturer(manu);
        equipment.setManufacturerModel(model);
        Center center = new Center();
        center.setName("name");
        center.setId(1L);
        equipment.setCenter(center);
        return equipment;
    }

}
