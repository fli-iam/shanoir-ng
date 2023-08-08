package org.shanoir.ng.datasetacquisition.repository;

import java.util.List;

import org.apache.commons.math3.util.Pair;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DatasetAcquisitionRepositoryCustom {
	
	Page<DatasetAcquisition> findPageByStudyCenterOrStudyIdIn(Iterable<Pair<Long, Long>> studyCenterIds, Iterable<Long> studyIds, Pageable pageable);

	List<DatasetAcquisition> findByStudyCardIdAndStudyCenterOrStudyIdIn(Long studyCardId, Iterable<Pair<Long, Long>> studyCenters, Iterable<Long> studyIds);
}
