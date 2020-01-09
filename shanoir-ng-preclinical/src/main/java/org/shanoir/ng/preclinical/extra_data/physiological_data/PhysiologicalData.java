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

package org.shanoir.ng.preclinical.extra_data.physiological_data;

import java.util.Objects;

import javax.persistence.Entity;
import javax.validation.constraints.NotNull;

import org.shanoir.ng.preclinical.extra_data.examination_extra_data.ExaminationExtraData;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.swagger.annotations.ApiModelProperty;
/**
 * Examination Extra Data
 */
@Entity
@JsonPropertyOrder({ "_links", "examinationId", "filename", "has_heart_rate","has_respiratory_rate","has_sao2","has_temperature" })
public class PhysiologicalData extends ExaminationExtraData   {
	
  @JsonProperty("has_heart_rate")
  @NotNull
  private boolean hasHeartRate = false;
  
  @JsonProperty("has_respiratory_rate")
  @NotNull
  private boolean hasRespiratoryRate = false;
  
  @JsonProperty("has_sao2")
  @NotNull
  private boolean hasSao2 = false;
  
  @JsonProperty("has_temperature")
  @NotNull
  private boolean hasTemperature = false;
	
 
  
  public PhysiologicalData hasHeartRate(boolean hasHeartRate) {
    this.hasHeartRate = hasHeartRate;
    return this;
  }

  @ApiModelProperty(value = "none")
  public boolean getHasHeartRate() {
    return hasHeartRate;
  }

  public void setHasHeartRate(boolean hasHeartRate) {
    this.hasHeartRate = hasHeartRate;
  }
  
  public PhysiologicalData hasRespiratoryRate(boolean hasRespiratoryRate) {
    this.hasRespiratoryRate = hasRespiratoryRate;
    return this;
  }

  @ApiModelProperty(value = "none")
  public boolean getHasRespiratoryRate() {
    return hasRespiratoryRate;
  }

  public void setHasRespiratoryRate(boolean hasRespiratoryRate) {
    this.hasRespiratoryRate = hasRespiratoryRate;
  }
  
  public PhysiologicalData hasSao2(boolean hasSao2) {
    this.hasSao2 = hasSao2;
    return this;
  }

  @ApiModelProperty(value = "none")
  public boolean getHasSao2() {
    return hasSao2;
  }

  public void setHasSao2(boolean hasSao2) {
    this.hasSao2 = hasSao2;
  }
  
  public PhysiologicalData hasTemperature(boolean hasTemperature) {
    this.hasTemperature = hasTemperature;
    return this;
  }

  @ApiModelProperty(value = "none")
  public boolean getHasTemperature() {
    return hasTemperature;
  }

  public void setHasTemperature(boolean hasTemperature) {
    this.hasTemperature = hasTemperature;
  }
  



  @Override
  public int hashCode() {
	return Objects.hash(getExaminationId(), getFilename(),getFilepath(),hasHeartRate, hasRespiratoryRate, hasSao2, hasTemperature);
  }

@Override
public boolean equals(Object obj) {
	if (this == obj) {
		return true;
	}
	if (!super.equals(obj)) {
		return false;
	}
	if (getClass() != obj.getClass()) {
		return false;
	}
	PhysiologicalData other = (PhysiologicalData) obj;
	if (hasHeartRate != other.hasHeartRate) {
		return false;
	}
	if (hasRespiratoryRate != other.hasRespiratoryRate) {
		return false;
	}
	if (hasSao2 != other.hasSao2) {
		return false;
	}
	return hasTemperature != other.hasTemperature;
}

@Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class PhysiologicalData {\n");
    
    sb.append("    examinationId: ").append(toIndentedString(getExaminationId())).append("\n");
    sb.append("    filename: ").append(toIndentedString(getFilename())).append("\n");
    sb.append("    filepath: ").append(toIndentedString(getFilepath())).append("\n");
    sb.append("    has_heart_rate: ").append(toIndentedString(hasHeartRate)).append("\n");
    sb.append("    has_respiratory_rate: ").append(toIndentedString(hasRespiratoryRate)).append("\n");
    sb.append("    has_sao2: ").append(toIndentedString(hasSao2)).append("\n");
    sb.append("    has_temperature: ").append(toIndentedString(hasTemperature)).append("\n");
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

