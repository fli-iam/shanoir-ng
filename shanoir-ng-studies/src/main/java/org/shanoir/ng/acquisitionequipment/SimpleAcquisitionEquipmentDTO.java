package org.shanoir.ng.acquisitionequipment;

/**
 * DTO for acquisition equipments.
 * 
 * @author msimon
 *
 */
public class SimpleAcquisitionEquipmentDTO {

	private String centerName;

	private Long id;

	private String manufacturerModelName;

	private String manufacturerName;

	private String serialNumber;

	/**
	 * Default constructor.
	 */
	public SimpleAcquisitionEquipmentDTO() {
	}

	/**
	 * @return the centerName
	 */
	public String getCenterName() {
		return centerName;
	}

	/**
	 * @param centerName
	 *            the centerName to set
	 */
	public void setCenterName(String centerName) {
		this.centerName = centerName;
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
	 * @return the manufacturerModelName
	 */
	public String getManufacturerModelName() {
		return manufacturerModelName;
	}

	/**
	 * @param manufacturerModelName
	 *            the manufacturerModelName to set
	 */
	public void setManufacturerModelName(String manufacturerModelName) {
		this.manufacturerModelName = manufacturerModelName;
	}

	/**
	 * @return the manufacturerName
	 */
	public String getManufacturerName() {
		return manufacturerName;
	}

	/**
	 * @param manufacturerName
	 *            the manufacturerName to set
	 */
	public void setManufacturerName(String manufacturerName) {
		this.manufacturerName = manufacturerName;
	}

	/**
	 * @return the serialNumber
	 */
	public String getSerialNumber() {
		return serialNumber;
	}

	/**
	 * @param serialNumber
	 *            the serialNumber to set
	 */
	public void setSerialNumber(String serialNumber) {
		this.serialNumber = serialNumber;
	}

}
