package org.shanoir.ng.preclinical.anesthetics.anesthetic_ingredients;

import static org.mockito.BDDMockito.given;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.shanoir.ng.preclinical.anesthetics.ingredients.AnestheticIngredient;
import org.shanoir.ng.preclinical.anesthetics.ingredients.AnestheticIngredientRepository;
import org.shanoir.ng.preclinical.anesthetics.ingredients.AnestheticIngredientServiceImpl;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.utils.AnestheticModelUtil;
import org.shanoir.ng.utils.ReferenceModelUtil;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

/**
 * Anesthetic ingredients service test.
 * 
 * @author sloury
 * 
 */
@RunWith(MockitoJUnitRunner.class)
public class AnestheticIngredientServiceTest {

	private static final Long INGREDIENT_ID = 1L;
	private static final Double UPDATED_INGREDIENT_CONCENTRATION = 2.0;
	

	@Mock
	private AnestheticIngredientRepository ingredientsRepository;

	@Mock
	private RabbitTemplate rabbitTemplate;

	@InjectMocks
	private AnestheticIngredientServiceImpl ingredientsService;
	
			
	
	@Before
	public void setup() {
		given(ingredientsRepository.findAll()).willReturn(Arrays.asList(AnestheticModelUtil.createAnestheticIngredient()));
		given(ingredientsRepository.findByAnesthetic(AnestheticModelUtil.createAnestheticGas())).willReturn(Arrays.asList(AnestheticModelUtil.createAnestheticIngredient()));
		given(ingredientsRepository.findOne(INGREDIENT_ID)).willReturn(AnestheticModelUtil.createAnestheticIngredient());
		given(ingredientsRepository.save(Mockito.any(AnestheticIngredient.class))).willReturn(AnestheticModelUtil.createAnestheticIngredient());
	}

	@Test
	public void deleteByIdTest() throws ShanoirException {
		ingredientsService.deleteById(INGREDIENT_ID);

		Mockito.verify(ingredientsRepository, Mockito.times(1)).delete(Mockito.anyLong());
	}

	@Test
	public void findAllTest() {
		final List<AnestheticIngredient> ingredients = ingredientsService.findAll();
		Assert.assertNotNull(ingredients);
		Assert.assertTrue(ingredients.size() == 1);

		Mockito.verify(ingredientsRepository, Mockito.times(1)).findAll();
	}

	@Test
	public void findByIdTest() {
		final AnestheticIngredient ingredient = ingredientsService.findById(INGREDIENT_ID);
		Assert.assertNotNull(ingredient);
		Assert.assertTrue(ReferenceModelUtil.REFERENCE_INGREDIENT_ISOFLURANE_VALUE.equals(ingredient.getName().getValue()));

		Mockito.verify(ingredientsRepository, Mockito.times(1)).findOne(Mockito.anyLong());
	}
	
	@Test
	public void findByAnestheticTest() {
		final List<AnestheticIngredient> ingredients = ingredientsService.findByAnesthetic(AnestheticModelUtil.createAnestheticGas());
		Assert.assertNotNull(ingredients);
		Assert.assertTrue(ingredients.size() == 1);

		Mockito.verify(ingredientsRepository, Mockito.times(1)).findByAnesthetic(AnestheticModelUtil.createAnestheticGas());
	}
	
	

	@Test
	public void saveTest() throws ShanoirException {
		ingredientsService.save(createAnestheticIngredient());

		Mockito.verify(ingredientsRepository, Mockito.times(1)).save(Mockito.any(AnestheticIngredient.class));
	}

	@Test
	public void updateTest() throws ShanoirException {
		final AnestheticIngredient updatedIngredient = ingredientsService.update(createAnestheticIngredient());
		Assert.assertNotNull(updatedIngredient);
		Assert.assertTrue(UPDATED_INGREDIENT_CONCENTRATION.equals(updatedIngredient.getConcentration()));

		Mockito.verify(ingredientsRepository, Mockito.times(1)).save(Mockito.any(AnestheticIngredient.class));
	}

/*
	@Test
	public void updateFromShanoirOldTest() throws ShanoirException {
		pathologiesService.updateFromShanoirOld(createPathology());

		Mockito.verify(pathologiesRepository, Mockito.times(1)).findOne(Mockito.anyLong());
		Mockito.verify(pathologiesRepository, Mockito.times(1)).save(Mockito.any(Pathology.class));
	}
*/
	private AnestheticIngredient createAnestheticIngredient() {
		final AnestheticIngredient ingredient = new AnestheticIngredient();
		ingredient.setId(INGREDIENT_ID);
		ingredient.setName(ReferenceModelUtil.createReferenceAnestheticIngredientIsoflurane());
		ingredient.setAnesthetic(AnestheticModelUtil.createAnestheticGas());
		ingredient.setConcentration(UPDATED_INGREDIENT_CONCENTRATION);
		return ingredient;
	}
	
}
