package org.shanoir.uploader.model.dto;

public class StudyCardDTO {

	private Long id;

	private String name;

	private Long centerId;

	private String centerName;

	private String acqEquipmentManufacturer;

	private String acqEquipmentManufacturerModel;

	private String acqEquipmentSerialNumber;

	public StudyCardDTO(Long id, String name, Long centerId, String centerName, String acqEquipmentManufacturer,
			String acqEquipmentManufacturerModel, String acqEquipmentSerialNumber) {
		super();
		this.id = id;
		this.name = name;
		this.centerId = centerId;
		this.centerName = centerName;
		this.acqEquipmentManufacturer = acqEquipmentManufacturer;
		this.acqEquipmentManufacturerModel = acqEquipmentManufacturerModel;
		this.acqEquipmentSerialNumber = acqEquipmentSerialNumber;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Long getCenterId() {
		return centerId;
	}

	public void setCenterId(Long centerId) {
		this.centerId = centerId;
	}

	public String getCenterName() {
		return centerName;
	}

	public void setCenterName(String centerName) {
		this.centerName = centerName;
	}

	public String getAcqEquipmentManufacturer() {
		return acqEquipmentManufacturer;
	}

	public void setAcqEquipmentManufacturer(String acqEquipmentManufacturer) {
		this.acqEquipmentManufacturer = acqEquipmentManufacturer;
	}

	public String getAcqEquipmentManufacturerModel() {
		return acqEquipmentManufacturerModel;
	}

	public void setAcqEquipmentManufacturerModel(String acqEquipmentManufacturerModel) {
		this.acqEquipmentManufacturerModel = acqEquipmentManufacturerModel;
	}

	public String getAcqEquipmentSerialNumber() {
		return acqEquipmentSerialNumber;
	}

	public void setAcqEquipmentSerialNumber(String acqEquipmentSerialNumber) {
		this.acqEquipmentSerialNumber = acqEquipmentSerialNumber;
	}

}
