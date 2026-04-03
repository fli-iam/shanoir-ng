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

import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.download.AcquisitionAttributes;
import org.shanoir.ng.download.WADODownloaderService;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.studycard.dto.QualityCardResult;
import org.shanoir.ng.studycard.dto.QualityCardResultEntry;
import org.shanoir.ng.studycard.model.QualityCard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class QualityService {

    private static final Logger LOG = LoggerFactory.getLogger(QualityService.class);

    @Autowired
    private WADODownloaderService downloader;

    public QualityCardResult checkQuality(DatasetAcquisition datasetAcquisition,
            AcquisitionAttributes<?> acquisitionAttributes, List<QualityCard> qualityCards) throws ShanoirException {
        QualityCardResult qualityResult = new QualityCardResult();
        for (QualityCard qualityCard : qualityCards) {
            // In case multiple quality cards are used with different roles, we check them
            // all
            qualityResult.merge(qualityCard.apply(datasetAcquisition, acquisitionAttributes, downloader));
            LOG.info("Quality Card {} applied on dataset acquisition {} with result: {}.", qualityCard.getName(),
                    datasetAcquisition.getId(), qualityResult.findById(datasetAcquisition.getId())
                            .map(QualityCardResultEntry::getTagSet).orElse(null));
        }
        return qualityResult;
    }
}
