package org.shanoir.ng.preclinical.anesthetics.examination_anesthetics;

import java.util.List;

import org.shanoir.ng.preclinical.anesthetics.anesthetic.Anesthetic;
import org.springframework.data.repository.CrudRepository;

public interface ExaminationAnestheticRepository
		extends CrudRepository<ExaminationAnesthetic, Long>, ExaminationAnestheticRepositoryCustom {

	List<ExaminationAnesthetic> findByExaminationId(Long examinationId);

	List<ExaminationAnesthetic> findByAnesthetic(Anesthetic anesthetic);
}
