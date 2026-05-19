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


import java.util.List;

import org.shanoir.ng.dicom.DicomProcessing;
import org.shanoir.ng.download.ExaminationAttributes;
import org.shanoir.ng.download.WADODownloaderService;
import org.shanoir.ng.importer.dto.ImportJob;
import org.shanoir.ng.importer.dto.Study;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.studycard.dto.QualityCardResult;
import org.shanoir.ng.studycard.dto.QualityCardResultEntry;
import org.shanoir.ng.studycard.model.ExaminationData;
import org.shanoir.ng.studycard.model.QualityCard;
import org.shanoir.ng.studycard.service.QualityCardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class QualityService {

    private static final Logger LOG = LoggerFactory.getLogger(QualityService.class);

    @Autowired
    private QualityCardService qualityCardService;

    @Autowired
    private WADODownloaderService downloader;


    public QualityCardResult checkQuality(ExaminationData examination, ImportJob importJob, List<QualityCard> qualityCards) throws ShanoirException {
        LOG.info("Checking quality at import for examination : " + importJob.getExaminationId());

        // If import comes from ShUp QualityCards are loaded, otherwise we query the database to get them
        if (qualityCards == null) {
            qualityCards = qualityCardService.findByStudy(examination.getStudyId());
        } else {
            LOG.info("Quality cards loaded from ShUp");
        }

        if (!hasQualityChecksAtImport(qualityCards)) {
            return new QualityCardResult();
        }
        Study firstStudy = importJob.getFirstStudy();
        if (firstStudy == null) {
            throw new ShanoirException("The given import job does not provide any serie. Examination : " + importJob.getExaminationId());
        }
        ExaminationAttributes<String> dicomAttributes = DicomProcessing.getDicomExaminationAttributes(firstStudy);
        QualityCardResult qualityResult = new QualityCardResult();
        for (QualityCard qualityCard : qualityCards) {
            // In case multiple quality cards are used with different roles, we check them all
            if (qualityCard.isToCheckAtImport()) {
                qualityResult.merge(qualityCard.apply(examination, dicomAttributes, downloader));
            }
        }
        return qualityResult;
    }

    private boolean hasQualityChecksAtImport(List<QualityCard> qualityCards) {
        if (qualityCards == null || qualityCards.isEmpty()) {
            LOG.warn("No qualitycard given for this import.");
            return false;
        }
        for (QualityCard qualityCard : qualityCards) {
            if (qualityCard.isToCheckAtImport()) {
                return true;
            }
        }
        return false;
    }

    public QualityCardResult retrieveQualityCardResult(ImportJob importJob) {
        if (importJob.getQualityTag() == null) {
            return new QualityCardResult();
        }
        QualityCardResult qualityCardResult = new QualityCardResult();
        QualityCardResultEntry qualityCardResultEntry = new QualityCardResultEntry();
        qualityCardResultEntry.setTagSet(importJob.getQualityTag());
        qualityCardResultEntry.setMessage("Tag " + importJob.getQualityTag() + " was applied to examination " + importJob.getExaminationId() + " during quality check at import from Shanoir Uploader.");
        qualityCardResult.add(qualityCardResultEntry);
        return qualityCardResult;
    }
}
