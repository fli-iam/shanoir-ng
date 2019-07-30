package org.shanoir.uploader.model.dto.rest;

public enum Sex {
	/**
	 * Male.
	 */
	M(1),

	/**
	 * Female.
	 */
	F(2);

	private int id;

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            id
	 */
	private Sex(final int id) {
		this.id = id;
	}

	/**
	 * Get a sex by its id.
	 * 
	 * @param id
	 *            sex id.
	 * @return sex.
	 */
	public static Sex getSex(final Integer id) {
		if (id == null) {
			return null;
		}
		for (Sex sex : Sex.values()) {
			if (id.equals(sex.getId())) {
				return sex;
			}
		}
		throw new IllegalArgumentException("No matching sex for id " + id);
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}
}
