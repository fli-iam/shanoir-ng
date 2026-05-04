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

package org.shanoir.ng.studycard.model.condition;

import org.shanoir.ng.download.AcquisitionAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonTypeName;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;


/**
 * Condition on DICOM attributes of the datasets of an acquisition.
 */
@Entity
@DiscriminatorValue("AcqDICOMConditionOnDatasets")
@JsonTypeName("AcqDICOMConditionOnDatasets")
public class AcqDICOMConditionOnDatasets extends DICOMConditionOnDatasets {

    private static final Logger LOG = LoggerFactory.getLogger(AcqDICOMConditionOnDatasets.class);


    /**
     * Check condition on acquisitions
     * so they will be downloaded if needed
     * @param acquisitions data checked
     * @param examinationAttributesCache to be used as a cache
     * @return
     */
    public boolean fulfilled(AcquisitionAttributes<?> acqAttributes) {
        return fulfilled(acqAttributes, new StringBuffer());
    }

    /**
     * Check condition on acquisitions
     * @param <T>
     * @param acqAttributes dicom attributes for the acquisition to be checked
     * @param report where you get report messages
     * @return
     */
    public <T> boolean fulfilled(AcquisitionAttributes<T> acqAttributes, StringBuffer report) {
        if (acqAttributes == null) throw new IllegalArgumentException("dicomAttributes can not be null");
        int nbOk = 0;
        int total = 0;
        int nbUnknown = 0;
        for (T datasetId : acqAttributes.getDatasetIds()) {
            total++;
            boolean alreadyFulfilled = getCardinality() >= 1 && nbOk >= getCardinality();
            if (!alreadyFulfilled) {
                Boolean fulfilled = fulfilled(acqAttributes.getDatasetAttributes(datasetId), report);
                if (fulfilled == null) {
                    nbUnknown++;
                } else if (fulfilled) {
                    nbOk++;
                }
            }
        }
        boolean complies = cardinalityComplies(nbOk, nbUnknown, total);
        writeConditionsReport(report, complies, nbOk, nbUnknown, total);
        return complies;
    }



}
