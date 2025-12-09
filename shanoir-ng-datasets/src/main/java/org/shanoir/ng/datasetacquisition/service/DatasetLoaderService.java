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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.dataset.repository.BidsDatasetRepository;
import org.shanoir.ng.dataset.repository.CtDatasetRepository;
import org.shanoir.ng.dataset.repository.EegDatasetRepository;
import org.shanoir.ng.dataset.repository.GenericDatasetRepository;
import org.shanoir.ng.dataset.repository.MrDatasetRepository;
import org.shanoir.ng.dataset.repository.PetDatasetRepository;
import org.shanoir.ng.dataset.repository.XaDatasetRepository;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.datasetacquisition.model.GenericDatasetAcquisition;
import org.shanoir.ng.datasetacquisition.model.bids.BidsDatasetAcquisition;
import org.shanoir.ng.datasetacquisition.model.ct.CtDatasetAcquisition;
import org.shanoir.ng.datasetacquisition.model.eeg.EegDatasetAcquisition;
import org.shanoir.ng.datasetacquisition.model.mr.MrDatasetAcquisition;
import org.shanoir.ng.datasetacquisition.model.pet.PetDatasetAcquisition;
import org.shanoir.ng.datasetacquisition.model.xa.XaDatasetAcquisition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DatasetLoaderService {

    @Autowired
    private MrDatasetRepository mrDatasetRepository;

    @Autowired
    private CtDatasetRepository ctDatasetRepository;

    @Autowired
    private PetDatasetRepository petDatasetRepository;

    @Autowired
    private EegDatasetRepository eegDatasetRepository;

    @Autowired
    private BidsDatasetRepository bidsDatasetRepository;

    @Autowired
    private GenericDatasetRepository genericDatasetRepository;

    @Autowired
    private XaDatasetRepository xaDatasetRepository;

    /**
     * Load datasets based on acquisition type.
     */
    public List<Dataset> loadDatasets(DatasetAcquisition acquisition) {
        if (acquisition == null) {
            return Collections.emptyList();
        }
        if (acquisition instanceof MrDatasetAcquisition) {
            return new ArrayList<>(mrDatasetRepository.findByAcquisitionId(acquisition.getId()));
        } else if (acquisition instanceof CtDatasetAcquisition) {
            return new ArrayList<>(ctDatasetRepository.findByAcquisitionId(acquisition.getId()));
        } else if (acquisition instanceof PetDatasetAcquisition) {
            return new ArrayList<>(petDatasetRepository.findByAcquisitionId(acquisition.getId()));
        } else if (acquisition instanceof EegDatasetAcquisition) {
            return new ArrayList<>(eegDatasetRepository.findByAcquisitionId(acquisition.getId()));
        } else if (acquisition instanceof BidsDatasetAcquisition) {
            return new ArrayList<>(bidsDatasetRepository.findByAcquisitionId(acquisition.getId()));
        } else if (acquisition instanceof GenericDatasetAcquisition) {
            return new ArrayList<>(genericDatasetRepository.findByAcquisitionId(acquisition.getId()));
        } else if (acquisition instanceof XaDatasetAcquisition) {
            return new ArrayList<>(xaDatasetRepository.findByAcquisitionId(acquisition.getId()));
        }
        return Collections.emptyList();
    }

}
