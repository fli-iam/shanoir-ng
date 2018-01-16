package org.shanoir.ng.importer.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Image {
	
		@JsonProperty("path")
        public String path;
		@JsonProperty("acquisitionNumber")
        public String acquisitionNumber;
		@JsonProperty("echoNumbers")
        public List<Integer> echoNumbers;
		@JsonProperty("imageOrientationPatient")
        public List<Double> imageOrientationPatient;

}
