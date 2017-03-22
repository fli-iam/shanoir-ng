package org.shanoir.ng.shared.validation;

import java.util.List;

import org.shanoir.ng.shared.model.AbstractGenericItem;

public interface UniqueCheckableService<T extends AbstractGenericItem> {

	List<T> findBy(String fieldName, Object value);

}
