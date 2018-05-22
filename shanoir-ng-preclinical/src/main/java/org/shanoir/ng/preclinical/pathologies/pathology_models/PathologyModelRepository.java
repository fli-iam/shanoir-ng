package org.shanoir.ng.preclinical.pathologies.pathology_models;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;


public interface PathologyModelRepository extends CrudRepository<PathologyModel, Long>, PathologyModelRepositoryCustom{

	Optional<PathologyModel> findByName(String name);
}
