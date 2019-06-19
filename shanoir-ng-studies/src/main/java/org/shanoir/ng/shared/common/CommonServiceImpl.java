package org.shanoir.ng.shared.common;

import org.shanoir.ng.center.model.Center;
import org.shanoir.ng.center.repository.CenterRepository;
import org.shanoir.ng.shared.core.model.IdName;
import org.shanoir.ng.study.model.Study;
import org.shanoir.ng.study.repository.StudyRepository;
import org.shanoir.ng.subject.model.Subject;
import org.shanoir.ng.subject.repository.SubjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Implementation of shared service.
 * 
 * @author ifakhfakh
 *
 */
@Service
public class CommonServiceImpl implements CommonService {

	@Autowired
	private StudyRepository studyRepository;

	@Autowired
	private SubjectRepository subjectRepository;

	@Autowired
	private CenterRepository centerRepository;

	@Override
	public CommonIdNamesDTO findByIds(final CommonIdsDTO commonIdsDTO) {
		CommonIdNamesDTO names = new CommonIdNamesDTO();
		if (commonIdsDTO.getStudyId() != null) {
			final Study study = studyRepository.findOne(commonIdsDTO.getStudyId());
			if (study != null) {
				names.setStudy(new IdName(commonIdsDTO.getStudyId(), study.getName()));
			}
		}
		if (commonIdsDTO.getCenterId() != null) {
			final Center center = centerRepository.findOne(commonIdsDTO.getCenterId());
			if (center != null) {
				names.setCenter(new IdName(commonIdsDTO.getCenterId(), center.getName()));
			}
		}
		if (commonIdsDTO.getSubjectId() != null) {
			final Subject subject = subjectRepository.findOne(commonIdsDTO.getSubjectId());
			if (subject != null) {
				names.setSubject(new IdName(commonIdsDTO.getSubjectId(), subject.getName()));
			}
		}

		return names;
	}

}
