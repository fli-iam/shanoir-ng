package org.shanoir.ng.shared.exception;

import org.shanoir.ng.shared.error.FieldErrorMap;

/**
 * Exception thrown when trying to delete an entity with dependencies that cannot be deleted (cascade delete).
 * 
 * @author jlouis
 */
public class UndeletableDependenciesException extends ShanoirException {


	private static final long serialVersionUID = -4380502787558788081L;

	public UndeletableDependenciesException(FieldErrorMap errorMap) {
		super(errorMap);
	}

}
