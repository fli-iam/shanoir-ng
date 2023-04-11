package org.shanoir.ng.processing.carmin.security;

import org.shanoir.ng.processing.carmin.model.CarminDatasetProcessing;
import org.shanoir.ng.shared.model.Study;
import org.shanoir.ng.study.rights.StudyRightsService;
import org.shanoir.ng.utils.KeycloakUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class CarminDatasetProcessingSecurityService {

    @Autowired
    private StudyRightsService studyRightsService;

    /**
     * Check that the connected user has the given right for the given carmin dataset processing.
     *
     * @param carminDatasetProcessing
     * @param rightStr
     * @return
     */
    public boolean hasRightOnCarminDatasetProcessing(CarminDatasetProcessing carminDatasetProcessing, String rightStr){
        if (KeycloakUtil.getTokenRoles().contains("ROLE_ADMIN")) {
            return true;
        }
        if(carminDatasetProcessing == null){
            throw new IllegalArgumentException("CarminDatasetProcessing cannot be null here.");
        }
        if(carminDatasetProcessing.getStudyId() == null){
            throw new IllegalArgumentException("Study id cannot be null here.");
        }

        return studyRightsService.hasRightOnStudy(carminDatasetProcessing.getStudyId(), rightStr);
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
     * @param carminDatasetProcessings
     * @param rightStr
     * @return
     */
    public List<CarminDatasetProcessing> filterCarminDatasetList(List<CarminDatasetProcessing> carminDatasetProcessings, String rightStr){
        if(hasRightOnEveryCarminDatasetProcessing()) return carminDatasetProcessings;

        List<CarminDatasetProcessing> validCarminDatasetProcessings = new ArrayList<>();

        for(CarminDatasetProcessing carminDatasetProcessing : carminDatasetProcessings){
            if(carminDatasetProcessing.getStudyId() == null){
                throw new IllegalArgumentException("Study id cannot be null here. carmin datasetId is : "+carminDatasetProcessing.getId());
            }
            if(studyRightsService.hasRightOnStudy(carminDatasetProcessing.getStudyId(), rightStr)){
                validCarminDatasetProcessings.add(carminDatasetProcessing);
            }
        }

        return validCarminDatasetProcessings;
    }


}
