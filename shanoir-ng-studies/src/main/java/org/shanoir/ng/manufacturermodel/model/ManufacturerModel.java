package org.shanoir.ng.manufacturermodel.model;

import javax.persistence.ColumnResult;
import javax.persistence.ConstructorResult;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.PostLoad;
import javax.persistence.SqlResultSetMapping;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.validator.constraints.Length;
import org.shanoir.ng.shared.core.model.IdName;
import org.shanoir.ng.shared.hateoas.HalEntity;
import org.shanoir.ng.shared.hateoas.Links;

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
@SqlResultSetMapping(name="ManufacturerModelNameResult", classes = {
	    @ConstructorResult(targetClass = IdName.class, 
	    columns = {@ColumnResult(name="id", type = Long.class), @ColumnResult(name="name")})
	})
@ManufacturerModelTypeCheck
public class ManufacturerModel extends HalEntity {

	/**
	 * UID
	 */
	private static final long serialVersionUID = -99873038897196966L;

	@NotNull
	private Integer datasetModalityType;

	private Double magneticField;

	@ManyToOne
	@NotNull
	private Manufacturer manufacturer;

	@NotNull
	@Length(min = 2, max = 200)
	private String name;

	/**
	 * Init HATEOAS links
	 */
	@PostLoad
	public void initLinks() {
		this.addLink(Links.REL_SELF, "manufacturermodel/" + getId());
	}

	/**
	 * @return the datasetModalityType
	 */
	public DatasetModalityType getDatasetModalityType() {
		return DatasetModalityType.getType(datasetModalityType);
	}

	/**
	 * @param datasetModalityType
	 *            the datasetModalityType to set
	 */
	public void setDatasetModalityType(DatasetModalityType datasetModalityType) {
		if (datasetModalityType != null) {
			this.datasetModalityType = datasetModalityType.getId();
		}
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
