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

package org.shanoir.ng.examination.service;

import org.apache.solr.client.solrj.SolrServerException;
import org.shanoir.ng.examination.model.Examination;
import org.shanoir.ng.shared.event.ShanoirEvent;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Examination service.
 *
 * @author ifakhfakh
 *
 */
public interface ExaminationService {

    /**
     * Delete an examination.
     *
     * @param id examination id.
     * @throws EntityNotFoundException
     * @throws ShanoirException
     */
    @PreAuthorize("hasRole('ADMIN') or (hasRole('EXPERT') and @datasetSecurityService.hasRightOnExamination(#id, 'CAN_ADMINISTRATE'))")
    void deleteById(Long id, ShanoirEvent event) throws ShanoirException, SolrServerException, IOException, RestServiceException;

    @PreAuthorize("hasRole('ADMIN') or (hasRole('EXPERT') and @datasetSecurityService.hasRightOnExamination(#examinationId, 'CAN_ADMINISTRATE'))")
    void deleteExaminationAsync(Long examinationId, Long studyId, ShanoirEvent event);

    /**
     * Get all examinations for a specific user to support DICOMweb.
     *
     * @return
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
    @PostAuthorize("hasRole('ADMIN') or @datasetSecurityService.filterExaminationList(returnObject, 'CAN_SEE_ALL')")
    List<Examination> findAll();
    
    /**
     * Get a paginated list of examinations reachable by connected user.
     *
     * @param pageable pagination data.
     * @return list of examinations.
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
    @PostAuthorize("hasRole('ADMIN') or @datasetSecurityService.filterExaminationPage(returnObject, 'CAN_SEE_ALL')")
    Page<Examination> findPage(final Pageable pageable, boolean preclinical, String searchStr, String searchField);
    
    /**
     * Get a paginated list of examinations reachable by connected user.
     *
     * @param pageable pagination data.
     * @return list of examinations.
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
    @PostAuthorize("hasRole('ADMIN') or @datasetSecurityService.filterExaminationPage(returnObject, 'CAN_SEE_ALL')")
    Page<Examination> findPage(final Pageable pageable, String patientName);
    
    /**
     * Find examination by its id.
     *
     * @param id examination id.
     * @return an examination or null.
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
    @PostAuthorize("hasRole('ADMIN') or @datasetSecurityService.hasRightOnStudyCenter(returnObject.getCenterId(), returnObject.getStudyId(), 'CAN_SEE_ALL')")
    Examination findById(Long id);

    /**
     * Find examinations related to particular subject
     * @param subjectId
     * @return
     * @author yyao
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
    @PostAuthorize("hasRole('ADMIN') or @datasetSecurityService.filterExaminationList(returnObject, 'CAN_SEE_ALL')")
    List<Examination> findBySubjectId(Long subjectId);

    /**
     * Find examinations related to particular study
     * @param subjectId
     * @return
     * @author yyao
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
    List<Long> findIdsByStudyId(Long studyId);

    /**
     * Find examinations related to particular study
     * @param subjectId
     * @return
     * @author yyao
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
    @PostAuthorize("hasRole('ADMIN') or @datasetSecurityService.filterExaminationList(returnObject, 'CAN_SEE_ALL')")
    List<Examination> findByStudyId(Long studyId);

    /**
     * Find examinations related to particular subject and study
     *
     * @param subjectId: the id of the subject
     * @param studyId: the id of the study
     * @return list of examinations.
     */
    @PreAuthorize("hasRole('ADMIN') or (hasAnyRole('EXPERT', 'USER') and @datasetSecurityService.hasRightOnStudy(#studyId, 'CAN_SEE_ALL'))")
    @PostAuthorize("hasRole('ADMIN') or (hasAnyRole('EXPERT', 'USER') and  @datasetSecurityService.filterExaminationList(returnObject, 'CAN_SEE_ALL'))")
    List<Examination> findBySubjectIdStudyId(Long subjectId, Long studyId);

    /**
     * Save an examination.
     *
     * @param examination  examination to create.
     * @return created examination.
     */
    @PreAuthorize("hasRole('ADMIN') or (hasAnyRole('EXPERT', 'USER') and @datasetSecurityService.hasRightOnStudyCenter(#examination.getCenterId(), #examination.getStudyId(), 'CAN_IMPORT'))")
    Examination save(Examination examination);
    
    /**
     * Update an examination.
     *
     * @param examination  examination to update.
     * @return updated examination.
     * @throws EntityNotFoundException
     * @throws ShanoirException
     */
    @PreAuthorize("hasRole('ADMIN') or (hasAnyRole('EXPERT', 'USER') and @datasetSecurityService.hasRightOnExamination(#examination.getId(), 'CAN_IMPORT'))")
    Examination update(Examination examination) throws EntityNotFoundException, ShanoirException;

    Long getExtraDataSizeByStudyId(Long studyId);

    /**
     * Add an extra data file to examination
     * @param examinationId the examination ID
     * @param file the file to add
     * @return true if it's a success, false otherwise
     */
    String addExtraData(Long examinationId, MultipartFile file);

    String addExtraDataFromFile(Long examinationId, File file);

    @PreAuthorize("hasRole('ADMIN') or (hasAnyRole('EXPERT', 'USER') and (@datasetSecurityService.hasRightOnExamination(#examinationId, 'CAN_DOWNLOAD') or @datasetSecurityService.hasRightOnExamination(#examinationId, 'CAN_ADMINISTRATE')))")
    String getExtraDataFilePath(Long examinationId, String fileName);
}
