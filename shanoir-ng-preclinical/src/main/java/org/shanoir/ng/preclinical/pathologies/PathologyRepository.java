package org.shanoir.ng.preclinical.pathologies;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;


public interface PathologyRepository extends CrudRepository<Pathology, Long>, PathologyRepositoryCustom{

	Optional<Pathology> findByName(String name);
}
