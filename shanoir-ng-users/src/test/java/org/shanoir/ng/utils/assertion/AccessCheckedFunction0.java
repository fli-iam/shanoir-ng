package org.shanoir.ng.utils.assertion;

import org.shanoir.ng.shared.exception.ShanoirException;
import org.springframework.security.access.AccessDeniedException;

@FunctionalInterface
public interface AccessCheckedFunction0 {
	void apply() throws AccessDeniedException, ShanoirException;
}