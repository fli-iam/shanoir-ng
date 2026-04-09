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

package org.shanoir.ng.dicom.web.service;

import java.util.ArrayList;
import java.util.List;

public class DICOMWebSTOWRSResult {

    private int successCount = 0;
    private int warningCount = 0;
    private int failureCount = 0;

    private List<DICOMWebSTOWRSResultInstance> instances = new ArrayList<>();

    public int getSuccessCount() {
        return successCount;
    }

    public int getWarningCount() {
        return warningCount;
    }

    public int getFailureCount() {
        return failureCount;
    }

    public List<DICOMWebSTOWRSResultInstance> getInstances() {
        return instances;
    }

    public void incrementSuccess() {
        successCount++;
    }

    public void incrementWarning() {
        warningCount++;
    }

    public void incrementFailure() {
        failureCount++;
    }

    public void addInstance(DICOMWebSTOWRSResultInstance inst) {
        instances.add(inst);
    }

}
