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

package org.shanoir.ng.importer.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class EegDataset {

    /** List of channels for the eeg dataset. */
    @JsonProperty("channels")
    private List<Channel> channels;

    /** List of events for the eeg dataset. */
    @JsonProperty("events")
    private List<Event> events;

    /** Name of the file -> name of the dataset created. */
    @JsonProperty("name")
    private String name;

    @JsonProperty("files")
    private List<String> files;

    @JsonProperty("samplingFrequency")
    private int samplingFrequency;

    @JsonProperty("channelCount")
    private int channelCount;

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

    public int getChannelCount() {
        return channelCount;
    }

    public void setChannelCount(int channelCount) {
        this.channelCount = channelCount;
    }

    public int getSamplingFrequency() {
        return samplingFrequency;
    }

    public void setSamplingFrequency(int samplingFrequency) {
        this.samplingFrequency = samplingFrequency;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
