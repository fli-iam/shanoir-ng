package org.shanoir.ng.preclinical.extra_data;

import org.shanoir.ng.preclinical.extra_data.examination_extra_data.ExaminationExtraData;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface ExtraDataBaseRepository<T extends ExaminationExtraData> extends CrudRepository<T, Long>, ExtraDataRepositoryCustom{

 
}