package org.shanoir.ng.dicom.web.dto.mapper;

import java.util.Optional;

import org.mapstruct.Mapper;
import org.shanoir.ng.anonymization.uid.generation.UIDGeneration;
import org.shanoir.ng.dicom.web.dto.StudyDTO;
import org.shanoir.ng.examination.model.Examination;
import org.shanoir.ng.shared.model.Subject;
import org.shanoir.ng.shared.paging.PageImpl;
import org.shanoir.ng.shared.repository.SubjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

/**
 * This class maps the Examination objects from the Shanoir-NG database
 * to a DICOM-study DTO to implement the DICOMweb protocol.
 * 
 * @author mkain
 *
 */
@Mapper(componentModel = "spring")
public abstract class ExaminationToStudyDTOMapper {
	
	@Autowired
	protected SubjectRepository subjectRepository;
	
	public StudyDTO examinationToStudyDTO(Examination examination) {
		StudyDTO studyDTO = new StudyDTO();
		final String studyInstanceUID = UIDGeneration.ROOT + "." + examination.getId();
		// 5 DICOM-study specific values
		studyDTO.setStudyInstanceUID(studyInstanceUID);
		studyDTO.setStudyID(examination.getId());
		studyDTO.setStudyDescription(examination.getComment());
		studyDTO.setStudyDate(examination.getExaminationDate().toString());
		studyDTO.setStudyTime("000000"); // today we do not store this info in our db
		// 4 patient specific values
		// @TODO optimize here: not ask the database for each subject id, use a cached list?
		Optional<Subject> subjectOpt = subjectRepository.findById(examination.getSubjectId());
		String subjectName = "error_subject_name_not_found_in_db";
		if (subjectOpt.isPresent()) {
			subjectName = subjectOpt.get().getName();
		}
		studyDTO.setPatientName(subjectName);
		studyDTO.setPatientID(examination.getSubjectId().toString());
		studyDTO.setPatientBirthDate("01011960"); // @TODO not yet in ms datasets database
		studyDTO.setPatientSex("F"); // @TODO not yet in ms datasets database
		return studyDTO;
	}
	
	/**
	 * Map list of @Examination to list of @StudyDTO.
	 *  
	 * @param examinations
	 * @return list of StudyDTO
	 */
	public abstract PageImpl<StudyDTO> examinationsToStudyDTOs(Page<Examination> examinations);
	
}
