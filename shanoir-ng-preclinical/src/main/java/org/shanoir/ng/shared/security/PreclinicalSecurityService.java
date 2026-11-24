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

package org.shanoir.ng.shared.security;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.shanoir.ng.shared.configuration.RabbitMQConfiguration;
import org.shanoir.ng.shared.dto.ExaminationStudyCenterDTO;
import org.shanoir.ng.study.rights.StudyRightsService;
import org.shanoir.ng.study.rights.UserRights;
import org.shanoir.ng.utils.KeycloakUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

/**
 * Study rights checks of ms preclinical, used in the @PreAuthorize expressions as @preclinicalSecurityService.
 *
 * The preclinical data is attached to examinations (anesthetics, extra data) or subjects (pathologies, therapies),
 * whose study, and center for examinations, are only known by ms datasets: they are asked for each check, then the
 * rights are checked on the local copy of the study members (kept up to date from ms studies).
 *
 * The answers of ms datasets are not cached on purpose: an admin can move an examination to another study
 * or center, and no event would invalidate the cache.
 *
 * Every check refuses access when the study cannot be found (unknown id, ms datasets not answering).
 */
@Service
public class PreclinicalSecurityService {

    private static final Logger LOG = LoggerFactory.getLogger(PreclinicalSecurityService.class);

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private StudyRightsService studyRightsService;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Check that the connected user has the given right on the study of the given examination,
     * and on its center if the user is restricted to some centers of the study.
     *
     * @param examinationId the examination id
     * @param rightStr the right, e.g. CAN_IMPORT
     * @return true if the user is an admin or has the right, false otherwise
     */
    public boolean hasRightOnExamination(Long examinationId, String rightStr) {
        if (KeycloakUtil.isAdmin()) {
            return true;
        }
        ExaminationStudyCenterDTO studyCenter = findExaminationStudyCenter(examinationId);
        if (studyCenter == null || studyCenter.getStudyId() == null) {
            return false;
        }
        return studyRightsService.hasRightOnStudy(studyCenter.getStudyId(), rightStr)
                && studyRightsService.hasRightOnCenter(studyCenter.getStudyId(), studyCenter.getCenterId());
    }

    /**
     * Check that the connected user has the given right on the study of the given subject.
     *
     * @param subjectId the subject id (the same as its animal subject id)
     * @param rightStr the right, e.g. CAN_SEE_ALL
     * @return true if the user is an admin or has the right, false otherwise
     */
    public boolean hasRightOnSubject(Long subjectId, String rightStr) {
        if (KeycloakUtil.isAdmin()) {
            return true;
        }
        Long studyId = findSubjectStudyId(subjectId);
        if (studyId == null) {
            return false;
        }
        return studyRightsService.hasRightOnStudy(studyId, rightStr);
    }

    /**
     * Check that the connected user has the given right on the given study,
     * for the creations, the new entity not being known by ms datasets yet.
     *
     * @param studyId the study id
     * @param rightStr the right, e.g. CAN_IMPORT
     * @return true if the user is an admin or has the right, false otherwise (and for a null study)
     */
    public boolean hasRightOnStudy(Long studyId, String rightStr) {
        if (KeycloakUtil.isAdmin()) {
            return true;
        }
        if (studyId == null) {
            return false;
        }
        return studyRightsService.hasRightOnStudy(studyId, rightStr);
    }

    /**
     * Keeps the subjects of a list on whose study the connected user has the given right,
     * asking ms datasets the studies of all the subjects at once.
     *
     * @param subjectIds the subject ids
     * @param rightStr the right, e.g. CAN_SEE_ALL
     * @return all the subject ids for an admin, otherwise the ones the user has the right on, in the same order
     *         (none if ms datasets did not answer)
     */
    public List<Long> filterSubjectIdsHasRight(List<Long> subjectIds, String rightStr) {
        if (subjectIds == null || subjectIds.isEmpty() || KeycloakUtil.isAdmin()) {
            return subjectIds;
        }
        Map<Long, Long> studyIds = findSubjectsStudyIds(subjectIds);
        if (studyIds.isEmpty()) {
            return List.of();
        }
        Set<Long> allowedStudyIds = studyRightsService.hasRightOnStudies(new HashSet<>(studyIds.values()), rightStr);
        return subjectIds.stream()
                .filter(subjectId -> studyIds.containsKey(subjectId) && allowedStudyIds.contains(studyIds.get(subjectId)))
                .toList();
    }

