package org.shanoir.ng.subject;

import org.shanoir.ng.study.StudyRepository;
import org.shanoir.ng.subject.dto.SubjectDTO;
import org.shanoir.ng.subject.dto.SubjectStudyDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public abstract class SubjectDecorator implements SubjectMapper {

	@Autowired
	@Qualifier("delegate")
	private SubjectMapper delegate;

	@Autowired
	private StudyRepository studyRepository;

	@Override
	public Subject subjectDTOToSubject(SubjectDTO subjectDTO) {
		Subject subject = delegate.subjectDTOToSubject(subjectDTO);

		// Subject Study List manual mapping
		if (!subjectDTO.getSubjectStudyList().isEmpty()) {
			for (SubjectStudyDTO s : subjectDTO.getSubjectStudyList()) {
				SubjectStudy subjectStudy = delegate.subjectStudyDTOToSubjectStudy(s);
				subject.addSubjectStudy(subjectStudy);
				subjectStudy.setSubject(subject);
			}
		}

		// Subject Image Object Category manual mapping
		if (subjectDTO.getImagedObjectCategory() != null) {
			for (ImagedObjectCategory i : ImagedObjectCategory.values()) {
				if (i.name().equals(subjectDTO.getImagedObjectCategory())) {
					subject.setImagedObjectCategory(i);
					break;
				}
			}
		}
		return subject;
	}

	@Override
	public SubjectStudy subjectStudyDTOToSubjectStudy(SubjectStudyDTO subjectStudyDTO) {
		SubjectStudy subjectStudy = delegate.subjectStudyDTOToSubjectStudy(subjectStudyDTO);
		if (subjectStudyDTO.getStudyId() != null) {
			subjectStudy.setStudy(studyRepository.findOne(subjectStudyDTO.getStudyId()));
		}
		return subjectStudy;
	}

}
