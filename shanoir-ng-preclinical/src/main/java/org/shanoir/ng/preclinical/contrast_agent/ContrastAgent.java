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

package org.shanoir.ng.preclinical.contrast_agent;

import java.util.Objects;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.ManyToOne;
import javax.persistence.PostLoad;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.GenericGenerator;
import org.shanoir.ng.preclinical.references.InjectionInterval;
import org.shanoir.ng.preclinical.references.InjectionSite;
import org.shanoir.ng.preclinical.references.InjectionType;
import org.shanoir.ng.preclinical.references.Reference;
import org.shanoir.ng.shared.hateoas.HalEntity;
import org.shanoir.ng.shared.hateoas.Links;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.swagger.annotations.ApiModelProperty;
/**
 * Contrast Agent
 */
@Entity
@Table(name = "contrast_agent")
@JsonPropertyOrder({ "_links", "name","","" })
public class ContrastAgent extends HalEntity   {
	
  @JsonProperty("protocol_id")
  @NotNull
  private Long protocolId;
	
  @JsonProperty("name")
  //@RefValueExists
  @ManyToOne
  private Reference name = null;
	
  @JsonProperty("manufactured_name")
  private String manufacturedName = null;
  
  @JsonProperty("concentration")
  private Double concentration;
  
  @JsonProperty("concentration_unit")
  //@RefValueExists
  @ManyToOne
  private Reference concentrationUnit = null;
  
  @JsonProperty("dose")
  private Double dose;
  
  @JsonProperty("dose_unit")
  //@RefValueExists
  @ManyToOne
  private Reference doseUnit = null;
  
  @JsonProperty("injection_interval")
  @Enumerated(EnumType.STRING)
  private InjectionInterval injectionInterval;
  
  @JsonProperty("injection_site")
  @Enumerated(EnumType.STRING)
  private InjectionSite injectionSite;
  
  @JsonProperty("injection_type")
  @Enumerated(EnumType.STRING)
  private InjectionType injectionType;
  
  
  /**
	* Init HATEOAS links
  */
  @PostLoad
	public void initLinks() {
		this.addLink(Links.REL_SELF, "contrastagent/" + getId());
  }
	
  @Override
  @GeneratedValue(strategy = GenerationType.IDENTITY, generator = "IdOrGenerate")
  @GenericGenerator(name = "IdOrGenerate", strategy = "increment")
  public Long getId() {
	return super.getId();
  }
  
  
  
  public Long getProtocolId() {
	return protocolId;
  }

  public void setProtocolId(Long protocolId) {
	this.protocolId = protocolId;
  }

  public ContrastAgent name(Reference name) {
    this.name = name;
    return this;
  }

  @ApiModelProperty(value = "none")
  public Reference getName() {
    return name;
  }

  public void setName(Reference name) {
    this.name = name;
  }
  
  public ContrastAgent manufacturedName(String manufacturedName) {
    this.manufacturedName = manufacturedName;
    return this;
  }
  
  @ApiModelProperty(value = "none")
  public String getManufacturedName() {
    return manufacturedName;
  }

  public void setManufacturedName(String manufacturedName) {
    this.manufacturedName = manufacturedName;
  }
	  
  
  public ContrastAgent concentration(Double concentration) {
    this.concentration = concentration;
    return this;
  }
  
  @ApiModelProperty(value = "none")
  public Double getConcentration() {
    return concentration;
  }

  public void setConcentration(Double concentration) {
    this.concentration = concentration;
  }
  
  public ContrastAgent concentrationUnit(Reference unit) {
    this.concentrationUnit = unit;
    return this;
  }

  @ApiModelProperty(value = "none")
  public Reference getConcentrationUnit() {
    return concentrationUnit;
  }

  public void setConcentrationUnit(Reference unit) {
    this.concentrationUnit = unit;
  }
  
  public ContrastAgent dose(Double dose) {
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
  
  public ContrastAgent doseUnit(Reference unit) {
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

  public ContrastAgent injectionInterval(InjectionInterval interval) {
    this.injectionInterval = interval;
    return this;
  }

  @ApiModelProperty(value = "none")
  public InjectionInterval getInjectionInterval() {
    return injectionInterval;
  }

  public void setInjectionInterval(InjectionInterval interval) {
    this.injectionInterval = interval;
  }
  
  public ContrastAgent injectionSite(InjectionSite site) {
    this.injectionSite = site;
    return this;
  }

  @ApiModelProperty(value = "none")
  public InjectionSite getInjectionSite() {
    return injectionSite;
  }

  public void setInjectionSite(InjectionSite site) {
    this.injectionSite = site;
  }
  
  public ContrastAgent injectionType(InjectionType type) {
    this.injectionType = type;
    return this;
  }

  @ApiModelProperty(value = "none")
  public InjectionType getInjectionType() {
    return injectionType;
  }

  public void setInjectionType(InjectionType type) {
    this.injectionType = type;
  }
  
  

  @Override
  public int hashCode() {
	return Objects.hash(name, manufacturedName,concentration,dose);
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
	ContrastAgent other = (ContrastAgent) obj;
	if (injectionInterval == null) {
		if (other.injectionInterval != null) {
			return false;
		}
	} else if (!injectionInterval.equals(other.injectionInterval)) {
		return false;
	}
	if (injectionSite == null) {
		if (other.injectionSite != null) {
			return false;
		}
	} else if (!injectionSite.equals(other.injectionSite)) {
		return false;
	}
	if (injectionType == null) {
		if (other.injectionType != null) {
			return false;
		}
	} else if (!injectionType.equals(other.injectionType)) {
		return false;
	}
	if (manufacturedName == null) {
		if (other.manufacturedName != null) {
			return false;
		}
	} else if (!manufacturedName.equals(other.manufacturedName)) {
		return false;
	}
	if (name == null) {
		if (other.name != null) {
			return false;
		}
	} else if (!name.equals(other.name)) {
		return false;
	}
	return true;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ContrastAgent {\n");
    
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    manufacturedName: ").append(toIndentedString(manufacturedName)).append("\n");
    sb.append("    concentration: ").append(toIndentedString(concentration)).append("\n");
    sb.append("    unit: ").append(toIndentedString(concentrationUnit)).append("\n");
    sb.append("    dose: ").append(toIndentedString(dose)).append("\n");
    sb.append("    unit: ").append(toIndentedString(doseUnit)).append("\n");
    sb.append("    injection_interval: ").append(toIndentedString(injectionInterval)).append("\n");
    sb.append("    injection_site: ").append(toIndentedString(injectionSite)).append("\n");
    sb.append("    injection_type: ").append(toIndentedString(injectionType)).append("\n");
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

