package org.shanoir.ng.datasetacquisition.repository;

import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.apache.commons.math3.util.Pair;

public interface DatasetAcquisitionRepositoryCustom {
	
	Page<DatasetAcquisition> findPageByStudyCenterOrStudyIdIn(Iterable<Pair<Long, Long>> studyCenterIds, Iterable<Long> studyIds, Pageable pageable);

}
