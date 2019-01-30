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

package org.shanoir.ng.shared.util;

/**
 * Constants definition class.
 *
 * @author aferial
 */
public final class ShanoirConstants {

//	public static enum DATE_EQUALITY_TYPE {
//		BETWEEN, EQUALS
//	}

	/**
	 * The Enum DICOM_RETURNED_TYPES.
	 */
	public static enum DICOM_RETURNED_TYPES {

		/** The BYT e_ ARRAY. */
		BYTE_ARRAY,
		/** The DATE. */
		DATE,
		/** The DAT e_ ARRAY. */
		DATE_ARRAY,
		/** The DAT e_ RANGE. */
		DATE_RANGE,
		/** The DOUBLE. */
		DOUBLE,
		/** The DOUBL e_ ARRAY. */
		DOUBLE_ARRAY,
		/** The FLOAT. */
		FLOAT,
		/** The FLOA t_ ARRAY. */
		FLOAT_ARRAY,
		/** The INT. */
		INT,
		/** The IN t_ ARRAY. */
		INT_ARRAY,
		/** The SHOR t_ ARRAY. */
		SHORT_ARRAY,
		/** The STRING. */
		STRING,
		/** The STRIN g_ ARRAY. */
		STRING_ARRAY
	}

//	public static enum EJBQL_TYPE {
//		CART, DATA_OF, RESULT_OF
//	}
//
//	/** Behavioural Examination type. *//*
//	public static final String BEHAVIOURAL_EXAMINATION_TYPE = "Behavioural";*/
//
//	/** Coded Score type. */
//	public static final String CODED_SCORE_TYPE = "Coded";
//
//	/** Coded Variable type. */
//	public static final String CODED_VARIABLE_TYPE = CODED_SCORE_TYPE;
//
//	/** Create string. */
//	public static final String CREATE = "create";
//
//	/** Select an experimental group for import process. */
//	public static final String GROUP_OF_SUBJECTS = "groupOfSubjects";
//
//	/** the name of the folder that contains the images used in this project. */
//	public static final String IMAGES_FOLDER_NAME = "img";
//
//	/** Mr Examination type. *//*
//	public static final String MR_EXAMINATION_TYPE = "MR";
//*/
//	/** Mr Modality. */
//	public static final String MR_MODALITY = "MR";
//
//	/** Neuro Clinical Examination type. *//*
//	public static final String NEURO_CLINICAL_EXAMINATION_TYPE = "NeuroClinical";*/
//
//	/** Neuro psychological Examination type. *//*
//	public static final String NEURO_PSYCHOLOGICAL_EXAMINATION_TYPE = "NeuroPsychological";*/
//
//	/** Nifti type. */
//	public static final String NIFTI = "Nifti";
//
//	/** Numerical Score type. */
//	public static final String NUMERICAL_SCORE_TYPE = "Numerical";
//
//	/** Numerical Variable type. */
//	public static final String NUMERICAL_VARIABLE_TYPE = NUMERICAL_SCORE_TYPE;
//
//	/** Pacs type. */
//	public static final String PACS = "Pacs";
//
//	/** Password Hash length. */
//	public static final int PASSWORD_HASH_LENGTH = 14;
//
//	/** Password Regular Expression. */
//	public static final String PASSWORD_REGEX = "((?=.*\\d)(?=.*[a-zA-Z])(?=.*[@#$%]).{6,20})";
//	//public static final String PASSWORD_REGEX = "{4,15}";
//
//	/** Project name. */
//	public static final String PROJECT_NAME = "Shanoir";
//
//	/** Questionnaire Based Assessment type. */
//	public static final String QUESTIONNAIRE_BASED_INTERVIEW_TYPE = "Questionnaire";
//
//	/** The Constant RAW. */
//	public static final String RAW = "Raw";
//
//	/** Select string. */
//	public static final String SELECT = "select";
//
//	/** Select a subject from the list of subjects of the research study. */
//	public static final String SELECT_STUDY_SUBJECT = "selectStudySubject";
//
//	/** Shanoir Home file Path. */
//	public static final String SHANOIR_HOME = System.getenv("SHANOIR_HOME");
//
//	/** Select a subject group for import process. */
//	public static final String SUBJECT = "subject";
//
//	/** Prefix for the temporary folder for the query/retrieve. */
//	public static final String TEMP_FOLDER_QUERY_RETRIEVE_PREFIX = "tmpQueryRetrieve";
//
//	/**
//	 * the name of the temporary folder that contains the temporary images used
//	 * in this project.
//	 */
//	public static final String TEMPORARY_IMAGES_FOLDER_NAME = "temp";
//
//	/** Test Based Assessment type. */
//	public static final String TEST_BASED_ASSESSMENT_TYPE = "Test";
//
//	/** Time pattern. */
//	public static String TIME_PATTERN = "HH'h'mm'm'ss's'";
//
//	/** Time pattern for file system. */
//	public static String TIME_PATTERN_FILE_SYSTEM = "yyyy_MM_dd_HH_mm_ss_SSS";
//
//	/** In order to preview the DICOM files to be imported, tmp dcm and png files are generated in a folder*/
//	public final static String tmpImageFolderPrefix = "tmpImageFolder";
//
//	public final static String tempDicomImgSuffix = "tempDicomImg";
//
//	public final static String tempPngImgSuffix = "tempPngImg";
//
//	public final static String GENERATE_CREATE_STUDY_TIMEPOINT = "generateOrCreateTimepoint";
//	public final static String GENERATE_STUDY_TIMEPOINT = "generateTimepoint";
//	public final static String CREATE_STUDY_TIMEPOINT = "createTimepoint";
//
//	public final static String TIMEPOINT_BEFORE = "before";
//	public final static String TIMEPOINT_AFTER = "after";
//
//	public final static String EQUAL_TO = "equal";
//	public final static String GREATER_THAN = "greater";
//	public final static String LOWER_THAN = "lower";
//
//	public final static String FIRST = "first";
//	public final static String PREVIOUS = "previous";
//	public final static String NEXT = "next";
//
//	public final static String SEARCH_ANY = "any";
//
//	/**Constants used to define extra file data type uploaded along examination*/
//	public final static String EXTRA_DATA_PHYSIOLOGICAL = "physiological";
//	public final static String EXTRA_DATA_BLOODGAS = "bloodgas";
//
//	/**Constants used to define nifti converters names*/
//	public final static String DCM2NII = "dcm2nii";
//	public final static String MCVERTER = "mcverter";
//	public final static String CLIDCM = "clidcm";
//	public final static String DICOM2NIFTI = "dicom2nifti";

	/**
	 * Hiding the constructor.
	 */
	private ShanoirConstants() {

	}
}
