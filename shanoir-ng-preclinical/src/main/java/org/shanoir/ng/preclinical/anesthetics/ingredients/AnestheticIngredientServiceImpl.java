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

package org.shanoir.ng.preclinical.anesthetics.ingredients;

import java.util.List;

import org.shanoir.ng.preclinical.anesthetics.anesthetic.Anesthetic;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

/**
 * Anesthetic ingredients service implementation.
 * 
 * @author sloury
 *
 */
@Service
public class AnestheticIngredientServiceImpl implements AnestheticIngredientService {

	/**
	 * Logger
	 */
	private static final Logger LOG = LoggerFactory.getLogger(AnestheticIngredientServiceImpl.class);

	@Autowired
	private RabbitTemplate rabbitTemplate;

	@Autowired
	private AnestheticIngredientRepository ingredientsRepository;

	@Override
	public void deleteById(final Long id) throws ShanoirException {
		ingredientsRepository.delete(id);
	}

	@Override
	public List<AnestheticIngredient> findAll() {
		return Utils.toList(ingredientsRepository.findAll());
	}

	@Override
	public List<AnestheticIngredient> findByAnesthetic(Anesthetic anesthetic) {
		return Utils.toList(ingredientsRepository.findByAnesthetic(anesthetic));
	}

	@Override
	public List<AnestheticIngredient> findBy(final String fieldName, final Object value) {
		return ingredientsRepository.findBy(fieldName, value);
	}

	@Override
	public AnestheticIngredient findById(final Long id) {
		return ingredientsRepository.findOne(id);
	}

	@Override
	public AnestheticIngredient save(final AnestheticIngredient ingredient) throws ShanoirException {
		AnestheticIngredient savedIngredient = null;
		try {
			savedIngredient = ingredientsRepository.save(ingredient);
		} catch (DataIntegrityViolationException dive) {
			LOG.error("Error while creating ingredient:  ", dive);
			throw new ShanoirException("Error while creating ingredient:  ", dive);
		}
		return savedIngredient;
	}

	@Override
	public AnestheticIngredient update(final AnestheticIngredient ingredient) throws ShanoirException {
		final AnestheticIngredient ingredientDb = ingredientsRepository.findOne(ingredient.getId());
		updateModelValues(ingredientDb, ingredient);
		try {
			ingredientsRepository.save(ingredientDb);
		} catch (Exception e) {
			LOG.error("Error while updating ingredient:  ", e);
			throw new ShanoirException("Error while updating ingredient:  ", e);
		}
		return ingredientDb;
	}

	private AnestheticIngredient updateModelValues(final AnestheticIngredient ingredientDb,
			final AnestheticIngredient ingredient) {
		// ingredientDb.setAnesthetic(ingredient.getAnesthetic());
		ingredientDb.setName(ingredient.getName());
		ingredientDb.setConcentration(ingredient.getConcentration());
		ingredientDb.setConcentrationUnit(ingredient.getConcentrationUnit());
		return ingredientDb;
	}

}
