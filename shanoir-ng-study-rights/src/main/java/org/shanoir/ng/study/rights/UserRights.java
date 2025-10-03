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

package org.shanoir.ng.study.rights;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.shanoir.ng.shared.security.rights.StudyUserRight;
import org.springframework.util.CollectionUtils;

public class UserRights {

    private Map<Long, StudyUser> studyRights = new HashMap<>();

    public UserRights(List<StudyUser> studyUsers) {
        if (studyUsers != null) {
            for (StudyUser su : studyUsers) {
                if (su.isConfirmed()) {
                    studyRights.put(su.getStudyId(), su);
                }
            }
        }
    }

    /**
     * Has the user the specified right for the study
     */
    public boolean hasStudyRights(Long studyId, String rightStr) {
        if (studyRights.containsKey(studyId)) {
            StudyUser su = studyRights.get(studyId);
            return su.getStudyUserRights().contains(StudyUserRight.valueOf(rightStr));
        } else {
            return false;
        }
    }

    /**
     * Has the user specific center restrictions for this study
     */
    public boolean hasCenterRestrictionsFor(Long studyId) {
        if (studyRights.containsKey(studyId)) {
            StudyUser su = studyRights.get(studyId);
            return !CollectionUtils.isEmpty(su.getCenterIds());
        } else {
            return false;
        }
    }

    /**
     * Has the user right to access this study-center (including no center restriction)
     */
    public boolean hasStudyCenterRights(Long studyId, Long centerId) {
        if (studyRights.containsKey(studyId)) {
            StudyUser su = studyRights.get(studyId);
            if (!CollectionUtils.isEmpty(su.getCenterIds())) {
                return su.getCenterIds().contains(centerId);
            } else {
                return true;
            }
        } else {
            return false;
        }
    }

    /**
     * Has the user the specified right string for the study and also rights for this center (or no center restriction)
     */
    public boolean hasStudyCenterRights(Long studyId, Long centerId, String rightStr) {
        if (hasStudyRights(studyId, rightStr)) {
            if (hasCenterRestrictionsFor(studyId)) {
                return hasStudyCenterRights(studyId, centerId);
            } else {
                return true;
            }
        } else {
            return false;
        }
    }

    /**
     * Has the user the specified right string for each study and also rights for each study-center (or no center restriction)
     */
    public boolean hasStudiesCenterRights(Iterable<Long> studyIds, Long centerId, String rightStr) {
        for (Long studyId : studyIds) {
            if (!hasStudyCenterRights(studyId, centerId, rightStr)) {
                return false;
            }
        }
        return true;
    }
}
