package org.shanoir.ng.importer.model;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author atouboul
 *
 */

public class Patient {
     @JsonProperty("patientID")
     public String patientID;

     @JsonProperty("patientName")
     public String patientName;

     @JsonProperty("patientBirthDate")
     public String patientBirthDate;

     @JsonProperty("patientSex")
     public String patientSex;

     @JsonProperty("studies")
     public List<Study> studies;

}
