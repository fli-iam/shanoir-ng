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
