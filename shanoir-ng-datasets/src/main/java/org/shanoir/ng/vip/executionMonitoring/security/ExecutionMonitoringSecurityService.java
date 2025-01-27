package org.shanoir.ng.vip.executionMonitoring.security;

import org.shanoir.ng.vip.executionMonitoring.model.ExecutionMonitoring;
import org.shanoir.ng.study.rights.StudyRightsService;
import org.shanoir.ng.utils.KeycloakUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ExecutionMonitoringSecurityService {

    @Autowired
    private StudyRightsService studyRightsService;

    /**
     * Check that the connected user has the given right for the given execution monitoring.
     *
     * @param executionMonitoring
     * @param rightStr
     * @return
     */
    public boolean hasRightOnExecutionMonitoring(ExecutionMonitoring executionMonitoring, String rightStr){
        if (KeycloakUtil.getTokenRoles().contains("ROLE_ADMIN")) {
            return true;
        }
        if(executionMonitoring == null){
            throw new IllegalArgumentException("ExecutionMonitoring cannot be null here.");
        }
        if(executionMonitoring.getStudyId() == null){
            throw new IllegalArgumentException("Study id cannot be null here.");
        }

        return studyRightsService.hasRightOnStudy(executionMonitoring.getStudyId(), rightStr);
    }

    /**
     * Check that the connected user has the given right to access all the execution monitoring.
     *
     * @return
     */
    public boolean hasRightOnEveryExecutionMonitoring(){
        return KeycloakUtil.getTokenRoles().contains("ROLE_ADMIN");
    }

    /**
     * Filter a list of execution monitoring depending on the connected user rights
     *
     * @param executionMonitorings
     * @param rightStr
     * @return
     */
    public List<ExecutionMonitoring> filterExecutionMonitoringList(List<ExecutionMonitoring> executionMonitorings, String rightStr){
        if(hasRightOnEveryExecutionMonitoring()) return executionMonitorings;

        List<ExecutionMonitoring> validExecutionMonitorings = new ArrayList<>();

        for(ExecutionMonitoring executionMonitoring : executionMonitorings){
            if(executionMonitoring.getStudyId() == null){
                throw new IllegalArgumentException("Study id cannot be null here. Execution monitoring id is : " + executionMonitoring.getId());
            }
            if(studyRightsService.hasRightOnStudy(executionMonitoring.getStudyId(), rightStr)){
                validExecutionMonitorings.add(executionMonitoring);
            }
        }

        return validExecutionMonitorings;
    }


}
