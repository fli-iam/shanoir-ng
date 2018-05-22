package org.shanoir.ng.utils;

import org.shanoir.ng.preclinical.anesthetics.anesthetic.Anesthetic;
import org.shanoir.ng.preclinical.anesthetics.anesthetic.AnestheticType;
import org.shanoir.ng.preclinical.anesthetics.examination_anesthetics.ExaminationAnesthetic;
import org.shanoir.ng.preclinical.anesthetics.ingredients.AnestheticIngredient;

/**
 * Utility class for test.
 * Generates anesthetic
 * 
 * @author sloury
 *
 */
public final class AnestheticModelUtil {

	// Anesthetic data
	public static final Long ANESTHETIC_ID = 1L;
	public static final String ANESTHETIC_NAME = "Gas Iso. 2% Ket. 25%";
	// Anesthetic 2 data
	public static final Long ANESTHETIC_3_ID = 3L;
	public static final String ANESTHETIC_3_NAME = "Injection Iso. 5%";
	// Ingredient data
	public static final Long INGREDIENT_ID = 1L;
	
	// Examination anesthetic data
	public static final Long EXAM_ANESTHETIC_ID = 1L;
	public static final Long EXAM_ID = 1L;
	
	/**
	 * Create an anesthetic.
	 * 
	 * @return anesthetic.
	 */
	public static Anesthetic createAnestheticGas() {
		Anesthetic anesthetic = new Anesthetic();
		anesthetic.setId(ANESTHETIC_ID);
		anesthetic.setName(ANESTHETIC_NAME);
		anesthetic.setAnestheticType(AnestheticType.GAS);
		return anesthetic;
	}
	public static Anesthetic createAnestheticInjection() {
		Anesthetic anesthetic = new Anesthetic();
		anesthetic.setId(ANESTHETIC_3_ID);
		anesthetic.setName(ANESTHETIC_3_NAME);
		anesthetic.setAnestheticType(AnestheticType.INJECTION);
		return anesthetic;
	}
	
	/**
	 * Create an anesthetic ingredient.
	 * 
	 * @return anesthetic ingredient.
	 */
	public static AnestheticIngredient createAnestheticIngredient() {
		AnestheticIngredient ingredient = new AnestheticIngredient();
		ingredient.setId(INGREDIENT_ID);
		ingredient.setName(ReferenceModelUtil.createReferenceAnestheticIngredientIsoflurane());
		ingredient.setAnesthetic(createAnestheticGas());
		return ingredient;
	}
	
	/**
	 * Create an examination anesthetic
	 * 
	 * @return examination anesthetic
	 */
	public static ExaminationAnesthetic createExaminationAnesthetic() {
		ExaminationAnesthetic examAnesthetic = new ExaminationAnesthetic();
		examAnesthetic.setId(EXAM_ANESTHETIC_ID);
		//examAnesthetic.setExamination(ExaminationModelUtil.createExamination());
		examAnesthetic.setAnesthetic(createAnestheticGas());
		return examAnesthetic;
	}
	
	
}
