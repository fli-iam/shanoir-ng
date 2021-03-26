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

package org.shanoir.ng.utils;

import org.shanoir.ng.preclinical.pathologies.Pathology;
import org.shanoir.ng.preclinical.pathologies.pathology_models.PathologyModel;
import org.shanoir.ng.preclinical.pathologies.subject_pathologies.SubjectPathology;

/**
 * Utility class for test. Generates pathology.
 * 
 * @author sloury
 *
 */
public final class PathologyModelUtil {

	// Pathology data
	public static final Long PATHOLOGY_ID = 1L;
	public static final String PATHOLOGY_NAME = "Stroke";
	public static final Long PATHOLOGY_CANCER_ID = 2L;
	public static final String PATHOLOGY_CANCER_NAME = "Cancer";
	public static final Long MODEL_ID = 1L;
	public static final String MODEL_NAME = "U836";

	/**
	 * Create a pathology.
	 * 
	 * @return pathology.
	 */
	public static Pathology createPathology() {
		Pathology pathology = new Pathology();
		pathology.setId(PATHOLOGY_ID);
		pathology.setName(PATHOLOGY_NAME);
		return pathology;
	}

	public static Pathology createPathologyCancer() {
		Pathology pathology = new Pathology();
		pathology.setId(PATHOLOGY_CANCER_ID);
		pathology.setName(PATHOLOGY_CANCER_NAME);
		return pathology;
	}

	/**
	 * Create a pathology model.
	 * 
	 * @return pathology model.
	 */
	public static PathologyModel createPathologyModel() {
		PathologyModel model = new PathologyModel();
		model.setId(MODEL_ID);
		model.setName(MODEL_NAME);
		model.setPathology(createPathology());
		return model;
	}

	/**
	 * Create a subject pathology
	 * 
	 * @return subject pathology.
	 */
	public static SubjectPathology createSubjectPathology() {
		SubjectPathology spatho = new SubjectPathology();
		spatho.setLocation(ReferenceModelUtil.createReferenceLocation());
		spatho.setPathology(createPathology());
		spatho.setPathologyModel(createPathologyModel());
		spatho.setAnimalSubject(AnimalSubjectModelUtil.createAnimalSubject());
		return spatho;
	}

}
