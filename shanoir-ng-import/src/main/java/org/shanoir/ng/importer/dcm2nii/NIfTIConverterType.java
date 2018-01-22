package org.shanoir.ng.importer.dcm2nii;

/**
 * NIfTIConverterType
 * 
 * @author mkain
 *
 */
public enum NIfTIConverterType {

	DCM2NII(1),

	MCVERTER(2),

	CLIDCM(3),
	
	DICOM2NIFTI(4);
	
	private int id;

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            id
	 */
	private NIfTIConverterType(final int id) {
		this.id = id;
	}

	/**
	 * Get a type by its id.
	 * 
	 * @param id
	 *            format id.
	 * @return type of converter
	 */
	public static NIfTIConverterType getType(final Integer id) {
		if (id == null) {
			return null;
		}
		for (NIfTIConverterType type : NIfTIConverterType.values()) {
			if (id.equals(type.getId())) {
				return type;
			}
		}
		throw new IllegalArgumentException("No matching type for id " + id);
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

}