package org.shanoir.ng.utils;

import org.shanoir.ng.preclinical.references.Reference;
import org.shanoir.ng.preclinical.subjects.AnimalSubject;

/**
 * Utility class for test. Generates subject.
 * 
 * @author sloury
 *
 */
public final class AnimalSubjectModelUtil {

	// References data
	public static final String REF_CATEGORY = "subject";
	public static final String REF_TYPE_SPECIE = "specie";
	public static final String REF_TYPE_STRAIN = "strain";
	public static final String REF_TYPE_STABULATION = "stabulation";
	public static final String REF_TYPE_BIOTYPE = "biotype";
	public static final String REF_TYPE_PROVIDER = "provider";
	public static final String REF_SPECIE_VALUE = "Rat";
	public static final String REF_STRAIN_VALUE = "Wistar";
	public static final String REF_STABULATION_VALUE = "Grenoble";
	public static final String REF_BIOTYPE_VALUE = "wild";
	public static final String REF_PROVIDER_VALUE = "Simon";
	public static final Long REF_ID_SPECIE = 1L;
	public static final Long REF_ID_STRAIN = 2L;
	public static final Long REF_ID_STABULATION = 3L;
	public static final Long REF_ID_BIOTYPE = 4L;
	public static final Long REF_ID_PROVIDER = 5L;

	// Subject data
	public static final Long SUBJECT_ID = 1L;

	/**
	 * Create animal subject.
	 * 
	 * @return animal subject.
	 */
	public static AnimalSubject createAnimalSubject() {
		final AnimalSubject subject = new AnimalSubject();
		subject.setId(SUBJECT_ID);
		subject.setSubjectId(SUBJECT_ID);
		subject.setSpecie(createSpecie());
		subject.setStrain(createStrain());
		subject.setStabulation(createStabulation());
		subject.setBiotype(createBiotype());
		subject.setProvider(createProvider());
		return subject;
	}

	public static Reference createSpecie() {
		final Reference reference = new Reference();
		reference.setId(REF_ID_SPECIE);
		reference.setCategory(REF_CATEGORY);
		reference.setReftype(REF_TYPE_SPECIE);
		reference.setValue(REF_SPECIE_VALUE);
		return reference;
	}

	public static Reference createStrain() {
		final Reference reference = new Reference();
		reference.setId(REF_ID_STRAIN);
		reference.setCategory(REF_CATEGORY);
		reference.setReftype(REF_TYPE_STRAIN);
		reference.setValue(REF_STRAIN_VALUE);
		return reference;
	}

	public static Reference createStabulation() {
		final Reference reference = new Reference();
		reference.setId(REF_ID_STABULATION);
		reference.setCategory(REF_CATEGORY);
		reference.setReftype(REF_TYPE_STABULATION);
		reference.setValue(REF_STABULATION_VALUE);
		return reference;
	}

	public static Reference createBiotype() {
		final Reference reference = new Reference();
		reference.setId(REF_ID_BIOTYPE);
		reference.setCategory(REF_CATEGORY);
		reference.setReftype(REF_TYPE_BIOTYPE);
		reference.setValue(REF_BIOTYPE_VALUE);
		return reference;
	}

	public static Reference createProvider() {
		final Reference reference = new Reference();
		reference.setId(REF_ID_PROVIDER);
		reference.setCategory(REF_CATEGORY);
		reference.setReftype(REF_TYPE_PROVIDER);
		reference.setValue(REF_PROVIDER_VALUE);
		return reference;
	}

}
