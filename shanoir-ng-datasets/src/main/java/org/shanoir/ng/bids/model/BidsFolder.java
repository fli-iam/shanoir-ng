package org.shanoir.ng.bids.model;

import java.util.List;

/**
 * Component class of composite object for BIDS elements.
 * These classes exists to serialize/deserialize a BIDS folder to be visualized in front
 * And to be able to modify a bids file from the front
 * This class represents a folder with potentially a sub-list of files
 * @author JComeD
 *
 */
public class BidsFolder extends BidsElement {

	public BidsFolder(String path) {
		super(path);
	}

	List<BidsElement> elements;

	/**
	 * @return the elements
	 */
	public List<BidsElement> getElements() {
		return elements;
	}

	/**
	 * @param elements the elements to set
	 */
	public void setElements(List<BidsElement> elements) {
		this.elements = elements;
	}

	@Override
	public boolean isFile() {
		return false;
	}

}
