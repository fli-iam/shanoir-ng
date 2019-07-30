package org.shanoir.uploader.model.dto;

import java.util.List;

import org.shanoir.uploader.model.dto.rest.IdNameDTO;

public class SimpleStudyDTO {

	private Long id;

	private String name;

	private List<SimpleStudyCardDTO> studyCards;
	
	private List<IdNameDTO> centers;
	
	private Boolean compatible;

	/**
	 * Simple constructor.
	 */
	public SimpleStudyDTO() {
	}

//	/**
//	 * Constructor.
//	 * 
//	 * @param id
//	 *            study id.
//	 * @param name
//	 *            study name.
//	 */
//	public SimpleStudyDTO(final Long id, final String name) {
//		this.id = id;
//		this.name = name;
//		this.centers = new ArrayList<>();
//		this.studyCards = new ArrayList<>();
//	}

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

	/**
	 * @return the studyCards
	 */
	public List<SimpleStudyCardDTO> getStudyCards() {
		return studyCards;
	}

	/**
	 * @param studyCards
	 *            the studyCards to set
	 */
	public void setStudyCards(List<SimpleStudyCardDTO> studyCards) {
		this.studyCards = studyCards;
	}

	public List<IdNameDTO> getCenters() {
		return centers;
	}

	public void setCenters(List<IdNameDTO> centers) {
		this.centers = centers;
	}

	public Boolean getCompatible() {
		return compatible;
	}

	public void setCompatible(Boolean compatible) {
		this.compatible = compatible;
	}
}
