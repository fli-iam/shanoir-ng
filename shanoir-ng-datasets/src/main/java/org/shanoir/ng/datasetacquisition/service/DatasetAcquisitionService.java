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

package org.shanoir.ng.datasetacquisition.service;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.apache.solr.client.solrj.SolrServerException;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.shared.event.ShanoirEvent;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.multipart.MultipartFile;

public interface DatasetAcquisitionService {

    @PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
    @PostAuthorize("hasRole('ADMIN') or returnObject == null or @datasetSecurityService.hasRightOnExamination(returnObject.getExamination().getId(), 'CAN_SEE_ALL')")
    DatasetAcquisition findById(Long id);

    @PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
    Optional<DatasetAcquisition> findByExaminationAndSeriesInstanceUIDWithDatasets(Long examinationId,
            String seriesInstanceUID);

    @PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
    public DatasetAcquisition findByIdWithDatasets(Long id);

    @PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
    @PostAuthorize("hasRole('ADMIN') or @datasetSecurityService.checkDatasetAcquisitionPage(returnObject, 'CAN_SEE_ALL')")
    public Page<DatasetAcquisition> findPage(final Pageable pageable);

    @PreAuthorize("#entity.getId() == null and (hasRole('ADMIN') or (hasAnyRole('EXPERT', 'USER') and @datasetSecurityService.hasRightOnExamination(#entity.getExamination().getId(), 'CAN_IMPORT')))")
    DatasetAcquisition create(DatasetAcquisition entity, boolean indexDatasetsToSolr);

    @PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
    @PostAuthorize("hasRole('ADMIN') or @datasetSecurityService.filterDatasetAcquisitionList(returnObject, 'CAN_SEE_ALL')")
    List<DatasetAcquisition> findById(List<Long> ids);

    @PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
    @PostAuthorize("hasRole('ADMIN') or @datasetSecurityService.filterDatasetAcquisitionList(returnObject, 'CAN_SEE_ALL')")
    public List<DatasetAcquisition> findByStudyCard(Long studyCardId);

    @PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
    @PostAuthorize("hasRole('ADMIN') or @datasetSecurityService.filterDatasetAcquisitionList(returnObject, 'CAN_SEE_ALL')")
    List<DatasetAcquisition> findByDatasetId(Long[] datasetIds);

    @PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
    @PostAuthorize("hasRole('ADMIN') or @datasetSecurityService.filterDatasetAcquisitionList(returnObject, 'CAN_SEE_ALL')")
    List<DatasetAcquisition> findByExamination(Long examinationId);

    @PreAuthorize("hasAnyRole('ADMIN', 'EXPERT') and  @datasetSecurityService.hasRightOnExamination(#entity.examination.id, 'CAN_ADMINISTRATE')")
    DatasetAcquisition update(DatasetAcquisition entity) throws EntityNotFoundException;

    @PreAuthorize("hasAnyRole('ADMIN', 'EXPERT') and  @datasetSecurityService.filterDatasetAcquisitionList(#entities, 'CAN_ADMINISTRATE')")
    Iterable<DatasetAcquisition> update(List<DatasetAcquisition> entities);

    @PreAuthorize("hasAnyRole('ADMIN', 'EXPERT') and  @datasetSecurityService.hasRightOnDatasetAcquisition(#id, 'CAN_ADMINISTRATE')")
    void deleteById(Long id, ShanoirEvent event) throws EntityNotFoundException, ShanoirException, SolrServerException, IOException, RestServiceException;

    @PreAuthorize("hasAnyRole('ADMIN', 'EXPERT') and  @datasetSecurityService.hasRightOnDatasetAcquisition(#entity.id, 'CAN_ADMINISTRATE')")
    void delete(DatasetAcquisition entity, ShanoirEvent event) throws EntityNotFoundException, ShanoirException, SolrServerException, IOException, RestServiceException;

    void deleteByIdCascade(Long id, ShanoirEvent event) throws EntityNotFoundException, ShanoirException, SolrServerException, IOException, RestServiceException;

    /**
     * Tells whether an acquisition holds no dataset anymore and may be removed automatically.
     * An acquisition is only removable when it carries no data of its own: no extra-data file
     * uploaded on the acquisition itself, and no copy pointing to it as its source.
     *
     * @param id the acquisition id
     * @return true when the acquisition is empty and may be removed
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'EXPERT') and @datasetSecurityService.hasRightOnDatasetAcquisition(#id, 'CAN_ADMINISTRATE')")
    boolean isEmptyAndRemovable(Long id) throws EntityNotFoundException;

    /**
     * Deletes an acquisition that does not hold any dataset anymore.
     *
     * @param id the acquisition id
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'EXPERT') and @datasetSecurityService.hasRightOnDatasetAcquisition(#id, 'CAN_ADMINISTRATE')")
    void deleteEmptyAcquisition(Long id) throws EntityNotFoundException, RestServiceException;

    /**
     * Finds the acquisitions that would not hold any dataset anymore once the given datasets are
     * deleted, and that may then be removed automatically. Acquisitions holding extra data or
     * copied to another study are left out: they are meant to survive the deletion.
     *
     * @param datasetIds the datasets about to be deleted
     * @return the acquisitions that the deletion would leave empty and removable
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'EXPERT')")
    @PostAuthorize("hasRole('ADMIN') or @datasetSecurityService.filterDatasetAcquisitionList(returnObject, 'CAN_ADMINISTRATE')")
    List<DatasetAcquisition> findAcquisitionsLeftEmptyBy(List<Long> datasetIds);

    /**
     * Finds the acquisitions that hold no dataset at all and that may be removed. Meant for the
     * clean up of the acquisitions emptied before their removal was proposed on deletion.
     *
     * @param studyId the study to look into, or null to look into all of them
     * @return the empty and removable acquisitions
     */
    @PreAuthorize("hasRole('ADMIN')")
    List<DatasetAcquisition> findEmptyAcquisitions(Long studyId);

    boolean existsByStudyCardId(Long studyCardId);

    Collection<DatasetAcquisition> createAll(Collection<DatasetAcquisition> acquisitions);

    String addExtraData(Long acquisitionId, MultipartFile file);

}
