package org.shanoir.ng.examination;

import org.springframework.data.repository.CrudRepository;

/**
 * Repository for examination.
 *
 * @author ifakhfakh
 */
public interface ExaminationRepository extends CrudRepository<Examination, Long>, ExaminationRepositoryCustom {

}
