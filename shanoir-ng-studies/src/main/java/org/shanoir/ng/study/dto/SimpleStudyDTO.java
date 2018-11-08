package org.shanoir.ng.study.dto;

import java.util.ArrayList;
import java.util.List;

import org.shanoir.ng.studycenter.StudyCenterDTO;

/**
 * Simple DTO for studies.
 * 
 * @author msimon
 *
 */
public class SimpleStudyDTO {

	private Long id;

	private String name;
	
	private List<StudyCenterDTO> studyCenterList;
	
	private Boolean compatible = false;

	/**
	 * Simple constructor.
	 */
	public SimpleStudyDTO() {
	}

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            study id.
	 * @param name
	 *            study name.
	 */
	public SimpleStudyDTO(final Long id, final String name) {
		this.id = id;
		this.name = name;
		this.setStudyCenterList(new ArrayList<>());
	}

	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	public Boolean getCompatible() {
		return compatible;
	}

	public void setCompatible(Boolean compatible) {
		this.compatible = compatible;
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
