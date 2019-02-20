package org.shanoir.ng.utils.assertion;

import org.shanoir.ng.shared.exception.ShanoirException;
import org.springframework.security.access.AccessDeniedException;

@FunctionalInterface
public interface AccessCheckedFunction1Arg<T> {
	void apply(T t) throws AccessDeniedException, ShanoirException;
}