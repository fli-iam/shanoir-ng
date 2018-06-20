package org.shanoir.ng.datasetacquisition.mr;

/**
 * Sequence Variant.
 * 
 * @author atouboul
 *
 */

public enum MrSequenceVariant {

	// segmented k-space
	SK(1),

	// magnetization transfer contrast
	MTC(2),

	// steady state
	SS(3),

	// time reversed steady state
	TRSS(4),
	
	// spoiled
	SP(5),
	
	// MAG prepared;
	MP(6),
	
	// oversampling phase 
	OSP(7),
	
	// no sequence variant
	NONE(8);
	
	private int id;

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            id
	 */
	private MrSequenceVariant(final int id) {
		this.id = id;
	}

	/**
	 * Get a Sequence Variant by its id.
	 * 
	 * @param id Sequence Variant id.
	 * @return Sequence Variant.
	 */
	public static MrSequenceVariant getSequenceVariant(final Integer id) {
		if (id == null) {
			return null;
		}
		for (MrSequenceVariant sequenceVariant : MrSequenceVariant.values()) {
			if (id.equals(sequenceVariant.getId())) {
				return sequenceVariant;
			}
		}
		throw new IllegalArgumentException("No matching scanning sequence for id " + id);
	}
	
	/**
	 * Get an Sequence Variant by its name.
	 * 
	 * @param type
	 *            Sequence Variant
	 * @return Sequence Variant.
	 */
	public static MrSequenceVariant getIdByType(final String type) {
		if (type == null) {
			return null;
		}
		return MrSequenceVariant.valueOf(type);
	}
	
	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

}
