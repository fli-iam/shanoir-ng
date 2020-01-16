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

package org.shanoir.ng.preclinical.anesthetics.examination_anesthetics;

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
import org.shanoir.ng.preclinical.anesthetics.anesthetic.Anesthetic;
import org.shanoir.ng.preclinical.references.Reference;
import org.shanoir.ng.shared.hateoas.HalEntity;
import org.shanoir.ng.shared.hateoas.Links;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.swagger.annotations.ApiModelProperty;
/**
 * Examination Anesthetic
 */
@Entity
@Table(name = "examination_anesthetic")
@JsonPropertyOrder({ "_links" })
public class ExaminationAnesthetic extends HalEntity  {
  
  @JsonProperty("examination_id")
  @NotNull
  private Long examinationId;
	
  @JsonProperty("anesthetic")
  @ManyToOne
  @NotNull
  private Anesthetic anesthetic = null;
 
  @JsonProperty("dose")
  private Double dose;
  
  @JsonProperty("dose_unit")
  //@RefValueExists
  @ManyToOne
  private Reference doseUnit = null;
  
  @JsonProperty("injection_interval")
  private String injectionInterval;
  
  @JsonProperty("injection_site")
  private String injectionSite;
  
  @JsonProperty("injection_type")
  private String injectionType;
  
  @JsonProperty("startDate")
  private Date startDate = null;

  @JsonProperty("endDate")
  private Date endDate = null;
  
  /**
	* Init HATEOAS links
	*/
  @PostLoad
  public void initLinks() {
	  this.addLink(Links.REL_SELF, "examination/"+ examinationId +"/anesthetic/" + getId());
  }
			
  @Override
  @GeneratedValue(strategy = GenerationType.IDENTITY, generator = "IdOrGenerate")
  @GenericGenerator(name = "IdOrGenerate", strategy = "increment")
  public Long getId() {
	  return super.getId();
  }
  
  public ExaminationAnesthetic examinationId(Long id) {
    this.examinationId = id;
    return this;
  }

  @ApiModelProperty(value = "none")
  public Long getExaminationId() {
    return examinationId;
  }

  public void setExaminationId(Long id) {
    this.examinationId = id;
  }
  	  
  public ExaminationAnesthetic anesthetic(Anesthetic anesthetic) {
    this.anesthetic = anesthetic;
    return this;
  }
  
  @ApiModelProperty(value = "none")
  public Anesthetic getAnesthetic() {
    return anesthetic;
  }

  public void setAnesthetic(Anesthetic anesthetic) {
    this.anesthetic = anesthetic;
  }
  
  public ExaminationAnesthetic dose(Double dose) {
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
  
  public ExaminationAnesthetic doseUnit(Reference unit) {
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
  
  public ExaminationAnesthetic injectionInterval(String interval) {
    this.injectionInterval = interval;
    return this;
  }

  @ApiModelProperty(value = "none")
  public String getInjectionInterval() {
    return injectionInterval;
  }

  public void setInjectionInterval(String interval) {
    this.injectionInterval = interval;
  }
  
  public ExaminationAnesthetic injectionSite(String site) {
    this.injectionSite = site;
    return this;
  }

  @ApiModelProperty(value = "none")
  public String getInjectionSite() {
    return injectionSite;
  }

  public void setInjectionSite(String site) {
    this.injectionSite = site;
  }
  
  public ExaminationAnesthetic injectionType(String type) {
    this.injectionType = type;
    return this;
  }

  @ApiModelProperty(value = "none")
  public String getInjectionType() {
    return injectionType;
  }

  public void setInjectionType(String type) {
    this.injectionType = type;
  }
  
  public ExaminationAnesthetic startDate(Date startDate) {
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

  public ExaminationAnesthetic endDate(Date endDate) {
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
  public int hashCode() {
	return Objects.hash(examinationId, anesthetic, injectionInterval, injectionSite, injectionType);
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
	ExaminationAnesthetic other = (ExaminationAnesthetic) obj;
	if (anesthetic == null) {
		if (other.anesthetic != null) {
			return false;
		}
	} else if (!anesthetic.equals(other.anesthetic)) {
		return false;
	}
	if (examinationId == null) {
		if (other.examinationId != null) {
			return false;
		}
	} else if (!examinationId.equals(other.examinationId)) {
		return false;
	}
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
	return true;
  }

@Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ExaminationAnesthetic {\n");
    sb.append("    examinationId: ").append(toIndentedString(examinationId)).append("\n");
    sb.append("    anesthetic: ").append(toIndentedString(anesthetic)).append("\n");
    sb.append("    dose: ").append(toIndentedString(dose)).append("\n");
    sb.append("    unit: ").append(toIndentedString(doseUnit)).append("\n");
    sb.append("    injection_interval: ").append(toIndentedString(injectionInterval)).append("\n");
    sb.append("    injection_site: ").append(toIndentedString(injectionSite)).append("\n");
    sb.append("    injection_type: ").append(toIndentedString(injectionType)).append("\n");
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

