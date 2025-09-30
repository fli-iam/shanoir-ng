package org.shanoir.ng.dicom.web.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * This class represents a DICOM-SERIE used for the DICOMweb protocol.
 *
 * See for the standard:
 * https://dicom.nema.org/medical/dicom/current/output/html/part18.html#sect_10.4
 *
 * Goal is to support OHIF viewer, v2.x
 *
 * @author mkain
 *
 */
public class SerieDTO {

	/**
	 * SERIE: 3 attributes; the string ".2." is chosen randomly by MK, it could be any other number
	 * to separate the UIDs of the study, of the serie UIDs, 2==serie
	 */
	// Unique key == RootPrefix + ".2." + DatasetAcquisitionID (to avoid mixing with exams==study)
    @JsonProperty("serieInstanceUID")
	private String serieInstanceUID;

    @JsonProperty("seriesNumber")
	private Integer seriesNumber;

    @JsonProperty("modality")
	private String modality;

    @JsonProperty("instances")
	private List<InstanceDTO> instances;

	public String getSerieInstanceUID() {
		return serieInstanceUID;
	}

	public void setSerieInstanceUID(String serieInstanceUID) {
		this.serieInstanceUID = serieInstanceUID;
	}

	public Integer getSeriesNumber() {
		return seriesNumber;
	}

	public void setSeriesNumber(Integer seriesNumber) {
		this.seriesNumber = seriesNumber;
	}

	public String getModality() {
		return modality;
	}

	public void setModality(String modality) {
		this.modality = modality;
	}

	public List<InstanceDTO> getInstances() {
		return instances;
	}

	public void setInstances(List<InstanceDTO> instances) {
		this.instances = instances;
	}

}
