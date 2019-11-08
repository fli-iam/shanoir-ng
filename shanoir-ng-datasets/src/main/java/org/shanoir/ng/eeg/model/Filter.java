package org.shanoir.ng.eeg.model;

import javax.persistence.Entity;

import org.shanoir.ng.shared.hateoas.HalEntity;

/**
 * Filters applied during EEG acquisition for a given channel.
 * @author JComeD
 *
 */
@Entity
public class Filter extends HalEntity {
	
	/** Serial version UID. */
	private static final long serialVersionUID = 1L;

	/** Low cutoff filter value. */
	private int lowCutOff;
	
	/** High cutoff filter value. */
	private int highCutOff;
	
	/** Notch filter value. */
	private float notch;

	/**
	 * @return the lowCutOff
	 */
	public int getLowCutOff() {
		return lowCutOff;
	}

	/**
	 * @param lowCutOff the lowCutOff to set
	 */
	public void setLowCutOff(int lowCutOff) {
		this.lowCutOff = lowCutOff;
	}

	/**
	 * @return the highCutOff
	 */
	public int getHighCutOff() {
		return highCutOff;
	}

	/**
	 * @param highCutOff the highCutOff to set
	 */
	public void setHighCutOff(int highCutOff) {
		this.highCutOff = highCutOff;
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

	
}
