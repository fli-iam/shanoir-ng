package org.shanoir.ng.center;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ColumnResult;
import javax.persistence.ConstructorResult;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.PostLoad;
import javax.persistence.SqlResultSetMapping;
import javax.validation.constraints.Pattern;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.validator.constraints.NotBlank;
import org.shanoir.ng.acquisitionequipment.AcquisitionEquipment;
import org.shanoir.ng.shared.dto.IdNameDTO;
import org.shanoir.ng.shared.hateoas.HalEntity;
import org.shanoir.ng.shared.hateoas.Links;
import org.shanoir.ng.shared.validation.EditableOnlyBy;
import org.shanoir.ng.shared.validation.Unique;
import org.shanoir.ng.studycenter.StudyCenter;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Center
 * 
 * @author yyao
 *
 */
@Entity
@JsonPropertyOrder({ "_links", "id", "name" })
@GenericGenerator(name = "IdOrGenerate", strategy = "increment")
@SqlResultSetMapping(name="centerNameResult", classes = {
	    @ConstructorResult(targetClass = IdNameDTO.class, 
	    columns = {@ColumnResult(name="id", type = Long.class), @ColumnResult(name="name")})
	})
public class Center extends HalEntity {

	/**
	 * UID
	 */
	private static final long serialVersionUID = -1965594174611746591L;

	/** List of the acquisition equipments related to this center. */
	@OneToMany(mappedBy = "center", fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private List<AcquisitionEquipment> acquisitionEquipments;

	private String city;

	private String country;

	@NotBlank
	@Column(unique = true)
	@Unique
	@EditableOnlyBy(roles = { "ROLE_ADMIN", "ROLE_EXPERT" })
	private String name;

	@Pattern(regexp = "[\\+]?[\\d]*")
	private String phoneNumber;

	private String postalCode;

	private String street;

	/** Relations between the investigators, the centers and the studies. */
	@OneToMany(mappedBy = "center", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	private List<StudyCenter> studyCenterList;

	private String website;

	/**
	 * Init HATEOAS links
	 */
	@PostLoad
	public void initLinks() {
		this.addLink(Links.REL_SELF, "center/" + getId());
	}

	/**
	 * @return the acquisitionEquipments
	 */
	public List<AcquisitionEquipment> getAcquisitionEquipments() {
		return acquisitionEquipments;
	}

	/**
	 * @param acquisitionEquipments
	 *            the acquisitionEquipments to set
	 */
	public void setAcquisitionEquipments(List<AcquisitionEquipment> acquisitionEquipments) {
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
	 * @return the studyCenterList
	 */
	public List<StudyCenter> getStudyCenterList() {
		return studyCenterList;
	}

	/**
	 * @param studyCenterList
	 *            the studyCenterList to set
	 */
	public void setStudyCenterList(List<StudyCenter> studyCenterList) {
		this.studyCenterList = studyCenterList;
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

}
