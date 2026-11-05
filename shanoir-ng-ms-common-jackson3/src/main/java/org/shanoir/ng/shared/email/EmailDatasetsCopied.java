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

package org.shanoir.ng.shared.email;

import java.util.Map;

/**
 * This class represents an EMAIL to be sent after datasets have been copied
 * into a study.
 */
public class EmailDatasetsCopied extends EmailBase {

    private int copiedDatasetCount;

    /** Examinations created in the study by the copy, examination id to label, possibly truncated. */
    private Map<Long, String> examinations;

    /** Total number of examinations created in the study by the copy. */
    private int totalExaminations;

    public int getCopiedDatasetCount() {
        return copiedDatasetCount;
    }

    public void setCopiedDatasetCount(int copiedDatasetCount) {
        this.copiedDatasetCount = copiedDatasetCount;
    }

    public Map<Long, String> getExaminations() {
        return examinations;
    }

    public void setExaminations(Map<Long, String> examinations) {
        this.examinations = examinations;
    }

    public int getTotalExaminations() {
        return totalExaminations;
    }

    public void setTotalExaminations(int totalExaminations) {
        this.totalExaminations = totalExaminations;
    }

}
