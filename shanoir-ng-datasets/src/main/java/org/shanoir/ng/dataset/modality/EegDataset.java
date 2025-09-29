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

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.eeg.model.Channel;
import org.shanoir.ng.eeg.model.Event;

import java.util.ArrayList;
import java.util.List;

import org.shanoir.ng.dataset.model.DatasetType;

/**
 * EEG dataset.
 * 
 * @author msimon
 *
 */
@Entity
public class EegDataset extends Dataset {
	
	/**
	 * UID
	 */
	private static final long serialVersionUID = -7618433089837302003L;

	@Override
	public DatasetType getType() {
		return DatasetType.Eeg;
	}

	private float samplingFrequency;
	
	private int channelCount;
	
	private String coordinatesSystem;

	@LazyCollection(LazyCollectionOption.FALSE)
	@OneToMany(mappedBy = "dataset", cascade = CascadeType.ALL)
	private List<Channel> channels;

	@LazyCollection(LazyCollectionOption.FALSE)
	@OneToMany(mappedBy = "dataset", cascade = CascadeType.ALL)
	private List<Event> events;

	public EegDataset() { }

	public EegDataset(Dataset other) {
		super(other);
		this.samplingFrequency = ((EegDataset) other).getSamplingFrequency();
		this.channelCount = ((EegDataset) other).getChannelCount();
		this.coordinatesSystem = ((EegDataset) other).getCoordinatesSystem();
		this.channels = new ArrayList<>(((EegDataset) other).getChannels().size());
		for (Channel ch : ((EegDataset) other).getChannels()) {
			this.channels.add(new Channel(ch, this));
		}

		this.events = new ArrayList<>(((EegDataset) other).getEvents().size());
		for (Event ev : ((EegDataset) other).getEvents()) {
			this.events.add(new Event(ev, this));
		}
	}

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
	
	public float getSamplingFrequency() {
		return samplingFrequency;
	}

	public void setSamplingFrequency(float samplingFrequency) {
		this.samplingFrequency = samplingFrequency;
	}

	public int getChannelCount() {
		return channelCount;
	}

	public void setChannelCount(int channelCount) {
		this.channelCount = channelCount;
	}

	/**
	 * @return the channelList
	 */
	public List<Channel> getChannels() {
		return channels;
	}

	/**
	 * @param channelList the channelList to set
	 */
	public void setChannels(List<Channel> channels) {
		this.channels = channels;
	}

	/**
	 * @return the eventList
	 */
	public List<Event> getEvents() {
		return events;
	}

	/**
	 * @param eventList the eventList to set
	 */
	public void setEvents(List<Event> events) {
		this.events = events;
	}
	
}
