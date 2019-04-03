package org.shanoir.ng.shared.security;

import org.shanoir.ng.shared.core.model.AbstractEntity;
import org.shanoir.ng.shared.error.FieldErrorMap;
import org.springframework.stereotype.Service;

@Service
public interface FieldEditionSecurityManager<T extends AbstractEntity> {

	public FieldErrorMap validate(final T entity);
	
}
