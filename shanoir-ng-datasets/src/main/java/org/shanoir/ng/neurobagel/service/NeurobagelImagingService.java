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

package org.shanoir.ng.neurobagel.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;

import org.shanoir.ng.neurobagel.repository.NeurobagelImagingRepository;
import org.shanoir.ng.neurobagel.repository.NeurobagelImagingRow;
import org.shanoir.ng.neurobagel.service.NeurobagelSuffix.Modality;
import org.shanoir.ng.shared.configuration.RabbitMQConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Builds the Neurobagel imaging table of a study (input of 'bagel bids --bids-table') for ms studies,
 * which serves it. The table can be several MB: it is written into the bids-data volume shared with
 * ms studies, and only its file name travels through RabbitMQ. ms studies reads and deletes the file.
 *
 * ms studies only asks for exported studies (it owns the export flag).
 */
@Service
public class NeurobagelImagingService {

    private static final Logger LOG = LoggerFactory.getLogger(NeurobagelImagingService.class);

    /** Sub-folder of the bids-data volume, shared with ms studies. */
    public static final String FOLDER = "neurobagel";

    private static final String HEADER = "sub\tses\tsuffix\tpath\n";

    @Autowired
    private NeurobagelImagingRepository imagingRepository;

    @Value("${storage.file-system.bids-data}")
    private String bidsDataDir;

    /**
     * Request from ms studies: writes the imaging table of the study and replies with the file name,
     * or null when it cannot be written (ms studies then answers 404 rather than waiting for a timeout).
     */
    @RabbitListener(queues = RabbitMQConfiguration.NEUROBAGEL_IMAGING_TSV, containerFactory = "singleConsumerFactory")
    public String writeImagingTsv(Long studyId) {
        try {
            Path folder = Path.of(bidsDataDir, FOLDER);
            Files.createDirectories(folder);
            String fileName = "study-" + studyId + "-imaging-" + UUID.randomUUID() + ".tsv";
            Files.writeString(folder.resolve(fileName), buildImagingTsv(studyId), StandardCharsets.UTF_8);
            return fileName;
        } catch (IOException | RuntimeException e) {
            LOG.error("Cannot write the Neurobagel imaging table of study {}", studyId, e);
            return null;
        }
    }

    /**
     * One row per acquired dataset with a Neurobagel suffix. Datasets without one are left out and counted
     * in the logs. Header only when no dataset has a suffix.
     */
    public String buildImagingTsv(Long studyId) {
        StringBuilder tsv = new StringBuilder(HEADER);
        int exported = 0;
        int leftOut = 0;
        for (NeurobagelImagingRow row : imagingRepository.findImagingRows(studyId)) {
            Optional<String> suffix = NeurobagelSuffix.of(modality(row.getModality()),
                    row.getMrDatasetNature(), row.getMrSequenceApplication());
            if (suffix.isEmpty()) {
                leftOut++;
                continue;
            }
            // bagel only requires a non-empty path: the session path it publishes is built from sub and ses
            tsv.append("sub-").append(row.getSubjectId()).append('\t')
                    .append("ses-").append(row.getExaminationId()).append('\t')
                    .append(suffix.get()).append('\t')
                    .append("/shanoir/examination/").append(row.getExaminationId())
                    .append("/dataset/").append(row.getDatasetId()).append('\n');
            exported++;
        }
        LOG.info("Neurobagel imaging table of study {}: {} datasets exported, {} left out (no Neurobagel suffix)",
                studyId, exported, leftOut);
        return tsv.toString();
    }

    private static Modality modality(String name) {
        try {
            return Modality.valueOf(name);
        } catch (IllegalArgumentException | NullPointerException e) {
            return Modality.OTHER;
        }
    }

}
