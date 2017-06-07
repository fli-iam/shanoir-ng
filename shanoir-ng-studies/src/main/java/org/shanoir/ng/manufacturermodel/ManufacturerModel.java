package org.shanoir.ng.manufacturermodel;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;
import org.shanoir.ng.shared.hateoas.HalEntity;
import org.shanoir.ng.shared.validation.ManufactureModelTypeCheck;
import org.shanoir.ng.shared.validation.Unique;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Manufacturer model.
 * 
 * @author msimon
 *
 */
@Entity
@JsonPropertyOrder({ "_links", "id", "name" })
@GenericGenerator(name = "IdOrGenerate", strategy = "increment")
@ManufactureModelTypeCheck
public class ManufacturerModel extends HalEntity {

	/**
	 * UID
	 */
	private static final long serialVersionUID = -99873038897196966L;

	@NotNull
	@Enumerated(EnumType.STRING)
	private DatasetModalityType datasetModalityType;

	private Double magneticField;

	@ManyToOne
	@NotNull
	private Manufacturer manufacturer;

	@NotNull
	@Length(min = 2, max = 200)
	private String name;

	/**
	 * @return the datasetModalityType
	 */
	public DatasetModalityType getDatasetModalityType() {
		return datasetModalityType;
	}

	/**
	 * @param datasetModalityType
	 *            the datasetModalityType to set
	 */
	public void setDatasetModalityType(DatasetModalityType datasetModalityType) {
		this.datasetModalityType = datasetModalityType;
	}

	/**
	 * @return the magneticField
	 */
	public Double getMagneticField() {
		return magneticField;
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
