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
