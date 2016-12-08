package org.shanoir.ng.model.validation;

import java.util.List;

public interface UniqueCheckableService<T> {

	List<T> findBy(String fieldName, Object value);

}
