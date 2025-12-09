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

import org.apache.solr.client.solrj.SolrServerException;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.shared.event.ShanoirEvent;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;

public interface DatasetAcquisitionService {

    @PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
    @PostAuthorize("hasRole('ADMIN') or returnObject == null or @datasetSecurityService.hasRightOnExamination(returnObject.getExamination().getId(), 'CAN_SEE_ALL')")
    DatasetAcquisition findById(Long id);

    @PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
    public DatasetAcquisition findByIdWithDatasets(Long id);

    @PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
    public List<Dataset> getDatasets(DatasetAcquisition acquisition);

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

    @PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
    DatasetAcquisition findByExaminationIdAndSeriesInstanceUIDWithDatasets(Long examinationId, String seriesInstanceUID);

    @PreAuthorize("hasAnyRole('ADMIN', 'EXPERT') and  @datasetSecurityService.hasRightOnExamination(#entity.examination.id, 'CAN_ADMINISTRATE')")
    DatasetAcquisition update(DatasetAcquisition entity) throws EntityNotFoundException;

    @PreAuthorize("hasAnyRole('ADMIN', 'EXPERT') and  @datasetSecurityService.filterDatasetAcquisitionList(#entities, 'CAN_ADMINISTRATE')")
    Iterable<DatasetAcquisition> update(List<DatasetAcquisition> entities);

    @PreAuthorize("hasAnyRole('ADMIN', 'EXPERT') and  @datasetSecurityService.hasRightOnDatasetAcquisition(#id, 'CAN_ADMINISTRATE')")
    void deleteById(Long id, ShanoirEvent event) throws EntityNotFoundException, ShanoirException, SolrServerException, IOException, RestServiceException;

    void deleteByIdCascade(Long id, ShanoirEvent event) throws EntityNotFoundException, ShanoirException, SolrServerException, IOException, RestServiceException;

    boolean existsByStudyCardId(Long studyCardId);

    Collection<DatasetAcquisition> createAll(Collection<DatasetAcquisition> acquisitions);

}
