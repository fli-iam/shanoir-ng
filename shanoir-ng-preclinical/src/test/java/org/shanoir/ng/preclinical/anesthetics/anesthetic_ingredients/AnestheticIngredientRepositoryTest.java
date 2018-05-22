package org.shanoir.ng.preclinical.anesthetics.anesthetic_ingredients;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;

import java.util.Iterator;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.shanoir.ng.ShanoirPreclinicalApplication;
import org.shanoir.ng.preclinical.anesthetics.ingredients.AnestheticIngredient;
import org.shanoir.ng.preclinical.anesthetics.ingredients.AnestheticIngredientRepository;
import org.shanoir.ng.utils.AnestheticModelUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import springfox.documentation.spring.web.plugins.DocumentationPluginsBootstrapper;
import springfox.documentation.spring.web.plugins.WebMvcRequestHandlerProvider;

/**
 * Tests for repository 'anesthetic ingredients'.
 * 
 * @author sloury
 *
 */
@RunWith(SpringRunner.class)
@DataJpaTest
@ActiveProfiles("test")
@ContextConfiguration(classes = ShanoirPreclinicalApplication.class)
public class AnestheticIngredientRepositoryTest {

	private static final Long INGREDIENT_TEST_1_ID = 1L;
	private static final String INGREDIENT_TEST_1_NAME = "Isoflurane";

	@Autowired
	private AnestheticIngredientRepository repository;

	/*
	 * Mocks used to avoid unsatisfied dependency exceptions.
	 */
	@MockBean
	private AuthenticationManager authenticationManager;
	@MockBean
	private DocumentationPluginsBootstrapper documentationPluginsBootstrapper;
	@MockBean
	private WebMvcRequestHandlerProvider webMvcRequestHandlerProvider;

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
		AnestheticIngredient ingredientDb = repository.findOne(INGREDIENT_TEST_1_ID);
		assertThat(ingredientDb.getName().getValue()).isEqualTo(INGREDIENT_TEST_1_NAME);
		assertThat(ingredientDb.getAnesthetic().getName()).isEqualTo(AnestheticModelUtil.ANESTHETIC_NAME);
	}

}
