package org.shanoir.uploader.model.rest;

public class IdName {

	private Long id;

	private String name;

	/**
	 * Default constructor.
	 */
	public IdName() {
	}

	/**
	 * Constructor with id and name.
	 * 
	 * @param id
	 *            object id.
	 * @param name
	 *            object name.
	 */
	public IdName(final Long id, final String name) {
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

	@Override
	public String toString() {
		return name;
	}

}