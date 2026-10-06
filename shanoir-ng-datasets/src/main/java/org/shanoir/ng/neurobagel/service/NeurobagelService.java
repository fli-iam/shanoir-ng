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
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import org.shanoir.ng.neurobagel.dto.NeurobagelDatasetDescriptionDTO;
import org.shanoir.ng.neurobagel.dto.NeurobagelStudyDTO;
import org.shanoir.ng.shared.model.Study;
import org.shanoir.ng.shared.configuration.RabbitMQConfiguration;
import org.shanoir.ng.shared.repository.StudyRepository;
import org.shanoir.ng.tag.model.StudyTag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

/**
 * Studies exposed to the Neurobagel federation: flagged by an admin (neurobagel_export) and not drafts.
 *
 * The Neurobagel export job has no user behind it and bypasses the per-study rights, so every
 * /neurobagel/study/{studyId}/... endpoint must answer 404 unless {@link #isExported(Long)} is true.
 */
@Service
public class NeurobagelService {

    private static final Logger LOG = LoggerFactory.getLogger(NeurobagelService.class);

    /** Neurobagel data dictionary of participants.tsv: its columns are the same for every study. */
    private static final String PARTICIPANTS_DICTIONARY = "neurobagel/participants.json";

    private String participantsDictionary;

    @Autowired
    private StudyRepository studyRepository;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /** Public base URL of the Shanoir front, e.g. https://shanoir.example.org/shanoir-ng/ */
    @Value("${front.server.address}")
    private String frontServerAddress;

    public List<NeurobagelStudyDTO> findExportedStudies() {
        return studyRepository.findByNeurobagelExportTrueAndIsDraftFalseOrderByIdAsc().stream()
                .map(study -> new NeurobagelStudyDTO(study.getId(), study.getName()))
                .toList();
    }

    public boolean isExported(Long studyId) {
        return studyId != null && studyRepository.existsByIdAndNeurobagelExportTrueAndIsDraftFalse(studyId);
    }

    public Optional<NeurobagelStudyDTO> findExportedStudy(Long studyId) {
        if (!isExported(studyId)) {
            return Optional.empty();
        }
        return studyRepository.findById(studyId).map(study -> new NeurobagelStudyDTO(study.getId(), study.getName()));
    }

    /**
     * participants.tsv of an exported study, built by ms studies (which checks the export flag again).
     * Empty when the study is not exported, or when ms studies does not answer.
     */
    public Optional<String> findParticipantsTsv(Long studyId) {
        if (!isExported(studyId)) {
            return Optional.empty();
        }
        Object tsv = rabbitTemplate.convertSendAndReceive(RabbitMQConfiguration.NEUROBAGEL_PARTICIPANTS_TSV,
                String.valueOf(studyId));
        if (tsv == null) {
            LOG.warn("No Neurobagel participants.tsv received from ms studies for exported study {}", studyId);
            return Optional.empty();
        }
        return Optional.of((String) tsv);
    }

    /**
     * Neurobagel data dictionary annotating the columns of every participants.tsv.
     */
    public String getParticipantsDictionary() {
        if (participantsDictionary == null) {
            try (InputStream in = new ClassPathResource(PARTICIPANTS_DICTIONARY).getInputStream()) {
                participantsDictionary = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new UncheckedIOException("Cannot read " + PARTICIPANTS_DICTIONARY, e);
            }
        }
        return participantsDictionary;
    }

    /**
     * Neurobagel dataset description of an exported study. Empty when the study is not exported.
     */
    public Optional<NeurobagelDatasetDescriptionDTO> findDatasetDescription(Long studyId) {
        if (!isExported(studyId)) {
            return Optional.empty();
        }
        return studyRepository.findByIdWithStudyTags(studyId).map(this::toDatasetDescription);
    }

    private NeurobagelDatasetDescriptionDTO toDatasetDescription(Study study) {
        String front = frontServerAddress.endsWith("/") ? frontServerAddress : frontServerAddress + "/";
        String accessLink = front + "access-request/study/" + study.getId();
        NeurobagelDatasetDescriptionDTO description = new NeurobagelDatasetDescriptionDTO();
        description.setName(study.getName());
        description.setKeywords(study.getStudyTags() == null ? List.of()
                : study.getStudyTags().stream().map(StudyTag::getName).sorted().toList());
        description.setReferencesAndLinks(List.of(front + "study/details/" + study.getId()));
        description.setAccessType(NeurobagelDatasetDescriptionDTO.ACCESS_TYPE_RESTRICTED);
        description.setAccessInstructions("The data is hosted in Shanoir. Request access to the study at " + accessLink
                + " (a Shanoir account is needed).");
        description.setAccessLink(accessLink);
        return description;
    }

}
