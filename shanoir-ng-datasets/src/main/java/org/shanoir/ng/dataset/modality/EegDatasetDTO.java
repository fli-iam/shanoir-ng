package org.shanoir.ng.dataset.modality;

import java.util.List;

import org.shanoir.ng.dataset.dto.DatasetDTO;
import org.shanoir.ng.eeg.model.Channel;
import org.shanoir.ng.eeg.model.Event;

public class EegDatasetDTO extends DatasetDTO {
	
	private float samplingFrequency;
	
	private int channelCount;

	private List<Channel> channelList;

	private List<Event> eventList;
	
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

	/**
	 * @return the channelList
	 */
	public List<Channel> getChannelList() {
		return channelList;
	}

	/**
	 * @param channelList the channelList to set
	 */
	public void setChannelList(List<Channel> channelList) {
		this.channelList = channelList;
	}

	/**
	 * @return the eventList
	 */
	public List<Event> getEventList() {
		return eventList;
	}

	/**
	 * @param eventList the eventList to set
	 */
	public void setEventList(List<Event> eventList) {
		this.eventList = eventList;
	}
}
