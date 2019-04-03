package org.shanoir.ng.shared.validation;

import org.shanoir.ng.shared.core.model.AbstractEntity;
import org.shanoir.ng.shared.error.FieldErrorMap;

public interface UniqueConstraintManager<T extends AbstractEntity> {

	public FieldErrorMap validate(final T entity);
	
}
