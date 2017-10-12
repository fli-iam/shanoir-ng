package org.shanoir.ng.examination;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

/**
 * Repository for examination.
 *
 * @author ifakhfakh
 */
public interface ExaminationRepository extends CrudRepository<Examination, Long>, ExaminationRepositoryCustom {

	/**
	 * Find template by data.
	 *
	 * @param data
	 *            data.
	 * @return a template.
	 */
	//Optional<Examination> findByData(String data);

}
