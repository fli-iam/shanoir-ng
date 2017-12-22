package org.shanoir.ng.shared.exception;

/**
 * List of error codes for current microservice.
 * 
 * @author msimon
 *
 */
public class DatasetsErrorModelCode extends ErrorModelCode {

	/** No study found */
	public static final Integer STUDY_NOT_FOUND = 201;

	/** No center found */
	public static final Integer CENTER_NOT_FOUND = 211;

	/** No subject found */
	public static final Integer SUBJECT_NOT_FOUND = 291;

	/** No dataset found */
	public static final Integer DATASET_NOT_FOUND = 292;

}
