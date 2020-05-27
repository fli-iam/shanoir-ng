package org.shanoir.ng.dataset.modality;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Class to export EEG dataset in BIDS format beautifully.
 * @author JcomeD
 *
 */
public class EegDataSetDescription {

	@JsonProperty("TaskName")
	private String taskName;

	@JsonProperty("EEGreference")
	private String eegreference = "CMS";

	@JsonProperty("SamplingFrequency")
	private String samplingFrequency;
	
	@JsonProperty("PowerLineFrequency")
	private String powerLineFrequency = "50";
	
	@JsonProperty("SoftwareFilters")
	private String softwareFilters = "n/a";

	public String getTaskName() {
		return taskName;
	}

	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}

	public String getEegreference() {
		return eegreference;
	}

	public void setEegreference(String eegreference) {
		this.eegreference = eegreference;
	}

	public String getSamplingFrequency() {
		return samplingFrequency;
	}

	public void setSamplingFrequency(String samplingFrequency) {
		this.samplingFrequency = samplingFrequency;
	}

	public String getPowerLineFrequency() {
		return powerLineFrequency;
	}

	public void setPowerLineFrequency(String powerLineFrequency) {
		this.powerLineFrequency = powerLineFrequency;
	}

	public String getSoftwareFilters() {
		return softwareFilters;
	}

	public void setSoftwareFilters(String softwareFilters) {
		this.softwareFilters = softwareFilters;
	}
}
