package org.shanoir.ng.study.dto;

import java.util.ArrayList;
import java.util.List;

import org.shanoir.ng.shared.core.model.IdName;
import org.shanoir.ng.studycenter.StudyCenterDTO;

/**
 * Simple DTO for studies.
 * 
 * @author msimon
 *
 */
public class IdNameCenterStudyDTO extends IdName {
	
	private List<StudyCenterDTO> studyCenterList;

	/**
	 * Simple constructor.
	 */
	public IdNameCenterStudyDTO() {
	}

	/**
	 * Constructor.
	 */
	public IdNameCenterStudyDTO(final Long id, final String name) {
		super(id, name);
		this.setStudyCenterList(new ArrayList<>());
	}

	/**
	 * @return the studyCenterList
	 */
	public List<StudyCenterDTO> getStudyCenterList() {
		return studyCenterList;
	}

	/**
	 * @param studyCenterList the studyCenterList to set
	 */
	public void setStudyCenterList(List<StudyCenterDTO> studyCenterList) {
		this.studyCenterList = studyCenterList;
	}

}
