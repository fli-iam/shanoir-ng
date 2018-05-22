package org.shanoir.ng.utils;

import org.shanoir.ng.preclinical.references.Reference;

/**
 * Utility class for test.
 * Generates references.
 * 
 * @author sloury
 *
 */
public final class ReferenceModelUtil {

	// Reference data
	public static final Long REFERENCE_ID = 1L;
	public static final String REFERENCE_CATEGORY = "subject";
	public static final String REFERENCE_TYPE = "specie";
	public static final String REFERENCE_VALUE = "Rat";
	public static final String REFERENCE_VALUE_MOUSE = "Mouse";
	
	// Reference Location data
	public static final Long REFERENCE_LOCATION_ID = 14L;
	public static final String REFERENCE_LOCATION_CATEGORY = "location";
	public static final String REFERENCE_LOCATION_TYPE = "anatomy";
	public static final String REFERENCE_LOCATION_VALUE = "Brain";
	
	
	// Reference Contrast Agent type data
	public static final Long REFERENCE_AGENT_TYPE_ID = 34L;
	public static final String REFERENCE_AGENT_TYPE_CATEGORY = "contrastagent";
	public static final String REFERENCE_AGENT_TYPE_TYPE = "name";
	public static final String REFERENCE_AGENT_TYPE_VALUE = "Gadolinium";
	
	// Reference Contrast Agent type data
	public static final Long REFERENCE_INGREDIENT_ISOFLURANE_ID = 29L;
	public static final String REFERENCE_INGREDIENT_ISOFLURANE_CATEGORY = "anesthetic";
	public static final String REFERENCE_INGREDIENT_ISOFLURANE_TYPE = "ingredient";
	public static final String REFERENCE_INGREDIENT_ISOFLURANE_VALUE = "Isoflurane";
	
	/**
	 * Create a reference.
	 * 
	 * @return reference.
	 */
	public static Reference createReferenceSpecie() {
		final Reference reference = new Reference();
		reference.setId(REFERENCE_ID);
		reference.setCategory(REFERENCE_CATEGORY);
		reference.setReftype(REFERENCE_TYPE);
		reference.setValue(REFERENCE_VALUE);
		return reference;
	}
	public static Reference createReferenceSpecieMouse() {
		final Reference reference = new Reference();
		reference.setId(2L);
		reference.setCategory(REFERENCE_CATEGORY);
		reference.setReftype(REFERENCE_TYPE);
		reference.setValue(REFERENCE_VALUE_MOUSE);
		return reference;
	}
	
	/**
	 * Create a loaction.
	 * 
	 * @return location.
	 */
	public static Reference createReferenceLocation() {
		final Reference reference = new Reference();
		reference.setId(REFERENCE_LOCATION_ID);
		reference.setCategory(REFERENCE_LOCATION_CATEGORY);
		reference.setReftype(REFERENCE_LOCATION_TYPE);
		reference.setValue(REFERENCE_LOCATION_VALUE);
		return reference;
	}
	
	/**Create contrast agent name*/
	public static Reference createReferenceContrastAgentGado() {
		final Reference reference = new Reference();
		reference.setId(REFERENCE_AGENT_TYPE_ID);
		reference.setCategory(REFERENCE_AGENT_TYPE_CATEGORY);
		reference.setReftype(REFERENCE_AGENT_TYPE_TYPE);
		reference.setValue(REFERENCE_AGENT_TYPE_VALUE);
		return reference;
	}
	
	/**Create anesthetic ingredient*/
	public static Reference createReferenceAnestheticIngredientIsoflurane() {
		final Reference reference = new Reference();
		reference.setId(REFERENCE_INGREDIENT_ISOFLURANE_ID);
		reference.setCategory(REFERENCE_INGREDIENT_ISOFLURANE_CATEGORY);
		reference.setReftype(REFERENCE_INGREDIENT_ISOFLURANE_TYPE);
		reference.setValue(REFERENCE_INGREDIENT_ISOFLURANE_VALUE);
		return reference;
	}
	
	
}
