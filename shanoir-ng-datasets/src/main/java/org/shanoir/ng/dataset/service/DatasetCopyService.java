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

package org.shanoir.ng.dataset.service;

import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.examination.model.Examination;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.Map;

public interface DatasetCopyService {
    @PreAuthorize("hasRole('ADMIN') or (hasRole('EXPERT') and @datasetSecurityService.hasRightOnDataset(#dataset.getId(), 'CAN_ADMINISTRATE'))")
    Object[] moveDataset(Dataset ds, Long studyId, Map<Long, Examination> examMap, Map<Long, DatasetAcquisition> acqMap, Long userId) throws Exception;
}
