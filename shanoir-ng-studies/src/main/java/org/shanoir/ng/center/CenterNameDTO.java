package org.shanoir.ng.center;

/**
 * DTO for centers with id and name.
 * 
 * @author msimon
 *
 */
public class CenterNameDTO {

	private Long id;

	private String name;

	/**
	 * Default constructor.
	 */
	public CenterNameDTO() {
	}

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            center id.
	 * @param name
	 *            center name.
	 */
	public CenterNameDTO(final Long id, final String name) {
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