    /**
     * Keeps the examinations of a list on whose study the connected user has the given right, and on whose
     * center if the user is restricted to some centers of the study, asking ms datasets the studies and centers
     * of all the examinations at once.
     *
     * @param examinationIds the examination ids
     * @param rightStr the right, e.g. CAN_SEE_ALL
     * @return all the examination ids for an admin, otherwise the ones the user has the right on, in the same order
     *         (none if ms datasets did not answer)
     */
    public List<Long> filterExaminationIdsHasRight(List<Long> examinationIds, String rightStr) {
        if (examinationIds == null || examinationIds.isEmpty() || KeycloakUtil.isAdmin()) {
            return examinationIds;
        }
        Map<Long, ExaminationStudyCenterDTO> studyCenters = findExaminationsStudyCenters(examinationIds);
        if (studyCenters.isEmpty()) {
            return List.of();
        }
        UserRights userRights = studyRightsService.getUserRights();
        return examinationIds.stream()
                .filter(examinationId -> {
                    ExaminationStudyCenterDTO studyCenter = studyCenters.get(examinationId);
                    return studyCenter != null && studyCenter.getStudyId() != null
                            && userRights.hasStudyCenterRights(studyCenter.getStudyId(), studyCenter.getCenterId(), rightStr);
                })
                .toList();
    }

    /**
     * Check that the connected user has the given right on at least one study,
     * for the actions that are not attached to a study yet (e.g. a Bruker upload before its import).
     *
     * @param rightStr the right, e.g. CAN_IMPORT
     * @return true if the user is an admin or has the right on one study at least, false otherwise
     */
    public boolean hasRightOnAtLeastOneStudy(String rightStr) {
        if (KeycloakUtil.isAdmin()) {
            return true;
        }
        return studyRightsService.hasRightOnAtLeastOneStudy(rightStr);
    }

    /**
     * Asks ms datasets the study and center of an examination.
     *
     * @param examinationId the examination id
     * @return the study and center, null if unknown or if ms datasets did not answer
     */
    private ExaminationStudyCenterDTO findExaminationStudyCenter(Long examinationId) {
        if (examinationId == null) {
            return null;
        }
        try {
            Object reply = rabbitTemplate.convertSendAndReceive(
                    RabbitMQConfiguration.EXAMINATION_STUDY_CENTER_QUEUE, examinationId);
            if (!(reply instanceof String json) || json.isEmpty()) {
                LOG.warn("No study found by ms datasets for examination {}: access refused", examinationId);
                return null;
            }
            return objectMapper.readValue(json, ExaminationStudyCenterDTO.class);
        } catch (AmqpException | JacksonException e) {
            LOG.error("Cannot get the study of examination {} from ms datasets: access refused", examinationId, e);
            return null;
        }
    }

    /**
     * Asks ms datasets the studies and centers of several examinations at once.
     *
     * @param examinationIds the examination ids
     * @return the study and center by examination id, without the unknown examinations;
     *         empty if ms datasets did not answer
     */
    private Map<Long, ExaminationStudyCenterDTO> findExaminationsStudyCenters(List<Long> examinationIds) {
        try {
            Object reply = rabbitTemplate.convertSendAndReceive(RabbitMQConfiguration.EXAMINATIONS_STUDY_CENTERS_QUEUE,
                    objectMapper.writeValueAsString(examinationIds));
            if (!(reply instanceof String json) || json.isEmpty()) {
                LOG.warn("No studies found by ms datasets for examinations {}: access refused", examinationIds);
                return Map.of();
            }
            return objectMapper.readValue(json, new TypeReference<Map<Long, ExaminationStudyCenterDTO>>() { });
        } catch (AmqpException | JacksonException e) {
            LOG.error("Cannot get the studies of examinations {} from ms datasets: access refused", examinationIds, e);
            return Map.of();
        }
    }

    /**
     * Asks ms datasets the studies of several subjects at once.
     *
     * @param subjectIds the subject ids
     * @return the study id by subject id, without the unknown subjects; empty if ms datasets did not answer
     */
    private Map<Long, Long> findSubjectsStudyIds(List<Long> subjectIds) {
        try {
            Object reply = rabbitTemplate.convertSendAndReceive(
                    RabbitMQConfiguration.SUBJECTS_STUDY_IDS_QUEUE, objectMapper.writeValueAsString(subjectIds));
            if (!(reply instanceof String json) || json.isEmpty()) {
                LOG.warn("No studies found by ms datasets for subjects {}: access refused", subjectIds);
                return Map.of();
            }
            return objectMapper.readValue(json, new TypeReference<Map<Long, Long>>() { });
        } catch (AmqpException | JacksonException e) {
            LOG.error("Cannot get the studies of subjects {} from ms datasets: access refused", subjectIds, e);
            return Map.of();
        }
    }

    /**
     * Asks ms datasets the study of a subject.
     *
     * @param subjectId the subject id
     * @return the study id, null if unknown or if ms datasets did not answer
     */
    private Long findSubjectStudyId(Long subjectId) {
        if (subjectId == null) {
            return null;
        }
        try {
            Object reply = rabbitTemplate.convertSendAndReceive(RabbitMQConfiguration.SUBJECT_STUDY_ID_QUEUE, subjectId);
            if (!(reply instanceof Number studyId) || studyId.longValue() == RabbitMQConfiguration.UNKNOWN_STUDY_ID) {
                LOG.warn("No study found by ms datasets for subject {}: access refused", subjectId);
                return null;
            }
            return studyId.longValue();
        } catch (AmqpException e) {
            LOG.error("Cannot get the study of subject {} from ms datasets: access refused", subjectId, e);
            return null;
        }
    }

}
