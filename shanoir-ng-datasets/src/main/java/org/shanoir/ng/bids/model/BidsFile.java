package org.shanoir.ng.bids.model;

/**
 * Leaf class of composite object for BIDS elements.
 * These classes exists to serialize/deserialize a BIDS folder to be visualized in front
 * And to be able to modify a bids file from the front
 * This class represents a File with content
 * @author JComeD
 *
 */
public class BidsFile extends BidsElement {

	/** The content of the bids file. */
	String content;

	public BidsFile(String absolutePath) {
		super(absolutePath);
	}

	/**
	 * @return the content
	 */
	public String getContent() {
		return content;
	}

	/**
	 * @param content the content to set
	 */
	public void setContent(String content) {
		this.content = content;
	}

	/**
	 * This methods checks from the file name if it is editable or not.
	 * @return <code>true</code> if the file can be edited, <code>false</code> otherwise
	 */
	public boolean isEditable() {
		return false;
	}

	/**
	 * This methods checks from the file name if it is deletable or not.
	 * @return <code>true</code> if the file can be deleted, <code>false</code> otherwise
	 */
	public boolean isDeletable() {
		return false;
	}

	@Override
	public boolean isFile() {
		return true;
	}
}
