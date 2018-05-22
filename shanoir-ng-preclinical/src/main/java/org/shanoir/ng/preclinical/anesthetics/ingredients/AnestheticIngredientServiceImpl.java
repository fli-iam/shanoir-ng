package org.shanoir.ng.preclinical.anesthetics.ingredients;

import java.util.List;

import org.shanoir.ng.preclinical.anesthetics.anesthetic.Anesthetic;
import org.shanoir.ng.shared.exception.ShanoirPreclinicalException;
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
	public void deleteById(final Long id) throws ShanoirPreclinicalException {
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
	public AnestheticIngredient save(final AnestheticIngredient ingredient) throws ShanoirPreclinicalException {
		AnestheticIngredient savedIngredient = null;
		try {
			savedIngredient = ingredientsRepository.save(ingredient);
		} catch (DataIntegrityViolationException dive) {
			ShanoirPreclinicalException.logAndThrow(LOG, "Error while creating ingredient: " + dive.getMessage());
		}
		return savedIngredient;
	}

	@Override
	public AnestheticIngredient update(final AnestheticIngredient ingredient) throws ShanoirPreclinicalException {
		final AnestheticIngredient ingredientDb = ingredientsRepository.findOne(ingredient.getId());
		updateModelValues(ingredientDb, ingredient);
		try {
			ingredientsRepository.save(ingredientDb);
		} catch (Exception e) {
			ShanoirPreclinicalException.logAndThrow(LOG, "Error while updating ingredient: " + e.getMessage());
		}
		return ingredientDb;
	}

	

	private AnestheticIngredient updateModelValues(final AnestheticIngredient ingredientDb, final AnestheticIngredient ingredient) {
		//ingredientDb.setAnesthetic(ingredient.getAnesthetic());
		ingredientDb.setName(ingredient.getName());
		ingredientDb.setConcentration(ingredient.getConcentration());
		ingredientDb.setConcentrationUnit(ingredient.getConcentrationUnit());
		return ingredientDb;
	}

}
