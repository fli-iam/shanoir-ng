package org.shanoir.ng.utils.assertion;

import org.shanoir.ng.shared.exception.ShanoirException;
import org.springframework.security.access.AccessDeniedException;

@FunctionalInterface
public interface AccessCheckedFunction2Arg<T, U> {
	void apply(T t, U u) throws AccessDeniedException, ShanoirException;
}