package org.shanoir.ng.center;

/**
 * DTO for centers.
 * 
 * @author msimon
 *
 */
public class SimpleCenterDTO {

	private Long id;

	private String name;

	/**
	 * Default constructor.
	 */
	public SimpleCenterDTO() {
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
