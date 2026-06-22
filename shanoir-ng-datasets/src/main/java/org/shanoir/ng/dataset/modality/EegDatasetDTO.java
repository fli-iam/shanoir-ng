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

import java.util.List;

import org.shanoir.ng.dataset.dto.DatasetDTO;
import org.shanoir.ng.eeg.model.Channel;
import org.shanoir.ng.eeg.model.Event;

import com.fasterxml.jackson.annotation.JsonProperty;

public class EegDatasetDTO extends DatasetDTO {

    private int channelCount;

    @JsonProperty("channels")
    private List<Channel> channels;

    @JsonProperty("events")
    private List<Event> events;

    @JsonProperty("files")
    private List<String> files;

    @JsonProperty("samplingFrequency")
    private float samplingFrequency;

    @JsonProperty("coordinatesSystem")
    private String coordinatesSystem;


    /**
     * @return the coordinatesSystem
     */
    public String getCoordinatesSystem() {
        return coordinatesSystem;
    }

    /**
     * @param coordinatesSystem the coordinatesSystem to set
     */
    public void setCoordinatesSystem(String coordinatesSystem) {
        this.coordinatesSystem = coordinatesSystem;
    }

    /**
     * @return the samplingFrequency
     */
    public float getSamplingFrequency() {
        return samplingFrequency;
    }

    /**
     * @param samplingFrequency the samplingFrequency to set
     */
    public void setSamplingFrequency(float samplingFrequency) {
        this.samplingFrequency = samplingFrequency;
    }

    /**
     * @return the channelCount
     */
    public int getChannelCount() {
        return channelCount;
    }

    /**
     * @param channelCount the channelCount to set
     */
    public void setChannelCount(int channelCount) {
        this.channelCount = channelCount;
    }

    public List<Channel> getChannels() {
        return channels;
    }

    public void setChannels(List<Channel> channels) {
        this.channels = channels;
    }

    public List<Event> getEvents() {
        return events;
    }

    public void setEvents(List<Event> events) {
        this.events = events;
    }

    public List<String> getFiles() {
        return files;
    }

    public void setFiles(List<String> files) {
        this.files = files;
    }

}
