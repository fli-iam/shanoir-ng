package org.shanoir.ng.shared.api;

import org.shanoir.ng.center.Center;
import org.shanoir.ng.center.CenterRepository;
import org.shanoir.ng.study.Study;
import org.shanoir.ng.study.StudyRepository;
import org.shanoir.ng.study.dto.StudySubjectCenterNamesDTO;
import org.shanoir.ng.subject.Subject;
import org.shanoir.ng.subject.SubjectRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Implementation of study shared service.
 * 
 * @author ifakhfakh
 *
 */
@Service
public class StudyServiceSharedImpl implements StudyServiceShared {

	/**
	 * Logger
	 */
	private static final Logger LOG = LoggerFactory.getLogger(StudyServiceSharedImpl.class);

	@Autowired
	private StudyRepository studyRepository;

	@Autowired
	private SubjectRepository subjectRepository;

	@Autowired
	private CenterRepository centerRepository;

	@Override
	public StudySubjectCenterNamesDTO findByIds(final Long studyId, final Long subjectId, final Long centerId) {
		Study study = studyRepository.findOne(studyId);
		Subject subject = new Subject();;
		if (subjectId !=0)
			subject = subjectRepository.findOne(subjectId);
		Center center = centerRepository.findOne(centerId);
		
		StudySubjectCenterNamesDTO names = new StudySubjectCenterNamesDTO();
		names.setStudyName(study.getName());
		if (subjectId !=0)
			names.setSubjectName(subject.getName());
		else 
			names.setSubjectName("");
		names.setCenterName(center.getName());
		
		return names;
	}

}
