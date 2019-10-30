package org.shanoir.uploader.model.rest;

import java.util.ArrayList;
import java.util.List;


public class IdList {

	private List<Long> idList;

	/**
	 * Default constructor.
	 */
	public IdList() {
		this.idList = new ArrayList<Long>();
	}

	/**
	 * @return the idList
	 */
	public List<Long> getIdList() {
		return idList;
	}

	/**
	 * @param idList the idList to set
	 */
	public void setIdList(List<Long> idList) {
		this.idList = idList;
	}

}