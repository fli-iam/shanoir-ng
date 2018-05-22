package org.shanoir.ng.preclinical.anesthetics.anesthetic;

import java.util.List;


import org.springframework.data.repository.CrudRepository;


public interface AnestheticRepository extends CrudRepository<Anesthetic, Long>, AnestheticRepositoryCustom{

	List<Anesthetic> findAllByAnestheticType(AnestheticType type);
}
