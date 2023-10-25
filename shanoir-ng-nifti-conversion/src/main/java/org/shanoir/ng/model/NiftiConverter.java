package org.shanoir.ng.model;

public enum NiftiConverter {

	DCM2NII(1),

	MCVERTER(2),

	CLIDCM(3),
	
	DICOM2NIFTI(4),

	DICOMIFIER(5),

	MRICONVERTER(6);
	
	private int id;

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            id
	 */
	private NiftiConverter(final int id) {
		this.id = id;
	}

	/**
	 * Get a type by its id.
	 * 
	 * @param id
	 *            format id.
	 * @return type of converter
	 */
	public static NiftiConverter getType(final Integer id) {
		if (id == null) {
			return null;
		}
		for (NiftiConverter type : NiftiConverter.values()) {
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

	public boolean isMcverter() {
		return MCVERTER.equals(this);
	}

	public boolean isClidcm() {
		return CLIDCM.equals(this);
	}

	public boolean isDicom2Nifti() {
		return DICOM2NIFTI.equals(this);
	}

	public boolean isDicomifier() {
		return DICOMIFIER.equals(this);
	}

}
