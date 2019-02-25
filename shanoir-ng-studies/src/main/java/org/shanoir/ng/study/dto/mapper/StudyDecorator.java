package org.shanoir.ng.study.dto.mapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.shanoir.ng.groupofsubjects.ExperimentalGroupOfSubjectsMapper;
import org.shanoir.ng.shared.dto.IdNameDTO;
import org.shanoir.ng.study.dto.MembersCategoryDTO;
import org.shanoir.ng.study.dto.SimpleStudyDTO;
import org.shanoir.ng.study.dto.StudyDTO;
import org.shanoir.ng.study.model.Study;
import org.shanoir.ng.studycenter.StudyCenterMapper;
import org.shanoir.ng.studyuser.StudyUser;
import org.shanoir.ng.studyuser.StudyUserRight;
import org.shanoir.ng.subjectstudy.SubjectStudyMapper;
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
			if (study.getStudyUserList() != null && !study.getStudyUserList().isEmpty()) {
				prepareMembersCategories(study, studyDTO);
			}
		}
		return studyDTO;
	}

	private void prepareMembersCategories(final Study study, final StudyDTO studyDTO) {
		// Sort members by category (studyUserType)
		final Map<StudyUserRight, List<IdNameDTO>> membersMap = new HashMap<>();
		for (StudyUser studyUser : study.getStudyUserList()) {
			final IdNameDTO member = new IdNameDTO(studyUser.getUserId(), studyUser.getUserName());
			if (membersMap.containsKey(studyUser.getStudyUserType())) {
				membersMap.get(studyUser.getStudyUserType()).add(member);
			} else {
				final List<IdNameDTO> studyUsers = new ArrayList<>();
				studyUsers.add(member);
				membersMap.put(studyUser.getStudyUserType(), studyUsers);
			}
		}
		// Transform map into list
		studyDTO.setMembersCategories(new ArrayList<>());
		for (StudyUserRight type : membersMap.keySet()) {
			studyDTO.getMembersCategories().add(new MembersCategoryDTO(type, membersMap.get(type)));
		}
		// Sort categories by importance
		Collections.sort(studyDTO.getMembersCategories(), new MembersCategoryComparator());
	}
	
	private class MembersCategoryComparator implements Comparator<MembersCategoryDTO> {
		
		@Override
		public int compare(MembersCategoryDTO category1, MembersCategoryDTO category2) {
			return (category1.getStudyUserType().getId() > category2.getStudyUserType().getId()) ? 1 : -1;
		}
		
	}
}

