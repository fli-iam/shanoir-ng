package org.shanoir.ng.acquisitionequipment;

import java.util.ArrayList;
import java.util.List;

import org.shanoir.ng.manufacturermodel.ManufacturerModel;
import org.shanoir.ng.shared.dto.IdNameDTO;

/**
 * DTO for acquisition equipments.
 * 
 * @author msimon
 *
 */
public class AcquisitionEquipmentDTO {

	private IdNameDTO center;

	private Long id;

	private ManufacturerModel manufacturerModel;

	private String serialNumber;

	private List<IdNameDTO> studyCards;

	/**
	 * Default constructor.
	 */
	public AcquisitionEquipmentDTO() {
		studyCards = new ArrayList<>();
	}

	/**
	 * @return the center
	 */
	public IdNameDTO getCenter() {
		return center;
	}

	/**
	 * @param center
	 *            the center to set
	 */
	public void setCenter(IdNameDTO center) {
		this.center = center;
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
	 * @return the manufacturerModel
	 */
	public ManufacturerModel getManufacturerModel() {
		return manufacturerModel;
	}

	/**
	 * @param manufacturerModel
	 *            the manufacturerModel to set
	 */
	public void setManufacturerModel(ManufacturerModel manufacturerModel) {
		this.manufacturerModel = manufacturerModel;
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

	/**
	 * @return the studyCards
	 */
	public List<IdNameDTO> getStudyCards() {
		return studyCards;
	}

	/**
	 * @param studyCards the studyCards to set
	 */
	public void setStudyCards(List<IdNameDTO> studyCards) {
		this.studyCards = studyCards;
	}

}
