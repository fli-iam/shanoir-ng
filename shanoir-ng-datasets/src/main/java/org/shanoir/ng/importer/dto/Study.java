package org.shanoir.ng.importer.dto;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;


/**
 * @author atouboul
 *
 */

 public class Study {

     @JsonProperty("studyInstanceUID")
     public String studyInstanceUID;

     @JsonProperty("studyDate")
     public String studyDate;

     @JsonProperty("studyDescription")
     public String studyDescription;

     @JsonProperty("series")
     public List<Serie> series;

}
