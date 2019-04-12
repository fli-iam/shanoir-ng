package org.shanoir.ng.datasetacquisition.model.mr;

/**
 * Scanning Sequence.
 * 
 * @author atouboul
 *
 */
public enum MrScanningSequence {

	// Spin Echo
	SE(1),

	// Inversion Recovery
	IR(2),

	// Gradient Recalled
	GR(3),

	// Echo Planar
	EP(4),
	
	//Research Mode
	RM(5);
	
	private int id;

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            id
	 */
	private MrScanningSequence(final int id) {
		this.id = id;
	}

	/**
	 * Get a Scanning Sequence by its id.
	 * 
	 * @param id
	 *            sequence id.
	 * @return Scanning Sequence.
	 */
	public static MrScanningSequence getScanningSequence(final Integer id) {
		if (id == null) {
			return null;
		}
		for (MrScanningSequence scanningSequence : MrScanningSequence.values()) {
			if (id.equals(scanningSequence.getId())) {
				return scanningSequence;
			}
		}
		throw new IllegalArgumentException("No matching scanning sequence for id " + id);
	}
	
	/**
	 * Get a Scanning Sequence by its name.
	 * 
	 * @param type
	 *            sequence id.
	 * @return Scanning Sequence.
	 */
	public static MrScanningSequence getIdByType(final String type) {
		if (type == null) {
			return null;
		}
		return MrScanningSequence.valueOf(type);
	}
	
	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

}
