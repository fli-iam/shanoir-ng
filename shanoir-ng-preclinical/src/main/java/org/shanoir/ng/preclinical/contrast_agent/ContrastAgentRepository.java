package org.shanoir.ng.preclinical.contrast_agent;

import java.util.Optional;

import org.shanoir.ng.preclinical.references.Reference;
import org.springframework.data.repository.CrudRepository;


public interface ContrastAgentRepository extends CrudRepository<ContrastAgent, Long>, ContrastAgentRepositoryCustom{

	Optional<ContrastAgent> findByProtocolId(Long protocolId);
	
	Optional<ContrastAgent> findByName(Reference name);
}
