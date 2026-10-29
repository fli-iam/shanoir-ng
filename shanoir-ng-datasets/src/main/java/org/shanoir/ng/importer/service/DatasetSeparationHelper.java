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

package org.shanoir.ng.importer.service;

import java.util.HashSet;
import java.util.Set;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.shanoir.ng.dataset.modality.MrDataset;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.datasetacquisition.model.mr.MrDatasetAcquisition;
import org.shanoir.ng.shared.dicom.EchoTime;

/**
 * Echo times belong to the dataset separation key of MR acquisitions only: only
 * MR datasets store them. The key of an incoming instance and the key rebuilt from
 * an existing dataset must follow the same rule, otherwise they never match and
 * every instance of a non-MR series opens a new dataset.
 */
final class DatasetSeparationHelper {

    private DatasetSeparationHelper() {
    }

    /**
     * Echo times of an incoming DICOM instance, to match against existing datasets.
     */
    static Set<EchoTime> echoTimesForDatasetSeparation(DatasetAcquisition acquisition, Attributes attributes) {
        Set<EchoTime> echoTimes = new HashSet<>();
        if (acquisition instanceof MrDatasetAcquisition) {
            EchoTime echoTime = new EchoTime();
            echoTime.setEchoNumber(attributes.getInt(Tag.EchoNumbers, 0));
            echoTime.setEchoTime(attributes.getDouble(Tag.EchoTime, 0.0));
            echoTimes.add(echoTime);
        }
        return echoTimes;
    }

    /**
     * Echo times of an existing dataset, rebuilt from the database.
     */
    static Set<EchoTime> echoTimesForDatasetSeparation(DatasetAcquisition acquisition, Dataset dataset) {
        Set<EchoTime> echoTimes = new HashSet<>();
        if (acquisition instanceof MrDatasetAcquisition && dataset instanceof MrDataset) {
            ((MrDataset) dataset).getEchoTime().forEach(eT -> echoTimes.add(eT.getEchoTimeShared()));
        }
        return echoTimes;
    }

}
