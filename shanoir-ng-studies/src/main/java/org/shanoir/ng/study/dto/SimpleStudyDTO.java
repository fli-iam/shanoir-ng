package org.shanoir.ng.study.dto;

import java.util.ArrayList;
import java.util.List;

import org.shanoir.ng.center.CenterDTO;

/**
 * Simple DTO for studies.
 * 
 * @author msimon
 *
 */
public class SimpleStudyDTO {

	private Long id;

	private String name;
	
	private List<CenterDTO> centers;
	
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
		this.centers = new ArrayList<>();
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

	public List<CenterDTO> getCenters() {
		return centers;
	}

	public void setCenters(List<CenterDTO> centers) {
		this.centers = centers;
	}

	public Boolean getCompatible() {
		return compatible;
	}

	public void setCompatible(Boolean compatible) {
		this.compatible = compatible;
	}

}
