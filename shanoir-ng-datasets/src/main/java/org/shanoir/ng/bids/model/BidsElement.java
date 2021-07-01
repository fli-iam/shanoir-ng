package org.shanoir.ng.bids.model;

/**
 * Abstract class of composit object for BIDS elements.
 * These classes exists to serialize/deserialize a BIDS folder to be visualized in front
 * And to be able to modify a bids file from the front
 * @author JComeD
 *
 */
public abstract class BidsElement {

	/** Path of the Bids file. */
	String path;

	public BidsElement(String path) {
		this.path = path;
	}

	/**
	 * @return the path
	 */
	public String getPath() {
		return path;
	}

	/**
	 * @param path the path to set
	 */
	public void setPath(String path) {
		this.path = path;
	}

	public abstract boolean isFile();
}
