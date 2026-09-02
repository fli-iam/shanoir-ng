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

package org.shanoir.ng.studycard.model.rule;

import org.shanoir.ng.studycard.model.condition.CardCondition;
import org.shanoir.ng.studycard.model.condition.ExamDICOMConditionOnDatasets;
import org.shanoir.ng.studycard.model.condition.ExamMetadataCondOnAcq;
import org.shanoir.ng.studycard.model.condition.ExamMetadataCondOnDatasets;

import java.util.Comparator;

public class ConditionComparator implements Comparator<CardCondition> {
    @Override
    public int compare(CardCondition cond1, CardCondition cond2) {
        return priority(cond1) - priority(cond2);
    }
    /**
     * the higher the priority, the higher is the returned number.
     */
    private int priority(CardCondition condition) {
        if (condition instanceof ExamDICOMConditionOnDatasets) {
            return 1;
        } else if (condition instanceof ExamMetadataCondOnAcq) {
            return 3;
        } else if (condition instanceof ExamMetadataCondOnDatasets) {
            return 2;
        } else {
            return 0;
        }
    }
}
