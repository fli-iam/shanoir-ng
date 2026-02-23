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

package org.shanoir.ng.utils;

import org.shanoir.ng.shared.configuration.RabbitMQConfiguration;
import org.shanoir.ng.study.rights.StudyRightsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

@Service
public class ImportSecurityService {

    @Autowired
    private StudyRightsService rightsService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * Check that the connected user has the given right for the given study.
     *
     * @param studyId the study id
     * @param rightStr the right
     * @return true or false
     */
    public boolean hasRightOnStudy(Long studyId, String rightStr) {
        if (KeycloakUtil.isAdmin()) {
            return true;
        } else if (studyId == null) {
            return false;
        } else {
            return rightsService.hasRightOnStudy(studyId, rightStr);
        }
    }

    /**
     * Check that the connected user has the given right for at least one study.
     *
     * @param rightStr the right
     * @return true or false
     */
    public boolean hasRightOnOneStudy(String rightStr) {
        if (KeycloakUtil.isAdmin()) {
            return true;
        }
        return rightsService.hasRightOnAtLeastOneStudy(rightStr);
    }

    /**
     * Know if connected user can import from PACS
     *
     * @return a boolean
     */
    public boolean canImportFromPACS() {
        if (KeycloakUtil.isAdmin()) {
            return true;
        }
        return KeycloakUtil.canImportFromPACS();
    }

    /**
     * Checks if the user has the required right on the study and the study is not draft.
     *
     * @param studyId the study ID
     * @return true if the the study is draft
     */
    public boolean isDraftStudy(Long studyId, String right) throws EntityNotFoundException {
        try {
            String response = (String) rabbitTemplate.convertSendAndReceive(
                    RabbitMQConfiguration.STUDY_DRAFT_STATE_QUEUE,
                    String.valueOf(studyId)
            );

            if (response == null || "NOT_FOUND".equals(response)) {
                throw new EntityNotFoundException("Cannot find study.");
            }

            if ("ERROR".equals(response)) {
                throw new EntityNotFoundException("Error checking study state.");
            }

            return Boolean.parseBoolean(response);
        } catch (AmqpException e) {
            throw new EntityNotFoundException("Study service unavailable.");
        }
    }
}
