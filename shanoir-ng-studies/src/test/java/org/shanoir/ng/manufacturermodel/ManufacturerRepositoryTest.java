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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Iterator;

import org.junit.jupiter.api.Test;
import org.shanoir.ng.manufacturermodel.model.Manufacturer;
import org.shanoir.ng.manufacturermodel.repository.ManufacturerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Tests for repository 'manufacturer'.
 *
 * @author msimon
 *
 */

@DataJpaTest
@ActiveProfiles("test")
public class ManufacturerRepositoryTest {

    private static final Long MANUFACTURER_TEST_1_ID = 1L;
    private static final String MANUFACTURER_TEST_1_NAME = "GE Healthcare";
    
    @Autowired
    private ManufacturerRepository repository;
    
    @Test
    public void findAllTest() throws Exception {
        Iterable<Manufacturer> manufacturersDb = repository.findAll();
        assertThat(manufacturersDb).isNotNull();
        int nbManufacturers = 0;
        Iterator<Manufacturer> manufacturersIt = manufacturersDb.iterator();
        while (manufacturersIt.hasNext()) {
            manufacturersIt.next();
            nbManufacturers++;
        }
        assertThat(nbManufacturers).isEqualTo(3);
    }
    
    @Test
    public void findByIdTest() throws Exception {
        Manufacturer manufacturerDb = repository.findById(MANUFACTURER_TEST_1_ID).orElseThrow();
        assertThat(manufacturerDb).isNotNull();
        assertThat(manufacturerDb.getName()).isEqualTo(MANUFACTURER_TEST_1_NAME);
    }
    
}
