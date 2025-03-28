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

package org.shanoir.ng.preclinical.anesthetics.anesthetic_ingredients;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Iterator;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.shanoir.ng.ShanoirPreclinicalApplication;
import org.shanoir.ng.preclinical.anesthetics.ingredients.AnestheticIngredient;
import org.shanoir.ng.preclinical.anesthetics.ingredients.AnestheticIngredientRepository;
import org.shanoir.ng.utils.AnestheticModelUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

/**
 * Tests for repository 'anesthetic ingredients'.
 *
 * @author sloury
 *
 */

@DataJpaTest
@ActiveProfiles("test")
@ContextConfiguration(classes = ShanoirPreclinicalApplication.class)
public class AnestheticIngredientRepositoryTest {

    private static final Long INGREDIENT_TEST_1_ID = 1L;
    private static final String INGREDIENT_TEST_1_NAME = "Isoflurane";

    @Autowired
    private AnestheticIngredientRepository repository;

    @Test
    public void findAllTest() throws Exception {
        Iterable<AnestheticIngredient> ingredientsDb = repository.findAll();
        assertThat(ingredientsDb).isNotNull();
        int nbTemplates = 0;
        Iterator<AnestheticIngredient> ingredientsIt = ingredientsDb.iterator();
        while (ingredientsIt.hasNext()) {
            ingredientsIt.next();
            nbTemplates++;
        }
        assertThat(nbTemplates).isEqualTo(3);
    }

    @Test
    public void findAllByAnestheticTest() throws Exception {
        List<AnestheticIngredient> ingredientsDb = repository
                .findByAnesthetic(AnestheticModelUtil.createAnestheticGas());
        assertNotNull(ingredientsDb);
        assertThat(ingredientsDb.size()).isEqualTo(2);
        assertThat(ingredientsDb.get(0).getId()).isEqualTo(INGREDIENT_TEST_1_ID);
    }

    @Test
    public void findOneTest() throws Exception {
        AnestheticIngredient ingredientDb = repository.findById(INGREDIENT_TEST_1_ID).orElse(null);
        assertThat(ingredientDb.getName().getValue()).isEqualTo(INGREDIENT_TEST_1_NAME);
        assertThat(ingredientDb.getAnesthetic().getName()).isEqualTo(AnestheticModelUtil.ANESTHETIC_NAME);
    }

}
