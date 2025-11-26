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

package org.shanoir.ng.coil;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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
import org.shanoir.ng.coil.dto.mapper.CoilMapper;
import org.shanoir.ng.coil.model.Coil;
import org.shanoir.ng.coil.repository.CoilRepository;
import org.shanoir.ng.coil.service.CoilServiceImpl;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.utils.ModelsUtil;

/**
 * Coil service test.
 *
 * @author msimon
 *
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class CoilServiceTest {

    private static final Long COIL_ID = 1L;
    private static final String UPDATED_COIL_NAME = "test";

    @Mock
    private CoilMapper coilMapper;

    @Mock
    private CoilRepository coilRepository;

    @InjectMocks
    private CoilServiceImpl coilService;

    @BeforeEach
    public void setup() {
        given(coilRepository.findAll()).willReturn(Arrays.asList(ModelsUtil.createCoil()));
        given(coilRepository.findById(COIL_ID)).willReturn(Optional.of(ModelsUtil.createCoil()));
        given(coilRepository.save(Mockito.any(Coil.class))).willReturn(ModelsUtil.createCoil());
    }

    @Test
    public void deleteByBadIdTest() throws EntityNotFoundException {
        assertThrows(EntityNotFoundException.class, () -> {
            coilService.deleteById(2L);
        });
    }

    @Test
    public void deleteByIdTest() throws EntityNotFoundException {
        coilService.deleteById(COIL_ID);
        Mockito.verify(coilRepository, Mockito.times(1)).deleteById(Mockito.anyLong());
    }

    @Test
    public void findAllTest() {
        final List<Coil> coils = coilService.findAll();
        Assertions.assertNotNull(coils);
        Assertions.assertTrue(coils.size() == 1);

        Mockito.verify(coilRepository, Mockito.times(1)).findAll();
    }

    @Test
    public void findByIdTest() {
        final Coil coil = coilService.findById(COIL_ID).orElseThrow();
        Assertions.assertNotNull(coil);
        Assertions.assertTrue(ModelsUtil.COIL_NAME.equals(coil.getName()));

        Mockito.verify(coilRepository, Mockito.times(1)).findById(Mockito.anyLong());
    }

    @Test
    public void saveTest() {
        coilService.create(createCoil());
        Mockito.verify(coilRepository, Mockito.times(1)).save(Mockito.any(Coil.class));
    }

    @Test
    public void updateTest() throws EntityNotFoundException {
        final Coil coil = createCoil();
        final Coil updatedCoil = coilService.update(coil);
        Assertions.assertNotNull(updatedCoil);
        Assertions.assertTrue(UPDATED_COIL_NAME.equals(coil.getName()));
        Mockito.verify(coilRepository, Mockito.times(1)).save(Mockito.any(Coil.class));
    }

    private Coil createCoil() {
        final Coil coil = new Coil();
        coil.setId(COIL_ID);
        coil.setName(UPDATED_COIL_NAME);
        return coil;
    }

}
