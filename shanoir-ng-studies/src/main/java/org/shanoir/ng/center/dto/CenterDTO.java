/**
 * Shanoir NG - Import, manage and share neuroimaging data
 * Copyright (C) 2009-2019 Inria - https://www.inria.fr/
 * Contact us on https://project.inria.fr/shanoir/
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/gpl-3.0.html
 */

package org.shanoir.ng.center.dto;

import java.util.List;

import org.shanoir.ng.acquisitionequipment.dto.AcquisitionEquipmentDTO;
import org.shanoir.ng.studycenter.StudyCenterDTO;

/**
 * DTO for centers.
 * 
 * @author msimon
 *
 */
public class CenterDTO {

	private List<AcquisitionEquipmentDTO> acquisitionEquipments;

	private String city;

	private String country;

	private Long id;

	private String name;

	private String phoneNumber;

	private String postalCode;

	private String street;

	private String website;
	
	private Boolean compatible = false;
	
	private List<StudyCenterDTO> studyCenterList;

	/**
	 * Default constructor.
	 */
	public CenterDTO() {
	}

	/**
	 * @return the acquisitionEquipments
	 */
	public List<AcquisitionEquipmentDTO> getAcquisitionEquipments() {
		return acquisitionEquipments;
	}

	/**
	 * @param acquisitionEquipments
	 *            the acquisitionEquipments to set
	 */
	public void setAcquisitionEquipments(List<AcquisitionEquipmentDTO> acquisitionEquipments) {
		this.acquisitionEquipments = acquisitionEquipments;
	}

	/**
	 * @return the city
	 */
	public String getCity() {
		return city;
	}

	/**
	 * @param city
	 *            the city to set
	 */
	public void setCity(String city) {
		this.city = city;
	}

	/**
	 * @return the country
	 */
	public String getCountry() {
		return country;
	}

	/**
	 * @param country
	 *            the country to set
	 */
	public void setCountry(String country) {
		this.country = country;
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

	/**
	 * @return the phoneNumber
	 */
	public String getPhoneNumber() {
		return phoneNumber;
	}

	/**
	 * @param phoneNumber
	 *            the phoneNumber to set
	 */
	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	/**
	 * @return the postalCode
	 */
	public String getPostalCode() {
		return postalCode;
	}

	/**
	 * @param postalCode
	 *            the postalCode to set
	 */
	public void setPostalCode(String postalCode) {
		this.postalCode = postalCode;
	}

	/**
	 * @return the street
	 */
	public String getStreet() {
		return street;
	}

	/**
	 * @param street
	 *            the street to set
	 */
	public void setStreet(String street) {
		this.street = street;
	}

	/**
	 * @return the website
	 */
	public String getWebsite() {
		return website;
	}

	/**
	 * @param website
	 *            the website to set
	 */
	public void setWebsite(String website) {
		this.website = website;
	}

	public Boolean getCompatible() {
		return compatible;
	}

	public void setCompatible(Boolean compatible) {
		this.compatible = compatible;
	}

	public List<StudyCenterDTO> getStudyCenterList() {
		return studyCenterList;
	}

	public void setStudyCenterList(List<StudyCenterDTO> studyCenterList) {
		this.studyCenterList = studyCenterList;
	}

}
