package org.shanoir.ng.model.validation;

import java.util.List;

import org.shanoir.ng.model.AbstractGenericItem;

public interface UniqueCheckableService<T extends AbstractGenericItem> {

	List<T> findBy(String fieldName, Object value);

}
