package org.shanoir.ng.examination;

/**
 * Units of measure.
 * 
 * @author ifakhfakh
 *
 */
public enum UnitOfMeasure {

	MS(1),

	PERCENT(2),

	DEGREES(3),

	G(4),

	GY(5),

	HZ_PX(6),

	KG(7),

	M(8),

	MG(9),

	MG_ML(10),

	MHZ(11),

	ML(12),

	MM(13),

	PX(14);

	private int id;

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            id
	 */
	private UnitOfMeasure(final int id) {
		this.id = id;
	}

	/**
	 * Get a unit Of measure by its id.
	 * 
	 * @param id
	 *            unit Of measure id.
	 * @return unit Of measure.
	 */
	public static UnitOfMeasure getUnit(final Integer id) {
		if (id == null) {
			return null;
		}
		for (UnitOfMeasure measure : UnitOfMeasure.values()) {
			if (id.equals(measure.getId())) {
				return measure;
			}
		}
		throw new IllegalArgumentException("No matching unit Of measure for id " + id);
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

}
