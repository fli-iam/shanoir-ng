package org.shanoir.ng.eeg.model;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.shanoir.ng.dataset.modality.EegDataset;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.shared.hateoas.HalEntity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Event linked to an EEG dataset. Event happening during the acquisition of an EEG.
 * @author JComeD
 *
 */
@Entity
public class Event extends HalEntity {
	
	/** Serial version ID. */
	private static final long serialVersionUID = 1L;

    @JsonProperty("type")
	private String type;
    
    @JsonProperty("description")
	private String description;
    
    @JsonProperty("position")
	private String position;
    
    @JsonProperty("points")
	private int points;
    
    @JsonProperty("channelNumber")
	private int channelNumber;
    
    @JsonProperty("date")
	private Date date;

	/** Associated dataset. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dataset_id")
    @JsonIgnore
	private EegDataset dataset;

    
    
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getPosition() {
		return position;
	}

	public void setPosition(String position) {
		this.position = position;
	}

	public int getPoints() {
		return points;
	}

	public void setPoints(int points) {
		this.points = points;
	}

	public int getChannelNumber() {
		return channelNumber;
	}

	public void setChannelNumber(int channelNumber) {
		this.channelNumber = channelNumber;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}
	
	/**
	 * @return the dataset
	 */
	public EegDataset getDataset() {
		return dataset;
	}

	/**
	 * @param dataset the dataset to set
	 */
	public void setDataset(EegDataset dataset) {
		this.dataset = dataset;
	}
}
