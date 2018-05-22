package org.shanoir.ng.preclinical.subjects;

import java.util.Objects;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.shanoir.ng.preclinical.references.Reference;
import org.shanoir.ng.shared.hateoas.HalEntity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.swagger.annotations.ApiModelProperty;

/**
 * Animal Subject
 */

@Entity
@Table(name = "animal_subject")
@JsonPropertyOrder({ "_links", "subjectId", "specie", "strain", "biotype", "provider", "stabulation" })
public class AnimalSubject extends HalEntity {

	@NotNull
	private Long subjectId;

	@ManyToOne
	@NotNull
	@JsonProperty("specie")
	// @RefValueExists
	private Reference specie = null;

	@JsonProperty("strain")
	@ManyToOne
	@NotNull
	// @RefValueExists
	private Reference strain = null;

	@JsonProperty("biotype")
	// @RefValueExists
	@ManyToOne
	@NotNull
	private Reference biotype = null;

	@JsonProperty("provider")
	// @RefValueExists
	@ManyToOne
	@NotNull
	private Reference provider = null;

	@JsonProperty("stabulation")
	// @RefValueExists
	@ManyToOne
	@NotNull
	private Reference stabulation = null;

	public AnimalSubject specie(Reference specie) {
		this.specie = specie;
		return this;
	}

	public Long getSubjectId() {
		return subjectId;
	}

	public void setSubjectId(Long subjectId) {
		this.subjectId = subjectId;
	}

	/**
	 * none
	 * 
	 * @return specie
	 **/
	@ApiModelProperty(value = "none")
	public Reference getSpecie() {
		return specie;
	}

	public void setSpecie(Reference specie) {
		this.specie = specie;
	}

	public AnimalSubject strain(Reference strain) {
		this.strain = strain;
		return this;
	}

	/**
	 * none
	 * 
	 * @return strain
	 **/
	@ApiModelProperty(value = "none")
	public Reference getStrain() {
		return strain;
	}

	public void setStrain(Reference strain) {
		this.strain = strain;
	}

	public AnimalSubject biotype(Reference biotype) {
		this.biotype = biotype;
		return this;
	}

	/**
	 * none
	 * 
	 * @return biotype
	 **/
	@ApiModelProperty(value = "none")
	public Reference getBiotype() {
		return biotype;
	}

	public void setBiotype(Reference biotype) {
		this.biotype = biotype;
	}

	public AnimalSubject provider(Reference provider) {
		this.provider = provider;
		return this;
	}

	/**
	 * none
	 * 
	 * @return provider
	 **/
	@ApiModelProperty(value = "none")
	public Reference getProvider() {
		return provider;
	}

	public void setProvider(Reference provider) {
		this.provider = provider;
	}

	public AnimalSubject stabulation(Reference stabulation) {
		this.stabulation = stabulation;
		return this;
	}

	/**
	 * none
	 * 
	 * @return stabulation
	 **/
	@ApiModelProperty(value = "none")
	public Reference getStabulation() {
		return stabulation;
	}

	public void setStabulation(Reference stabulation) {
		this.stabulation = stabulation;
	}

	@Override
	public boolean equals(java.lang.Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		AnimalSubject subject = (AnimalSubject) o;
		return Objects.equals(this.subjectId, subject.subjectId) && Objects.equals(this.specie, subject.specie)
				&& Objects.equals(this.strain, subject.strain) && Objects.equals(this.biotype, subject.biotype)
				&& Objects.equals(this.provider, subject.provider)
				&& Objects.equals(this.stabulation, subject.stabulation);
	}

	@Override
	public int hashCode() {
		return Objects.hash(subjectId, specie, strain, biotype, provider, stabulation);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class AnimalSubject {\n");

		sb.append("    subjectId: ").append(toIndentedString(subjectId)).append("\n");
		sb.append("    specie: ").append(toIndentedString(specie)).append("\n");
		sb.append("    strain: ").append(toIndentedString(strain)).append("\n");
		sb.append("    biotype: ").append(toIndentedString(biotype)).append("\n");
		sb.append("    provider: ").append(toIndentedString(provider)).append("\n");
		sb.append("    stabulation: ").append(toIndentedString(stabulation)).append("\n");
		sb.append("}");
		return sb.toString();
	}

	/**
	 * Convert the given object to string with each line indented by 4 spaces
	 * (except the first line).
	 */
	private String toIndentedString(java.lang.Object o) {
		if (o == null) {
			return "null";
		}
		return o.toString().replace("\n", "\n    ");
	}
}
