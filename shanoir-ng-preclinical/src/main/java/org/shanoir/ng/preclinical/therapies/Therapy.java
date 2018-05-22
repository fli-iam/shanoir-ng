package org.shanoir.ng.preclinical.therapies;

import java.util.Objects;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.PostLoad;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.GenericGenerator;
import org.shanoir.ng.shared.hateoas.HalEntity;
import org.shanoir.ng.shared.hateoas.Links;
import org.shanoir.ng.shared.validation.Unique;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.swagger.annotations.ApiModelProperty;
/**
 * Therapy
 */
@Entity
@Table(name = "therapy")
@JsonPropertyOrder({ "_links", "name","therapyType","comment" })
public class Therapy extends HalEntity   {
	
  @JsonProperty("name")
  @Unique
  private String name = null;
  
  @JsonProperty("comment")
  private String comment = null;
  
  @JsonProperty("therapyType")
  @NotNull
  @Enumerated(EnumType.STRING)
  private TherapyType therapyType;

  /**
	* Init HATEOAS links
	*/
  @PostLoad
	public void initLinks() {
		this.addLink(Links.REL_SELF, "therapy/" + getId());
  }
	
  @Override
  @GeneratedValue(strategy = GenerationType.IDENTITY, generator = "IdOrGenerate")
  @GenericGenerator(name = "IdOrGenerate", strategy = "increment")
  public Long getId() {
	return super.getId();
  }
  
  
  public Therapy name(String name) {
    this.name = name;
    return this;
  }

  @ApiModelProperty(value = "none")
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
  
  public Therapy comment(String comment) {
    this.comment = comment;
    return this;
  }
  
  @ApiModelProperty(value = "none")
  public String getComment() {
    return comment;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }
  
  public Therapy therapyType(TherapyType therapyType) {
    this.therapyType = therapyType;
    return this;
  }

  @ApiModelProperty(value = "none")
  public TherapyType getTherapyType() {
    return therapyType;
  }

  public void setTherapyType(TherapyType therapyType) {
    this.therapyType = therapyType;
  }

  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Therapy therapy = (Therapy) o;
    return Objects.equals(this.name, therapy.name) &&
    		Objects.equals(this.therapyType, therapy.therapyType);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Therapy {\n");
    
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    therapy type: ").append(toIndentedString(therapyType)).append("\n");
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

