package org.shanoir.ng.subject;

import java.io.File;
import java.util.List;
import java.util.Optional;

import org.shanoir.ng.shared.dto.IdNameDTO;
import org.shanoir.ng.shared.exception.ShanoirStudiesException;
import org.shanoir.ng.shared.validation.UniqueCheckableService;
import org.shanoir.ng.subject.dto.SimpleSubjectDTO;
import org.shanoir.ng.subject.dto.SubjectFromShupDTO;
import org.shanoir.ng.subjectstudy.ExaminationDTO;
import org.shanoir.ng.subjectstudy.SubjectStudy;

/**
 * Subject service.
 *
 * @author msimon
 *
 */
public interface SubjectService extends UniqueCheckableService<Subject> {

	/**
	 * Delete a subject.
	 *
	 * @param id
	 *            subject id.
	 * @throws ShanoirStudiesException
	 */
	void deleteById(Long id) throws ShanoirStudiesException;

	/**
	 * Get all the subjects.
	 *
	 * @return a list of subjects.
	 */
	List<Subject> findAll();

	/**
	 * Find id and name for all subjects.
	 * 
	 * @return list of subjects.
	 */
	List<IdNameDTO> findIdsAndNames();
	
	/**
	 * Find subject by data.
	 *
	 * @param data
	 *            data.
	 * @return a subject.
	 */
	Optional<Subject> findByData(String data);

	/**
	 * Find subject by its id.
	 *
	 * @param id
	 *            template id.
	 * @return a template or null.
	 */
	Subject findById(Long id);

	/**
	 * Save a subject.
	 *
	 * @param subject
	 *            subject to create.
	 * @return created subject.
	 * @throws ShanoirStudiesException
	 */
	Subject save(Subject subject) throws ShanoirStudiesException;
	
	/**
	 * Save a subject for OFSEP.
	 *
	 * @param subject
	 *            subject to create.
	 * @param studyCardId
	 * 			id of the study card used to generate the subject common name
	 * @return created subject.
	 * @throws ShanoirStudiesException
	 */

	Subject saveForOFSEP( Subject subject,  Long studyCardId) throws ShanoirStudiesException;

	
	/**
	 * Save a subject for OFSEP.
	 *
	 * @param subject
	 *            subject to create.
	 * @param subjectStudy
	 *            subjectStudy to be added to subject.
	 * @param studyCardId
	 * 			id of the study card used to generate the subject common name
	 * @return created subject.
	 * @throws ShanoirStudiesException
	 */

	Subject saveForOFSEP(SubjectFromShupDTO subjectFromShupDTO) throws ShanoirStudiesException;

	
	/**
	 * Update a subject.
	 *
	 * @param subject
	 *            subject to update.
	 * @return updated subject.
	 * @throws ShanoirStudiesException
	 */
	Subject update(Subject subject) throws ShanoirStudiesException;

	/**
	 * Update a subject.
	 *
	 * @param subject
	 *            subject to update.
	 * @return updated subject.
	 * @throws ShanoirStudiesException
	 */
	Subject update(Subject subject, SubjectFromShupDTO subjectFromShupDTO) throws ShanoirStudiesException;
	
	/**
	 * Update a subject from the old Shanoir
	 *
	 * @param subject
	 *            subject.
	 * @throws ShanoirStudiesException
	 */
	void updateFromShanoirOld(Subject subject) throws ShanoirStudiesException;

	/*
	 * Update Shanoir Old with new subject.
	 *
	 * @param Subject subject.
	 *
	 * @return false if it fails, true if it succeed.
	 */
	boolean updateShanoirOld(final Subject subject);


	/**
	 * Get all the subjects of a study
	 *
	 * @param studyId
	 * @return list of subjects
	 */
	public List<SimpleSubjectDTO> findAllSubjectsOfStudy(final Long studyId);

	/**
	 * Find subject by its identifier.
	 *
	 * @param indentifier
	 *            data.
	 * @return a indentifier.
	 */
	Subject findByIdentifier(String indentifier);

	Subject saveFromJson(File jsonFile) throws ShanoirStudiesException;

	public String findSubjectOfsepByCenter(final String centerCode);
	
	public List<ExaminationDTO> findExaminationsForSubjectStudyRel(final Long subjectId, final Long studyId) throws ShanoirStudiesException;


}
