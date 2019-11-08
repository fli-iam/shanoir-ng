package org.shanoir.ng.eeg.model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import org.shanoir.ng.dataset.modality.EegDataset;
import org.shanoir.ng.shared.hateoas.HalEntity;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * A channel is a single analog-to-digital converter in the recording system that regularly samples the value of a transducer,
 *  which results in the signal being represented as a time series in the digitized data
 * @author JComeD
 *
 */
@Entity
public class Channel extends HalEntity {

	public enum ChannelType {
		AUDI("Audio signal"),
		EEG("Electroencephalogram channel"),
		EOG("Generic electrooculogram (eye), different from HEOG and VEOG "),
		ECG("Electrocardiogram (heart)"),
		EMG("Electromyogram (muscle)"),
		EYEGAZE("Eye tracker gaze"),
		GSR("Galvanic skin response"),
		HEOG("Horizontal EOG (eye)"),
		MISC("Miscellaneous"),
		PUPIL("Eye tracker pupil diameter"),
		REF("Reference channel"),
		RESP("Respiration"),
		SYSCLOCK("System time showing elapsed time since trial started"),
		TEMP("Temperature"),
		TRIG("System triggers"),
		VEOG("Vertical EOG (eye)");

		private String name;

		ChannelType(String name) {
			this.name = name;
		}

		public String toString(){
			return name;
		}
	}

	/** Serial Version ID. */
	private static final long serialVersionUID = 1L;

	/** Name of the channel. */
	private String name;

	/** Resolution of the channel. */
	private float resolution;

	/** Physical unit of the data values recorded by this channel in SI units. */
	private String referenceUnits;

	/** Type of channel. */
	private ChannelType referenceType;
	
	/** Associated dataset. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dataset_id")
    @JsonIgnore
	private EegDataset dataset;

	/** The set of filters applied to the channel. */
	@OneToOne
	@JoinColumn(name = "filter_id")
	Filter filter;

	/** The position in space of the channel. */
	@OneToOne
	@JoinColumn(name = "position_id")
	Position position;

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the resolution
	 */
	public float getResolution() {
		return resolution;
	}

	/**
	 * @param resolution the resolution to set
	 */
	public void setResolution(float resolution) {
		this.resolution = resolution;
	}

	/**
	 * @return the referenceUnits
	 */
	public String getReferenceUnits() {
		return referenceUnits;
	}

	/**
	 * @param referenceUnits the referenceUnits to set
	 */
	public void setReferenceUnits(String referenceUnits) {
		this.referenceUnits = referenceUnits;
	}

	/**
	 * @return the referenceType
	 */
	public ChannelType getReferenceType() {
		return referenceType;
	}

	/**
	 * @param referenceType the referenceType to set
	 */
	public void setReferenceType(ChannelType referenceType) {
		this.referenceType = referenceType;
	}

	/**
	 * @return the filter
	 */
	public Filter getFilter() {
		return filter;
	}

	/**
	 * @param filter the filter to set
	 */
	public void setFilter(Filter filter) {
		this.filter = filter;
	}

	/**
	 * @return the position
	 */
	public Position getPosition() {
		return position;
	}

	/**
	 * @param position the position to set
	 */
	public void setPosition(Position position) {
		this.position = position;
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
