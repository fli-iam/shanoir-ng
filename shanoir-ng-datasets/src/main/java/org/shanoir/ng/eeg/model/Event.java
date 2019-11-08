package org.shanoir.ng.eeg.model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.shanoir.ng.dataset.modality.EegDataset;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.shared.hateoas.HalEntity;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Event linked to an EEG dataset. Event happening during the acquisition of an EEG.
 * @author JComeD
 *
 */
@Entity
public class Event extends HalEntity {
	
	/** Serial version ID. */
	private static final long serialVersionUID = 1L;

	/** The "result" of the event, can be successful or not. */
	private String value;
	
	/** A sample of the event. */
	private int sample;

	/** The type of event: interruptino, noise, etc... */
	private String type;

	/** Associated dataset. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dataset_id")
    @JsonIgnore
	private EegDataset dataset;

	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * @param value the value to set
	 */
	public void setValue(String value) {
		this.value = value;
	}

	/**
	 * @return the sample
	 */
	public int getSample() {
		return sample;
	}

	/**
	 * @param sample the sample to set
	 */
	public void setSample(int sample) {
		this.sample = sample;
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
