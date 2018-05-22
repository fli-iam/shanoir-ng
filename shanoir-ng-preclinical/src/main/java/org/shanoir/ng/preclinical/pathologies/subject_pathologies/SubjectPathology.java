package org.shanoir.ng.preclinical.pathologies.subject_pathologies;

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
import org.shanoir.ng.preclinical.pathologies.Pathology;
import org.shanoir.ng.preclinical.pathologies.pathology_models.PathologyModel;
import org.shanoir.ng.preclinical.references.Reference;
import org.shanoir.ng.preclinical.subjects.AnimalSubject;
import org.shanoir.ng.shared.hateoas.HalEntity;
import org.shanoir.ng.shared.hateoas.Links;
import org.shanoir.ng.shared.validation.RefValueExists;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.swagger.annotations.ApiModelProperty;

/**
 * Subject Pathology
 */

@Entity
@Table(name = "subject_pathology")
@JsonPropertyOrder({ "_links", "pathology", "pathologyModel", "location", "startDate", "endDate", "animalSubject" })
public class SubjectPathology extends HalEntity {

	@ManyToOne
	@NotNull
	@JsonIgnore
	@JsonProperty("animalSubject")
	@JsonManagedReference
	private AnimalSubject animalSubject = null;

	@JsonProperty("pathology")
	@ManyToOne
	@NotNull
	private Pathology pathology = null;

	@JsonProperty("pathologyModel")
	@ManyToOne
	// @NotNull
	private PathologyModel pathologyModel = null;

	@JsonProperty("location")
	@RefValueExists
	@ManyToOne
	@NotNull
	private Reference location = null;

	@JsonProperty("startDate")
	private Date startDate = null;

	@JsonProperty("endDate")
	private Date endDate = null;

	/**
	 * Init HATEOAS links
	 */
	@PostLoad
	public void initLinks() {
		this.addLink(Links.REL_SELF, "subject/" + getAnimalSubject().getId() + "/pathology/" + getId());
	}

	@Override
	@GeneratedValue(strategy = GenerationType.IDENTITY, generator = "IdOrGenerate")
	@GenericGenerator(name = "IdOrGenerate", strategy = "increment")
	public Long getId() {
		return super.getId();
	}

	public SubjectPathology subject(AnimalSubject animalSubject) {
		this.animalSubject = animalSubject;
		return this;
	}

	@JsonIgnore
	@ApiModelProperty(value = "none")
	public AnimalSubject getAnimalSubject() {
		return animalSubject;
	}

	public void setAnimalSubject(AnimalSubject animalSubject) {
		this.animalSubject = animalSubject;
	}

	public SubjectPathology pathology(Pathology pathology) {
		this.pathology = pathology;
		return this;
	}

	/**
	 * none
	 * 
	 * @return subjectId
	 **/
	@ApiModelProperty(value = "none")
	public Pathology getPathology() {
		return pathology;
	}

	public void setPathology(Pathology pathology) {
		this.pathology = pathology;
	}

	public SubjectPathology pathologyModel(PathologyModel pathologyModel) {
		this.pathologyModel = pathologyModel;
		return this;
	}

	/**
	 * none
	 * 
	 * @return subjectId
	 **/
	@ApiModelProperty(value = "none")
	public PathologyModel getPathologyModel() {
		return pathologyModel;
	}

	public void setPathologyModel(PathologyModel pathologyModel) {
		this.pathologyModel = pathologyModel;
	}

	public SubjectPathology location(Reference location) {
		this.location = location;
		return this;
	}

	/**
	 * none
	 * 
	 * @return location
	 **/
	@ApiModelProperty(value = "none")
	public Reference getLocation() {
		return location;
	}

	public void setLocation(Reference location) {
		this.location = location;
	}

	public SubjectPathology startDate(Date startDate) {
		this.startDate = startDate;
		return this;
	}

	/**
	 * none
	 * 
	 * @return startDate
	 **/
	@ApiModelProperty(value = "none")
	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public SubjectPathology endDate(Date endDate) {
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

	@Override
	public boolean equals(java.lang.Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		SubjectPathology subjectPathos = (SubjectPathology) o;
		return Objects.equals(this.pathology, subjectPathos.pathology)
				&& Objects.equals(this.pathologyModel, subjectPathos.pathologyModel)
				&& Objects.equals(this.location, subjectPathos.location);
	}

	@Override
	public int hashCode() {
		return Objects.hash(pathology, pathologyModel, location);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class SubjectPathologies {\n");

		// sb.append(" subject: ").append(toIndentedString(subject)).append("\n");
		sb.append("    pathology: ").append(toIndentedString(pathology)).append("\n");
		sb.append("    pathologyModel: ").append(toIndentedString(pathologyModel)).append("\n");
		sb.append("    location: ").append(toIndentedString(location)).append("\n");
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
