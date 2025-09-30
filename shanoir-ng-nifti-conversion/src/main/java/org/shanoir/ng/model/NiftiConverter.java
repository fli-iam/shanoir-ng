package org.shanoir.ng.model;

public enum NiftiConverter {

	DCM2NII_2008_03_31(1, "/opt/nifti-converters/dcm2nii_2008-03-31"),

	MCVERTER_2_0_7(2, "/opt/nifti-converters/mcverter_2.0.7"),

	DCM2NII_2014_08_04(4, "/opt/nifti-converters/dcm2nii_2014-08-04"),

	MCVERTER_2_1_0(5, "/opt/nifti-converters/mcverter_2.1.0"),

	DCM2NIIX(6, "dcm2niix"),

	DICOMIFIER(7, "dicomifier"),

	MRICONVERTER(8, "/opt/nifti-converters/mriconverter/MRIFileManager/MRIManager.jar");

	private final int id;

	private final String path;

	/**
	 * Constructor.
	 *
	 * @param id
	 *            id
	 */
	private NiftiConverter(final int id, final String path) {
		this.id = id;
		this.path = path;
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

	public String getPath() { return path; }
}
