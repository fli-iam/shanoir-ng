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

import java.util.List;

import org.dcm4che3.data.Attributes;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.download.AcquisitionAttributes;
import org.shanoir.ng.download.ExaminationAttributes;
import org.shanoir.ng.download.WADODownloaderService;
import org.shanoir.ng.shared.exception.PacsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonTypeName;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/**
 * Condition on DICOM attributes of the datasets of an examination.
 */
@Entity
@DiscriminatorValue("ExamDICOMConditionOnDatasets")
@JsonTypeName("ExamDICOMConditionOnDatasets")
public class ExamDICOMConditionOnDatasets extends DICOMConditionOnDatasets {

    private static final Logger LOG = LoggerFactory.getLogger(ExamDICOMConditionOnDatasets.class);

    /**
     * Check the conditions on a complete set of already known dicom Attributes
     * @param <T> the type of the used keys
     * @param examinationAttributes complete set of already known dicom Attributes, not a cache
     * @return
     */
    public boolean fulfilled(ExaminationAttributes<?> dicomAttributes) {
        return fulfilled(dicomAttributes, new StringBuffer());
    }

    /**
     * Check the conditions on a complete set of already known dicom Attributes
     * @param <T> the type of the used keys
     * @param examinationAttributes complete set of already known dicom Attributes, not a cache
     * @param report a buffer where you get the execution report
     * @return
     */
    public <T> boolean fulfilled(ExaminationAttributes<T> examinationAttributes, StringBuffer report) {
        if (examinationAttributes == null) throw new IllegalArgumentException("dicomAttributes can not be null");
        int nbOk = 0;
        int total = 0;
        int nbUnknown = 0;
        for (T acqId : examinationAttributes.getAcquisitionIds()) {
            AcquisitionAttributes<T> acqAttributes = examinationAttributes.getAcquisitionAttributes(acqId);
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
        }
        boolean complies = cardinalityComplies(nbOk, nbUnknown, total);
        writeConditionsReport(report, complies, nbOk, nbUnknown, total);
        return complies;
    }

    /**
     * Check condition on exam list of acquisitions without having the dicom attributes in input,
     * so they will be downloaded if needed
     * @param acquisitions data checked
     * @param examinationAttributesCache to be used as a cache
     * @param report where you get report messages
     * @return
     */
    public boolean fulfilled(List<DatasetAcquisition> acquisitions, ExaminationAttributes<Long> examinationAttributesCache, WADODownloaderService downloader, StringBuffer report) {
        if (acquisitions == null) throw new IllegalArgumentException("acquisitions can not be null");
        int nbOk = 0;
        int total = 0;
        int nbUnknown = 0;
        for (DatasetAcquisition acquisition : acquisitions) {
            if (!examinationAttributesCache.has(acquisition.getId())) {
                examinationAttributesCache.addAcquisitionAttributes(acquisition.getId(), new AcquisitionAttributes<Long>());
            }
            AcquisitionAttributes<Long> acqAttributes = examinationAttributesCache.getAcquisitionAttributes(acquisition.getId());
            for (Dataset dataset : acquisition.getDatasets()) {
                total++;
                boolean alreadyFulfilled = getCardinality() >= 1 && nbOk >= getCardinality();
                if (!alreadyFulfilled) {
                    if (!acqAttributes.has(dataset.getId())) {
                        acqAttributes.addDatasetAttributes(dataset.getId(), downloadAttributes(dataset, downloader, report));
                    }
                    if (acqAttributes.getDatasetAttributes(dataset.getId()) == null) { // in case of pacs error
                        nbUnknown++;
                    } else {
                        Boolean fulfilled = fulfilled(acqAttributes.getDatasetAttributes(dataset.getId()), report);
                        if (fulfilled == null) {
                            nbUnknown++;
                        } else if (fulfilled) {
                            nbOk++;
                        }
                    }
                }
            }
        }
        boolean complies = cardinalityComplies(nbOk, nbUnknown, total);
        writeConditionsReport(report, complies, nbOk, nbUnknown, total);
        return complies;
    }

    private Attributes downloadAttributes(Dataset dataset, WADODownloaderService downloader, StringBuffer errorMsg) {
        try {
            Attributes attributes = downloader.getDicomAttributesForDataset(dataset);
            return attributes;
        } catch (PacsException e) {
            if (errorMsg != null) errorMsg.append("\nThe condition [" + toString()
                    + "] was ignored on dataset " + dataset.getId() + " because no dicom data could be found on pacs");
            LOG.warn("The condition [" + toString()
                    + "] was ignored on dataset " + dataset.getId() + " because no dicom data could be found on pacs, reason : " + e.getMessage());
            return null;
        }
    }

}
