package org.shanoir.ng.coil;

import org.shanoir.ng.manufacturermodel.ManufacturerModelDTO;
import org.shanoir.ng.shared.dto.IdNameDTO;

/**
 * DTO for coil.
 * 
 * @author msimon
 */
public class CoilDTO {

	private IdNameDTO center;

	private CoilType coilType;
	
	private Long id;

	private ManufacturerModelDTO manufacturerModel;

	private String name;

	private Long numberOfChannels;

	private String serialNumber;

	/**
	 * Default constructor.
	 */
	public CoilDTO() {
	}
	
	/**
	 * @return the center
	 */
	public IdNameDTO getCenter() {
		return center;
	}

	/**
	 * @param center the center to set
	 */
	public void setCenter(IdNameDTO center) {
		this.center = center;
	}

	/**
	 * @return the coilType
	 */
	public CoilType getCoilType() {
		return coilType;
	}

	/**
	 * @param coilType the coilType to set
	 */
	public void setCoilType(CoilType coilType) {
		this.coilType = coilType;
	}

	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * @return the manufacturerModel
	 */
	public ManufacturerModelDTO getManufacturerModel() {
		return manufacturerModel;
	}

	/**
	 * @param manufacturerModel the manufacturerModel to set
	 */
	public void setManufacturerModel(ManufacturerModelDTO manufacturerModel) {
		this.manufacturerModel = manufacturerModel;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the numberOfChannels
	 */
	public Long getNumberOfChannels() {
		return numberOfChannels;
	}

	/**
	 * @param numberOfChannels the numberOfChannels to set
	 */
	public void setNumberOfChannels(Long numberOfChannels) {
		this.numberOfChannels = numberOfChannels;
	}

	/**
	 * @return the serialNumber
	 */
	public String getSerialNumber() {
		return serialNumber;
	}

	/**
	 * @param serialNumber the serialNumber to set
	 */
	public void setSerialNumber(String serialNumber) {
		this.serialNumber = serialNumber;
	}

}
