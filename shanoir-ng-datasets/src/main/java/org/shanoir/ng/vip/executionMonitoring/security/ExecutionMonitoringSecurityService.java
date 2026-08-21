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

package org.shanoir.ng.vip.executionMonitoring.security;

import java.util.List;
import java.util.Optional;

import org.shanoir.ng.study.rights.StudyRightsService;
import org.shanoir.ng.utils.KeycloakUtil;
import org.shanoir.ng.vip.executionMonitoring.model.ExecutionMonitoring;
import org.shanoir.ng.vip.executionMonitoring.repository.ExecutionMonitoringRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ExecutionMonitoringSecurityService {

    @Autowired
    private StudyRightsService studyRightsService;

    @Autowired
    private ExecutionMonitoringRepository executionMonitoringRepository;

    /**
     * Check that the connected user has the given right for the given execution monitoring relative to the id paramater.
     *
     * @param id
     * @param rightStr
     * @return boolean
     */
    public boolean hasRightOnExecutionMonitoringById(Long id, String rightStr) {
        Optional<ExecutionMonitoring> monitoring = executionMonitoringRepository.findById(id);
        if (monitoring.isPresent()) {
            return hasRightOnExecutionMonitoring(executionMonitoringRepository.findById(id).get(), rightStr);
        } else {
            return true;
        }
    }

    /**
     * Filter a list of execution monitoring depending on the connected user rights
     *
     * @param executionMonitorings
     * @param rightStr
     * @return
     */
    public List<ExecutionMonitoring> filterExecutionMonitoringList(List<ExecutionMonitoring> executionMonitorings, String rightStr) {
        if (KeycloakUtil.getTokenRoles().contains("ROLE_ADMIN")) {
            return executionMonitorings;
        } else {
            return executionMonitorings.stream().filter(it -> hasRightOnExecutionMonitoring(it, rightStr)).toList();
        }
    }

    private boolean hasRightOnExecutionMonitoring(ExecutionMonitoring executionMonitoring, String rightStr) {
        if (executionMonitoring.getStudyId() == null) {
            throw new IllegalArgumentException("Study id cannot be null here.");
        }
        return studyRightsService.hasRightOnStudy(executionMonitoring.getStudyId(), rightStr);
    }

}
