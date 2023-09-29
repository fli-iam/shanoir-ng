package org.shanoir.ng.processing.carmin.security;

import org.shanoir.ng.processing.carmin.model.ExecutionMonitoring;
import org.shanoir.ng.study.rights.StudyRightsService;
import org.shanoir.ng.utils.KeycloakUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CarminDatasetProcessingSecurityService {

    @Autowired
    private StudyRightsService studyRightsService;

    /**
     * Check that the connected user has the given right for the given carmin dataset processing.
     *
     * @param executionMonitoring
     * @param rightStr
     * @return
     */
    public boolean hasRightOnCarminDatasetProcessing(ExecutionMonitoring executionMonitoring, String rightStr){
        if (KeycloakUtil.getTokenRoles().contains("ROLE_ADMIN")) {
            return true;
        }
        if(executionMonitoring == null){
            throw new IllegalArgumentException("CarminDatasetProcessing cannot be null here.");
        }
        if(executionMonitoring.getStudyId() == null){
            throw new IllegalArgumentException("Study id cannot be null here.");
        }

        return studyRightsService.hasRightOnStudy(executionMonitoring.getStudyId(), rightStr);
    }

    /**
     * Check that the connected user has the given right to access all the carmin dataset processing.
     *
     * @return
     */
    public boolean hasRightOnEveryCarminDatasetProcessing(){
        return KeycloakUtil.getTokenRoles().contains("ROLE_ADMIN");
    }

    /**
     * Filter a list of carmin dataset processing depending on the connected user rights
     *
     * @param executionMonitorings
     * @param rightStr
     * @return
     */
    public List<ExecutionMonitoring> filterCarminDatasetList(List<ExecutionMonitoring> executionMonitorings, String rightStr){
        if(hasRightOnEveryCarminDatasetProcessing()) return executionMonitorings;

        List<ExecutionMonitoring> validExecutionMonitorings = new ArrayList<>();

        for(ExecutionMonitoring executionMonitoring : executionMonitorings){
            if(executionMonitoring.getStudyId() == null){
                throw new IllegalArgumentException("Study id cannot be null here. carmin datasetId is : "+ executionMonitoring.getId());
            }
            if(studyRightsService.hasRightOnStudy(executionMonitoring.getStudyId(), rightStr)){
                validExecutionMonitorings.add(executionMonitoring);
            }
        }

        return validExecutionMonitorings;
    }


}
