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
  private boolean has_heart_rate = false;
  
  @JsonProperty("has_respiratory_rate")
  @NotNull
  private boolean has_respiratory_rate = false;
  
  @JsonProperty("has_sao2")
  @NotNull
  private boolean has_sao2 = false;
  
  @JsonProperty("has_temperature")
  @NotNull
  private boolean has_temperature = false;
	
 
  
  public PhysiologicalData has_heart_rate(boolean has_heart_rate) {
    this.has_heart_rate = has_heart_rate;
    return this;
  }

  @ApiModelProperty(value = "none")
  public boolean getHas_heart_rate() {
    return has_heart_rate;
  }

  public void setHas_heart_rate(boolean has_heart_rate) {
    this.has_heart_rate = has_heart_rate;
  }
  
  public PhysiologicalData has_respiratory_rate(boolean has_respiratory_rate) {
    this.has_respiratory_rate = has_respiratory_rate;
    return this;
  }

  @ApiModelProperty(value = "none")
  public boolean getHas_respiratory_rate() {
    return has_respiratory_rate;
  }

  public void setHas_respiratory_rate(boolean has_respiratory_rate) {
    this.has_respiratory_rate = has_respiratory_rate;
  }
  
  public PhysiologicalData has_sao2(boolean has_sao2) {
    this.has_sao2 = has_sao2;
    return this;
  }

  @ApiModelProperty(value = "none")
  public boolean getHas_sao2() {
    return has_sao2;
  }

  public void setHas_sao2(boolean has_sao2) {
    this.has_sao2 = has_sao2;
  }
  
  public PhysiologicalData has_temperature(boolean has_temperature) {
    this.has_temperature = has_temperature;
    return this;
  }

  @ApiModelProperty(value = "none")
  public boolean getHas_temperature() {
    return has_temperature;
  }

  public void setHas_temperature(boolean has_temperature) {
    this.has_temperature = has_temperature;
  }
  



  @Override
  public int hashCode() {
	return Objects.hash(getExaminationId(), getFilename(),getFilepath(),has_heart_rate, has_respiratory_rate, has_sao2, has_temperature);
  }

@Override
public boolean equals(Object obj) {
	if (this == obj)
		return true;
	if (!super.equals(obj))
		return false;
	if (getClass() != obj.getClass())
		return false;
	PhysiologicalData other = (PhysiologicalData) obj;
	if (has_heart_rate != other.has_heart_rate)
		return false;
	if (has_respiratory_rate != other.has_respiratory_rate)
		return false;
	if (has_sao2 != other.has_sao2)
		return false;
	if (has_temperature != other.has_temperature)
		return false;
	return true;
}

@Override	
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class PhysiologicalData {\n");
    
    sb.append("    examinationId: ").append(toIndentedString(getExaminationId())).append("\n");
    sb.append("    filename: ").append(toIndentedString(getFilename())).append("\n");
    sb.append("    filepath: ").append(toIndentedString(getFilepath())).append("\n");
    sb.append("    has_heart_rate: ").append(toIndentedString(has_heart_rate)).append("\n");
    sb.append("    has_respiratory_rate: ").append(toIndentedString(has_respiratory_rate)).append("\n");
    sb.append("    has_sao2: ").append(toIndentedString(has_sao2)).append("\n");
    sb.append("    has_temperature: ").append(toIndentedString(has_temperature)).append("\n");
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

