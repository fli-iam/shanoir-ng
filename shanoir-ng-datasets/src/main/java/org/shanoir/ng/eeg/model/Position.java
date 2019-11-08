package org.shanoir.ng.eeg.model;

import javax.persistence.Entity;

import org.shanoir.ng.shared.hateoas.HalEntity;

/**
 * Position in space of the electrode/channel for an EEG acquisition.
 * @author JComeD
 *
 */
@Entity
public class Position extends HalEntity {
	
	/** Serial version UID. */
	private static final long serialVersionUID = 1L;

	/** X position in space. */
	private int X;
	
	/** Y position in space. */
	private int Y;
	
	/** Z position in space. */
	private int Z;

	/**
	 * @return the x
	 */
	public int getX() {
		return X;
	}
	/**
	 * @param x the x to set
	 */
	public void setX(int x) {
		X = x;
	}
	/**
	 * @return the y
	 */
	public int getY() {
		return Y;
	}
	/**
	 * @param y the y to set
	 */
	public void setY(int y) {
		Y = y;
	}
	/**
	 * @return the z
	 */
	public int getZ() {
		return Z;
	}
	/**
	 * @param z the z to set
	 */
	public void setZ(int z) {
		Z = z;
	}

	

}
