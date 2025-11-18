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
