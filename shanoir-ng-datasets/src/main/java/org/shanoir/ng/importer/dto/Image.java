package org.shanoir.ng.importer.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Image {
	
		@JsonProperty("path")
        public String path;
		@JsonProperty("acquisitionNumber")
        public String acquisitionNumber;
		@JsonProperty("echoNumbers")
        public EchoNumbers echoNumbers;
		@JsonProperty("imageOrientationPatient")
        public ImageOrientationPatient imageOrientationPatient;

}
