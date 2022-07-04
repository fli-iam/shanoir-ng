package org.shanoir.ng.datasetacquisition.repository;

import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.mysql.cj.conf.ConnectionUrlParser.Pair;

public interface DatasetAcquisitionRepositoryCustom {
	
	Page<DatasetAcquisition> findByExaminationByStudyCenterOrStudyIdIn(Iterable<Pair<Long, Long>> studyCenterIds, Iterable<Long> studyIds, Pageable pageable);

}
