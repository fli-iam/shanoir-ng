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

package org.shanoir.ng.preclinical.anesthetics.ingredients;

import java.util.Objects;

import org.shanoir.ng.preclinical.anesthetics.anesthetic.Anesthetic;
import org.shanoir.ng.preclinical.references.Reference;
import org.shanoir.ng.shared.hateoas.HalEntity;
import org.shanoir.ng.shared.hateoas.Links;
import org.shanoir.ng.shared.validation.RefValueExists;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PostLoad;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
/**
 * Anesthetic Ingredient
 */
@Entity
@Table(name = "anesthetic_ingredient")
@JsonPropertyOrder({ "_links", "name","concentration","concentration_unit" })
public class AnestheticIngredient extends HalEntity   {
	
  @ManyToOne
  @NotNull
  @JsonIgnore
  @JsonManagedReference
  private Anesthetic anesthetic = null;
	
  @JsonProperty("name")
  @RefValueExists
  @ManyToOne
  private Reference name = null;

  @JsonProperty("concentration")
  private Double concentration;

  @JsonProperty("concentration_unit")
  @RefValueExists
  @ManyToOne
  private Reference concentrationUnit = null;

  /**
	* Init HATEOAS links
	*/
  @PostLoad
  public void initLinks() {
	this.addLink(Links.REL_SELF, "anesthetic/"+ anesthetic.getId() +"/ingredient/" + getId());
  }

  public AnestheticIngredient anesthetic(Anesthetic anesthetic) {
    this.anesthetic = anesthetic;
    return this;
  }

  @Schema(name = "none")
  public Anesthetic getAnesthetic() {
    return anesthetic;
  }

  public void setAnesthetic(Anesthetic anesthetic) {
    this.anesthetic = anesthetic;
  }

  public AnestheticIngredient name(Reference name) {
    this.name = name;
    return this;
  }

  @Schema(name = "none")
  public Reference getName() {
    return name;
  }

  public void setName(Reference name) {
    this.name = name;
  }


  public AnestheticIngredient concentration(Double concentration) {
    this.concentration = concentration;
    return this;
  }

  @Schema(name = "none")
  public Double getConcentration() {
    return concentration;
  }

  public void setConcentration(Double concentration) {
    this.concentration = concentration;
  }

  public AnestheticIngredient concentrationUnit(Reference unit) {
    this.concentrationUnit = unit;
    return this;
  }

  @Schema(name = "none")
  public Reference getConcentrationUnit() {
    return concentrationUnit;
  }

  public void setConcentrationUnit(Reference unit) {
    this.concentrationUnit = unit;
  }


  @Override
  public int hashCode() {
	return Objects.hash(name);
  }
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		AnestheticIngredient other = (AnestheticIngredient) obj;
		if (concentration == null) {
			if (other.concentration != null) {
				return false;
			}
		} else if (!concentration.equals(other.concentration)) {
			return false;
		}
		if (concentrationUnit == null) {
			if (other.concentrationUnit != null) {
				return false;
			}
		} else if (!concentrationUnit.equals(other.concentrationUnit)) {
			return false;
		}
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		if (anesthetic == null) {
			if (other.anesthetic != null) {
				return false;
			}
		} else if (!anesthetic.equals(other.anesthetic)) {
			return false;
		}
		return true;
	}

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AnestheticIngredient {\n");

    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    concentration: ").append(toIndentedString(concentration)).append("\n");
    sb.append("    concentration unit: ").append(toIndentedString(concentrationUnit)).append("\n");
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

