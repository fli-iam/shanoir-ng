package org.shanoir.ng.dicom.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * This class represents a DICOM-INSTANCE used for the DICOMweb protocol.
 *
 * See for the standard:
 * https://dicom.nema.org/medical/dicom/current/output/html/part18.html#sect_10.4
 *
 * Goal is to support OHIF viewer, v2.x
 *
 * @author mkain
 *
 */
public class InstanceDTO {

	/**
	 * INSTANCE: 2 attributes
	 */
    @JsonProperty("metadata")
	private MetadataDTO metadata;

    @JsonProperty("url")
	private String url;

	public MetadataDTO getMetadata() {
		return metadata;
	}

	public void setMetadata(MetadataDTO metadata) {
		this.metadata = metadata;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

}
