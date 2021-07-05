package org.shanoir.ng.eeg.model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.shanoir.ng.dataset.modality.EegDataset;
import org.shanoir.ng.shared.hateoas.HalEntity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

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

		@Override
		public String toString(){
			return name;
		}
	}

	/** Serial Version ID. */
	private static final long serialVersionUID = 1L;

	/** Name of the channel. */
	@JsonProperty("name")
	private String name;

	/** Resolution of the channel. */
	@JsonProperty("resolution")
	private float resolution;

	/** Physical unit of the data values recorded by this channel in SI units. */
	@JsonProperty("referenceUnits")
	private String referenceUnits;

	/** Type of channel. */
	@JsonProperty("referenceType")
	private ChannelType referenceType;
	
	/** Low cutoff filter value. */
	@JsonProperty("lowCutoff")
	private int lowCutoff;
	
	/** High cutoff filter value. */
	@JsonProperty("highCutoff")
	private int highCutoff;
	
	/** Notch filter value. */
	@JsonProperty("notch")
	private float notch;
	
	/** X position in space. */
	@JsonProperty("x")
	private int x;
	
	/** Y position in space. */
	@JsonProperty("y")
	private int y;
	
	/** Z position in space. */
	@JsonProperty("z")
	private int z;

	/** Associated dataset. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dataset_id")
    @JsonIgnore
	private EegDataset dataset;

	/**
	 * @return the lowCutOff
	 */
	public int getLowCutoff() {
		return lowCutoff;
	}

	/**
	 * @param lowCutOff the lowCutOff to set
	 */
	public void setLowCutoff(int lowCutOff) {
		this.lowCutoff = lowCutOff;
	}

	/**
	 * @return the highCutOff
	 */
	public int getHighCutoff() {
		return highCutoff;
	}

	/**
	 * @param highCutOff the highCutOff to set
	 */
	public void setHighCutoff(int highCutOff) {
		this.highCutoff = highCutOff;
	}

	/**
	 * @return the notch
	 */
	public float getNotch() {
		return notch;
	}

	/**
	 * @param notch the notch to set
	 */
	public void setNotch(float notch) {
		this.notch = notch;
	}

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
	 * @return the x
	 */
	public int getX() {
		return x;
	}

	/**
	 * @param x the x to set
	 */
	public void setX(int x) {
		this.x = x;
	}

	/**
	 * @return the y
	 */
	public int getY() {
		return y;
	}

	/**
	 * @param y the y to set
	 */
	public void setY(int y) {
		this.y = y;
	}

	/**
	 * @return the z
	 */
	public int getZ() {
		return z;
	}

	/**
	 * @param z the z to set
	 */
	public void setZ(int z) {
		this.z = z;
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
