package org.shanoir.ng.study;

/**
 * DTO for studies with id and name.
 * 
 * @author ifakhfakh
 *
 */
public class StudyNameDTO {

	private Long id;

	private String name;

	/**
	 * Default constructor.
	 */
	public StudyNameDTO() {
	}

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            study id.
	 * @param name
	 *            study name.
	 */
	public StudyNameDTO(final Long id, final String name) {
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

}
