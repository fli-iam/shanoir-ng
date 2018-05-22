package org.shanoir.ng.preclinical.references;

import org.springframework.data.repository.CrudRepository;
import org.shanoir.ng.preclinical.references.Reference;

public interface RefsRepository extends CrudRepository<Reference, Long>, RefsRepositoryCustom{

}
