package org.shanoir.ng.shared.paging;

import java.util.Collection;

public interface Page<T> extends org.springframework.data.domain.Page<T>, Collection<T> {

}