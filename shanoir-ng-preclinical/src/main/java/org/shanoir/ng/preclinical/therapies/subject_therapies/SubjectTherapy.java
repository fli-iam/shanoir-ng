package org.shanoir.ng.preclinical.therapies.subject_therapies;

import java.util.Date;
import java.util.Objects;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.ManyToOne;
import javax.persistence.PostLoad;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.GenericGenerator;
import org.shanoir.ng.preclinical.references.Reference;
import org.shanoir.ng.preclinical.subjects.AnimalSubject;
import org.shanoir.ng.preclinical.therapies.Therapy;
import org.shanoir.ng.shared.hateoas.HalEntity;
import org.shanoir.ng.shared.hateoas.Links;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.swagger.annotations.ApiModelProperty;

/**
 * Subject Therapies
 */
@Entity
@Table(name = "subject_therapy")
@JsonPropertyOrder({ "_links", "therapy", "startDate", "endDate", "dose", "frequency", "animalSubject" })
public class SubjectTherapy extends HalEntity {

	@JsonProperty("animalSubject")
	@ManyToOne
	@NotNull
	@JsonIgnore
	@JsonManagedReference
	private AnimalSubject animalSubject = null;

	@JsonProperty("therapy")
	@ManyToOne
	@NotNull
	private Therapy therapy = null;

	@JsonProperty("startDate")
	private Date startDate = null;

	@JsonProperty("endDate")
	private Date endDate = null;

	@JsonProperty("dose")
	private Double dose;

	@JsonProperty("dose_unit")
	// @RefValueExists
	@ManyToOne
	private Reference doseUnit = null;

	@JsonProperty("frequency")
	private String frequency;

	/**
	 * Init HATEOAS links
	 */
	@PostLoad
	public void initLinks() {
		this.addLink(Links.REL_SELF, "subject/" + getAnimalSubject().getId() + "/therapy/" + getId());
	}

	@Override
	@GeneratedValue(strategy = GenerationType.IDENTITY, generator = "IdOrGenerate")
	@GenericGenerator(name = "IdOrGenerate", strategy = "increment")
	public Long getId() {
		return super.getId();
	}

	public SubjectTherapy subject(AnimalSubject animalSubject) {
		this.animalSubject = animalSubject;
		return this;
	}

	@ApiModelProperty(value = "none")
	public AnimalSubject getAnimalSubject() {
		return animalSubject;
	}

	public void setAnimalSubject(AnimalSubject animalSubject) {
		this.animalSubject = animalSubject;
	}

	public SubjectTherapy therapy(Therapy therapy) {
		this.therapy = therapy;
		return this;
	}

	@ApiModelProperty(value = "none")
	public Therapy getTherapy() {
		return therapy;
	}

	public void setTherapy(Therapy therapy) {
		this.therapy = therapy;
	}

	public SubjectTherapy startDate(Date startDate) {
		this.startDate = startDate;
		return this;
	}

	@ApiModelProperty(value = "none")
	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public SubjectTherapy endDate(Date endDate) {
		this.endDate = endDate;
		return this;
	}

	@ApiModelProperty(value = "none")
	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public SubjectTherapy dose(Double dose) {
		this.dose = dose;
		return this;
	}

	@ApiModelProperty(value = "none")
	public Double getDose() {
		return dose;
	}

	public void setDose(Double dose) {
		this.dose = dose;
	}

	public SubjectTherapy doseUnit(Reference unit) {
		this.doseUnit = unit;
		return this;
	}

	@ApiModelProperty(value = "none")
	public Reference getDoseUnit() {
		return doseUnit;
	}

	public void setDoseUnit(Reference unit) {
		this.doseUnit = unit;
	}

	public SubjectTherapy frequency(String frequency) {
		this.frequency = frequency;
		return this;
	}

	@ApiModelProperty(value = "none")
	public String getFrequency() {
		return frequency;
	}

	public void setFrequency(String frequency) {
		this.frequency = frequency;
	}

	@Override
	public boolean equals(java.lang.Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		SubjectTherapy subjectTherapy = (SubjectTherapy) o;
		return Objects.equals(this.therapy, subjectTherapy.therapy);
	}

	@Override
	public int hashCode() {
		return Objects.hash(therapy);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class SubjectTherapies {\n");

		// sb.append(" subject: ").append(toIndentedString(subject)).append("\n");
		sb.append("    therapy: ").append(toIndentedString(therapy)).append("\n");
		sb.append("    dose: ").append(toIndentedString(dose)).append("\n");
		sb.append("    unit: ").append(toIndentedString(doseUnit)).append("\n");
		sb.append("    frequency: ").append(toIndentedString(frequency)).append("\n");
		sb.append("    startDate: ").append(toIndentedString(startDate)).append("\n");
		sb.append("    endDate: ").append(toIndentedString(endDate)).append("\n");
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
