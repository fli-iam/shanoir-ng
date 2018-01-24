package org.shanoir.ng.study.dto;

import org.shanoir.ng.shared.dto.IdNameDTO;

/**
 * Simple DTO for study cards.
 * 
 * @author msimon
 *
 */
public class SimpleStudyCardDTO {

	private Long id;

	private String name;
	
	private Boolean compatible;
	
	private IdNameDTO center;

	/**
	 * Simple constructor.
	 */
	public SimpleStudyCardDTO() {
	}

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            study card id.
	 * @param name
	 *            study card name.
	 */
	public SimpleStudyCardDTO(final Long id, final String name) {
		this.id = id;
		this.name = name;
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

	public IdNameDTO getCenter() {
		return center;
	}

	public void setCenter(IdNameDTO center) {
		this.center = center;
	}

}
