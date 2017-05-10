package org.shanoir.ng.shared.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO with list of ids.
 * 
 * @author msimon
 *
 */
public class IdListDto {

	private List<Long> idList;

	/**
	 * Default constructor.
	 */
	public IdListDto() {
		this.idList = new ArrayList<>();
	}

	/**
	 * @return the idList
	 */
	public List<Long> getIdList() {
		return idList;
	}

	/**
	 * @param idList
	 *            the idList to set
	 */
	public void setIdList(List<Long> idList) {
		this.idList = idList;
	}

}
