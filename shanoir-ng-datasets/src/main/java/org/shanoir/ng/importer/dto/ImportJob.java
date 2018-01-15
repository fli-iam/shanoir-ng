package org.shanoir.ng.importer.dto;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author atouboul
 *
 */
public class ImportJob {

    @JsonProperty("subjects")
    private List<Subject> subjects;

    @JsonProperty("fromDicomZip")
    private boolean fromDicomZip;

    @JsonProperty("fromShanoirUploader")
    private boolean fromShanoirUploader;

    @JsonProperty("fromPacs")
    private boolean fromPacs;

    @JsonProperty("patients")
    private Patients patients;

}
