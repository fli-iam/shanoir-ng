package org.shanoir.ng.vip.executionMonitoring.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Execution Status
 *
 * @author KhalilKes
 */
public enum ExecutionStatus {

    RUNNING("Running"),
    FINISHED("Finished"),
    EXECUTION_FAILED("ExecutionFailed"),
    UNKNOWN("Unknown"),
    KILLED("Killed");

    private String restLabel;

    ExecutionStatus(String restLabel) {
        this.restLabel = restLabel;
    }

    @JsonCreator
    public static ExecutionStatus fromRestLabel(String restlabel) {
        for (ExecutionStatus status : values()) {
            if (status.restLabel.equals(restlabel)) { return status; }
        }
        throw new IllegalArgumentException("Unknown execution status : " + restlabel);
    }

    @JsonValue
    public String getRestLabel() {
        return restLabel;
    }

}
