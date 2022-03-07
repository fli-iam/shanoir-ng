package org.shanoir.uploader.model.rest;

public class ManufacturerModel {

	private Double magneticField;

	private Manufacturer manufacturer;

	private String name;

	/**
	 * @return the magneticField
	 */
	public Double getMagneticField() {
		return magneticField;
	}

	@Override
	public String toString() {
		return manufacturer.toString() + " " + name + "(" + magneticField + ")";
	}

	/**
	 * @param magneticField
	 *            the magneticField to set
	 */
	public void setMagneticField(Double magneticField) {
		this.magneticField = magneticField;
	}

	/**
	 * @return the manufacturer
	 */
	public Manufacturer getManufacturer() {
		return manufacturer;
	}

	/**
	 * @param manufacturer
	 *            the manufacturer to set
	 */
	public void setManufacturer(Manufacturer manufacturer) {
		this.manufacturer = manufacturer;
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