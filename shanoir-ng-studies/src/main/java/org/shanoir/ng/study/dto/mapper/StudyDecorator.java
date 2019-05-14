package org.shanoir.ng.study.dto.mapper;

import java.util.ArrayList;
import java.util.List;

import org.shanoir.ng.groupofsubjects.ExperimentalGroupOfSubjectsMapper;
import org.shanoir.ng.study.dto.SimpleStudyDTO;
import org.shanoir.ng.study.dto.StudyDTO;
import org.shanoir.ng.study.model.Study;
import org.shanoir.ng.studycenter.StudyCenterMapper;
import org.shanoir.ng.subjectstudy.dto.mapper.SubjectStudyMapper;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Decorator for study.
 * 
 * @author msimon
 *
 */
public abstract class StudyDecorator implements StudyMapper {

	@Autowired
	private ExperimentalGroupOfSubjectsMapper experimentalGroupOfSubjectsMapper;

	@Autowired
	private StudyMapper delegate;

	@Autowired
	private StudyCenterMapper studyCenterMapper;

	@Autowired
	private SubjectStudyMapper subjectStudyMapper;

	@Override
	public List<StudyDTO> studiesToStudyDTOs(final List<Study> studies) {
		final List<StudyDTO> studyDTOs = new ArrayList<>();
		for (Study study : studies) {
			final StudyDTO studyDTO = convertStudyToStudyDTO(study, false);
			if (study.getSubjectStudyList() != null) {
				studyDTO.setNbSujects(study.getSubjectStudyList().size());
			}
			if (study.getExaminationIds() != null) {
				studyDTO.setNbExaminations(study.getExaminationIds().size());
			}
			studyDTOs.add(studyDTO);
		}
		return studyDTOs;
	}

	@Override
	public StudyDTO studyToStudyDTO(final Study study) {
		return convertStudyToStudyDTO(study, true);
	}
	
	@Override
	public SimpleStudyDTO studyToSimpleStudyDTO (final Study study) {
		final SimpleStudyDTO simpleStudyDTO = delegate.studyToSimpleStudyDTO(study);
		simpleStudyDTO.setStudyCenterList(studyCenterMapper.studyCenterListToStudyCenterDTOList(study.getStudyCenterList()));
		
		return simpleStudyDTO;
	}
	
	@Override
	public List<SimpleStudyDTO> studiesToSimpleStudyDTOs (final List<Study> studies) {
		List<SimpleStudyDTO> simpleStudyDTOs = new ArrayList<>();
		for (Study study: studies) {
			final SimpleStudyDTO simpleStudyDTO = studyToSimpleStudyDTO(study);
			simpleStudyDTOs.add(simpleStudyDTO);
		}
		return simpleStudyDTOs;
	}

	/*
	 * Map a @Study to a @StudyDTO.
	 * 
	 * @param study study to map.
	 * 
	 * @param withData study with data?
	 * 
	 * @return study DTO.
	 */
	private StudyDTO convertStudyToStudyDTO(final Study study, final boolean withData) {
		final StudyDTO studyDTO = delegate.studyToStudyDTO(study);
		if (withData) {
			studyDTO.setStudyCenterList(
					studyCenterMapper.studyCenterListToStudyCenterDTOList(study.getStudyCenterList()));
			studyDTO.setSubjectStudyList(subjectStudyMapper.subjectStudyListToSubjectStudyDTOList(study.getSubjectStudyList()));
			studyDTO.setExperimentalGroupsOfSubjects(experimentalGroupOfSubjectsMapper
					.experimentalGroupOfSubjectsToIdNameDTOs(study.getExperimentalGroupsOfSubjects()));
		}
		return studyDTO;
	}
}

