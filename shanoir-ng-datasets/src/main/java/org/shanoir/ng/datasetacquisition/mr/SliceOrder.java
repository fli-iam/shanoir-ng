package org.shanoir.ng.datasetacquisition.mr;

/**
 * Slice order.
 * 
 * @author msimon
 *
 */
public enum SliceOrder {

	// Head First-Prone
	HEAD_FIRST_PRONE(1),

	// Head First-Decubitus Right
	HEAD_FIRST_DECUBITUS_RIGHT(2),

	// Feet First-Decubitus Right
	FEET_FIRST_DECUBITUS_RIGHT(3),

	// Feet First-Prone
	FEET_FIRST_PRONE(4),

	// Head First-Supine
	HEAD_FIRST_SUPINE(5),

	// Head First-Decubitus Left
	HEAD_FIRST_DECUBITUS_LEFT(6),

	// Feet First-Decubitus Left
	FEET_FIRST_DECUBITUS_LEFT(7);

	private int id;

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            id
	 */
	private SliceOrder(final int id) {
		this.id = id;
	}

	/**
	 * Get a slice order by its id.
	 * 
	 * @param id
	 *            slice order id.
	 * @return slice order.
	 */
	public static SliceOrder getOrder(final Integer id) {
		if (id == null) {
			return null;
		}
		for (SliceOrder order : SliceOrder.values()) {
			if (id.equals(order.getId())) {
				return order;
			}
		}
		throw new IllegalArgumentException("No matching slice order for id " + id);
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

}
