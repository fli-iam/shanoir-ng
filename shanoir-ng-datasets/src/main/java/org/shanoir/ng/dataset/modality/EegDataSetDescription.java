/**
 * Shanoir NG - Import, manage and share neuroimaging data
 * Copyright (C) 2009-2019 Inria - https://www.inria.fr/
 * Contact us on https://project.inria.fr/shanoir/
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/gpl-3.0.html
 */

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
