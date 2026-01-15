/**
 * Shanoir NG - Import, manage and share neuroimaging data
 * Copyright (C) 2009-2019 Inria - https://www.inria.fr/
 * Contact us on https://project.inria.fr/shanoir/
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/gpl-3.0.html
 */

package org.shanoir.ng.subject.service;

import java.util.List;

import org.shanoir.ng.shared.core.model.IdName;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.MicroServiceCommunicationException;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.study.model.Study;
import org.shanoir.ng.subject.dto.SimpleSubjectDTO;
import org.shanoir.ng.subject.dto.SubjectDTO;
import org.shanoir.ng.subject.model.Subject;
import org.shanoir.ng.subjectstudy.model.SubjectStudy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * Subject service.
 *
 * @author msimon
 *
 */
public interface SubjectService {

    /**
     * Get all the subjects.
     *
     * @return a list of subjects.
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
    @PostFilter("hasRole('ADMIN') or @studySecurityService.hasRightOnTrustedSubjectForOneStudy(filterObject, 'CAN_SEE_ALL')")
    List<Subject> findAll();

    @PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
    @PostFilter("hasRole('ADMIN') or @studySecurityService.hasRightOnTrustedSubjectForOneStudy(filterObject, 'CAN_SEE_ALL')")
    Iterable<Subject> findAllById(List<Long> subjectIds);

    /**
     * Get all the subjects.
     *
     * @return a list of subjects.
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
    List<IdName> findAllNames();

    @PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
    List<IdName> findNames(List<Long> subjectIds);

    /**
     * Get all the subjects of a study
     *
     * @param studyId
     * @return list of subjects
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
    @PostAuthorize("hasRole('ADMIN') or @studySecurityService.hasRightOnSubjectsForOneStudy(returnObject, 'CAN_SEE_ALL')")
    public List<SimpleSubjectDTO> findAllSubjectsOfStudyId(final Long studyId);

    /**
     * Get all the subjects of a study
     *
     * @param studyId
     * @param preclinical is the subject preclinical or not
     * @return list of subjects
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
    @PostAuthorize("hasRole('ADMIN') or @studySecurityService.hasRightOnSubjectsForOneStudy(returnObject, 'CAN_SEE_ALL')")
    List<SimpleSubjectDTO> findAllSubjectsOfStudyAndPreclinical(Long studyId, Boolean preclinical);

    /**
     * Find subject by its id.
     *
     * @param id template id.
     * @return a template or null.
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
    @PostAuthorize("hasRole('ADMIN') or @studySecurityService.hasRightOnTrustedSubjectForOneStudy(returnObject, 'CAN_SEE_ALL')")
    Subject findById(Long id);

    @PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
    Subject findByStudyIdAndName(Long id, String name);

    /**
     * Find subject by its identifier and a list of studies (based on the rights).
     *
     * As only the list of accessible studies is used here, that is rights filtered,
     * I remove here the additional PostAuthorize filter to check again the rights
     * on the subject. We do not want to impact performance to heavily with double
     * or triple rights checks.
     *
     * @param identifier - hash to search a subject
     * @param studies - list of studies to search with identifier
     * @return the subject or null
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
    Subject findByIdentifierInStudiesWithRights(String identifier, List<Study> studies);

    /**
     * Find a subject from a center code
     * @param centerCode
     * @return a subject or null
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
    @PostAuthorize("hasRole('ADMIN') or @studySecurityService.hasRightOnTrustedSubjectForOneStudy(returnObject, 'CAN_SEE_ALL')")
    Subject findSubjectFromCenterCode(String centerCode);

    /**
     * Save a subject.
     *
     * @param subject subject to create.
     * @return created subject.
     */
    @PreAuthorize("hasRole('ADMIN') or (hasAnyRole('EXPERT', 'USER') and @studySecurityService.hasRightOnTrustedSubjectForOneStudy(#subject, 'CAN_IMPORT'))")
    Subject create(Subject subject, boolean withAMQP) throws ShanoirException;

    /**
     * Save a subject and auto-increment the common name on using the centerId.
     *
     * @param subject subject to create.
     * @return created subject.
     */
    @PreAuthorize("hasRole('ADMIN') or (hasAnyRole('EXPERT', 'USER') and @studySecurityService.hasRightOnTrustedSubjectForOneStudy(#subject, 'CAN_IMPORT'))")
    Subject createAutoIncrement(Subject subject, Long centerId, boolean withAMQP) throws ShanoirException;

    /**
     * Update a subject.
     *
     * @param subject subject to update.
     * @return updated subject.
     * @throws EntityNotFoundException
     * @throws MicroServiceCommunicationException
     * @throws ShanoirException
     * @throws RestServiceException
     */
    @PreAuthorize("hasRole('ADMIN') or (hasAnyRole('EXPERT', 'USER') and @studySecurityService.hasRightOnSubjectForOneStudy(#subject, 'CAN_IMPORT'))")
    Subject update(@Param("subject") Subject subject) throws ShanoirException;


    /**
     * Delete a subject.
     *
     * @param id subject id.
     * @throws EntityNotFoundException
     */
    @PreAuthorize("hasAnyRole('ADMIN') or hasAnyRole('EXPERT') and @studySecurityService.hasRightOnSubjectForEveryStudy(#id, 'CAN_ADMINISTRATE')")
    void deleteById(Long id) throws EntityNotFoundException;

    /**
     * Update subject name and values for other microservices.
     * @param subjectToSubjectDTO the subject DTO to update
     * @throws MicroServiceCommunicationException
     */
    boolean updateSubjectInMicroservices(SubjectDTO subjectToSubjectDTO) throws MicroServiceCommunicationException;

    /**
     * Returns a filtered page by clinical subject name.
     * @param page pageable
     * @param name the subject name filter
     * @param studies the list of allowed studies
     * @return the list of clinical subject as page
     */
    Page<Subject> getClinicalFilteredPageByStudies(Pageable page, String name, List<Study> studies);

    @PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
    @PostFilter("hasRole('ADMIN') or @studySecurityService.hasRightOnTrustedSubjectForOneStudy(filterObject, 'CAN_SEE_ALL')")
    List<Subject> findByPreclinical(boolean preclinical);

    boolean existsSubjectWithName(String name);

    public void mapSubjectStudyTagListToSubjectStudyTagList(SubjectStudy sSOld, SubjectStudy sSNew);

    boolean isSubjectNameExistForStudy(Long studyId, String subjectName);
}
