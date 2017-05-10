package org.shanoir.ng.study.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple DTO for studies.
 * 
 * @author msimon
 *
 */
public class SimpleStudyDTO {

	private Long id;

	private String name;

	private List<SimpleStudyCardDTO> studyCards;

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
		this.studyCards = new ArrayList<>();
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

}
