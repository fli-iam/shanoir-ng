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

package org.shanoir.ng.preclinical.anesthetics.anesthetic;

import java.util.Objects;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.OneToMany;
import javax.persistence.PostLoad;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.GenericGenerator;
import org.shanoir.ng.preclinical.anesthetics.ingredients.AnestheticIngredient;
import org.shanoir.ng.shared.hateoas.HalEntity;
import org.shanoir.ng.shared.hateoas.Links;
import org.shanoir.ng.shared.validation.Unique;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.swagger.annotations.ApiModelProperty;
/**
 * Anesthetic
 */
@Entity
@Table(name = "anesthetic")
@JsonPropertyOrder({ "_links", "name","anestheticType","comment" })
public class Anesthetic extends HalEntity   {
	
  @JsonProperty("name")
  @Unique
  @NotNull
  private String name = null;
  
  @JsonProperty("comment")
  private String comment;
  
  @JsonProperty("anestheticType")
  @Enumerated(EnumType.STRING)
  private AnestheticType anestheticType;
  
  @OneToMany(mappedBy = "anesthetic", orphanRemoval = true)
  @JsonProperty("ingredients")
  @JsonBackReference(value = "ingredients")
  private Set<AnestheticIngredient> ingredients;

  
  /**
	* Init HATEOAS links
	*/
  @PostLoad
  public void initLinks() {
		this.addLink(Links.REL_SELF, "anesthetic/" + getId());
  }
		
  @Override
  @GeneratedValue(strategy = GenerationType.IDENTITY, generator = "IdOrGenerate")
  @GenericGenerator(name = "IdOrGenerate", strategy = "increment")
  public Long getId() {
	return super.getId();
  }
  
  public Anesthetic name(String name) {
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
  
    
  public Anesthetic comment(String comment) {
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
  
  public Anesthetic anestheticType(AnestheticType anestheticType) {
    this.anestheticType = anestheticType;
    return this;
  }

  @ApiModelProperty(value = "none")
  public AnestheticType getAnestheticType() {
    return anestheticType;
  }

  public void setAnestheticType(AnestheticType anestheticType) {
    this.anestheticType = anestheticType;
  }
  
  public Anesthetic ingredients(Set<AnestheticIngredient> ingredients) {
    this.ingredients = ingredients;
    return this;
  }

  public Set<AnestheticIngredient> getIngredients() {
    return ingredients;
  }

  public void setIngredients(Set<AnestheticIngredient> ingredients) {
    this.ingredients = ingredients;
  }

  
  @Override
  public int hashCode() {
	return Objects.hash(name, anestheticType);
  }
	
  

  @Override
  public boolean equals(Object obj) {
	if (this == obj)
		return true;
	if (obj == null)
		return false;
	if (getClass() != obj.getClass())
		return false;
	Anesthetic other = (Anesthetic) obj;
	if (anestheticType == null) {
		if (other.anestheticType != null)
			return false;
	} else if (!anestheticType.equals(other.anestheticType))
		return false;
	if (ingredients == null) {
		if (other.ingredients != null)
			return false;
	} else if (!ingredients.equals(other.ingredients))
		return false;
	if (name == null) {
		if (other.name != null)
			return false;
	} else if (!name.equals(other.name))
		return false;
	return true;
  }

  @Override	
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Anesthetic {\n");
    
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    anesthetictype: ").append(toIndentedString(anestheticType)).append("\n");
    sb.append("    comment: ").append(toIndentedString(comment)).append("\n");
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

