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

import java.util.ArrayList;
import java.util.List;

public class ConditionResult {

    private boolean fulfilled;
    private List<String> fulfilledConditionsMsgList = new ArrayList<>();
    private List<String> unfulfilledConditionsMsgList = new ArrayList<>();

    public boolean isFulfilled() {
        return fulfilled;
    }

    public void setFulfilled(boolean fulfilled) {
        this.fulfilled = fulfilled;
    }

    public List<String> getFulfilledConditionsMsgList() {
        return fulfilledConditionsMsgList;
    }

    public void addFulfilledConditionsMsg(String msg) {
        this.fulfilledConditionsMsgList.add(msg);
    }

    public List<String> getUnfulfilledConditionsMsgList() {
        return unfulfilledConditionsMsgList;
    }

    public void addUnfulfilledConditionsMsg(String msg) {
        this.unfulfilledConditionsMsgList.add(msg);
    }
}
