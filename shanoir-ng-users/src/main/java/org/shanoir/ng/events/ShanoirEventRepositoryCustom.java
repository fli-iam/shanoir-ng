package org.shanoir.ng.events;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ShanoirEventRepositoryCustom {

    Page<ShanoirEvent> findByStudyIdOrderByCreationDateDescAndSearch(Pageable pageable, Long studyId, String searchStr, String searchField);
}
