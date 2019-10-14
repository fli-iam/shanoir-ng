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

package org.shanoir.ng.preclinical.pathologies.pathology_models;

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
import org.shanoir.ng.shared.hateoas.HalEntity;
import org.shanoir.ng.shared.hateoas.Links;
import org.shanoir.ng.shared.validation.Unique;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.swagger.annotations.ApiModelProperty;
/**
 * PathologyModel
 */
@Entity
@Table(name = "pathology_model")
@JsonPropertyOrder({ "_links", "type", "value" })
//@JsonIdentityInfo(generator =ObjectIdGenerators.IntSequenceGenerator.class,property="@id", scope = PathologyModel.class)
public class PathologyModel extends HalEntity   {
	
  @JsonProperty("name")
  @Unique
  private String name = null;

  @JsonProperty("comment")
  private String comment = null;
  
  @JsonProperty("filename")
  private String filename;
  
  @JsonIgnore
  @JsonProperty("filepath")
  private String filepath;
  
  @ManyToOne
  //fetch = FetchType.LAZY, optional = false)
  //@JoinColumn(name = "building_id")
  @NotNull
  @JsonProperty("pathology")
  @JsonIgnore
  private Pathology pathology;
  
  /**
	* Init HATEOAS links
  */
  @PostLoad
	public void initLinks() {
		this.addLink(Links.REL_SELF, "pathology/model/" + getId());
  }

  @Override
  @GeneratedValue(strategy = GenerationType.IDENTITY, generator = "IdOrGenerate")
  @GenericGenerator(name = "IdOrGenerate", strategy = "increment")
  public Long getId() {
	return super.getId();
  }
 
  
  public PathologyModel name(String name) {
    this.name = name;
    return this;
  }
  
  /**
   * none
   * @return type
  **/
  @ApiModelProperty(value = "none")
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
  
  public PathologyModel comment(String comment) {
    this.comment = comment;
    return this;
  }
  
  /**
   * none
   * @return type
  **/
  @ApiModelProperty(value = "none")
  public String getComment() {
    return comment;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }
  
  public PathologyModel filename(String filename) {
    this.filename = filename;
    return this;
  }
  
  @ApiModelProperty(value = "none")
  public String getFilename() {
    return filename;
  }

  public void setFilename(String filename) {
    this.filename = filename;
  }
	   
  @JsonIgnore
  @ApiModelProperty(value = "none")
  public String getFilepath() {
    return filepath;
  }

  public void setFilepath(String filepath) {
    this.filepath = filepath;
  }
  
  public PathologyModel pathology(Pathology pathology) {
    this.pathology = pathology;
    return this;
  }
  
  /**
   * none
   * @return type
  **/
  @ApiModelProperty(value = "none")
  public Pathology getPathology() {
    return pathology;
  }

  public void setPathology(Pathology pathology) {
    this.pathology = pathology;
  }
  
  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PathologyModel model = (PathologyModel) o;
    return Objects.equals(this.name, model.name) && Objects.equals(this.pathology, model.pathology);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, pathology);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class PathologyModel {\n");
    
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    comment: ").append(toIndentedString(comment)).append("\n");
    sb.append("    specifications file: ").append(toIndentedString(filename)).append("\n");
    sb.append("    pathology: ").append(toIndentedString(pathology.toString())).append("\n");
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

