package org.shanoir.ng.shared.core.model;

import java.util.ArrayList;
import java.util.List;


public class IdList {

	private List<Long> idList;

	/**
	 * Default constructor.
	 */
	public IdList() {
		this.idList = new ArrayList<>();
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
