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

package io.swagger.model;

import java.math.BigDecimal;
import java.util.Objects;

import io.swagger.annotations.ApiModelProperty;




/**
 * FlatScore
 */
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.SpringCodegen", date = "2016-10-14T09:15:21.251Z")

public class FlatScore   {
  private Float value = null;

  private BigDecimal studyId = null;

  private BigDecimal metricId = null;

  private BigDecimal challengerId = null;

  private BigDecimal patientId = null;

  private BigDecimal inputDatasetId = null;

  public FlatScore value(Float value) {
    this.value = value;
    return this;
  }

   /**
   * Get value
   * @return value
  **/
  @ApiModelProperty(value = "")
  public Float getValue() {
    return value;
  }

  public void setValue(Float value) {
    this.value = value;
  }

  public FlatScore studyId(BigDecimal studyId) {
    this.studyId = studyId;
    return this;
  }

   /**
   * Get studyId
   * @return studyId
  **/
  @ApiModelProperty(required = true, value = "")
  public BigDecimal getStudyId() {
    return studyId;
  }

  public void setStudyId(BigDecimal studyId) {
    this.studyId = studyId;
  }

  public FlatScore metricId(BigDecimal metricId) {
    this.metricId = metricId;
    return this;
  }

   /**
   * Get metricId
   * @return metricId
  **/
  @ApiModelProperty(required = true, value = "")
  public BigDecimal getMetricId() {
    return metricId;
  }

  public void setMetricId(BigDecimal metricId) {
    this.metricId = metricId;
  }

  public FlatScore challengerId(BigDecimal challengerId) {
    this.challengerId = challengerId;
    return this;
  }

   /**
   * Get challengerId
   * @return challengerId
  **/
  @ApiModelProperty(required = true, value = "")
  public BigDecimal getChallengerId() {
    return challengerId;
  }

  public void setChallengerId(BigDecimal challengerId) {
    this.challengerId = challengerId;
  }

  public FlatScore patientId(BigDecimal patientId) {
    this.patientId = patientId;
    return this;
  }

   /**
   * Get patientId
   * @return patientId
  **/
  @ApiModelProperty(required = true, value = "")
  public BigDecimal getPatientId() {
    return patientId;
  }

  public void setPatientId(BigDecimal patientId) {
    this.patientId = patientId;
  }

  public FlatScore inputDatasetId(BigDecimal inputDatasetId) {
    this.inputDatasetId = inputDatasetId;
    return this;
  }

   /**
   * Get inputDatasetId
   * @return inputDatasetId
  **/
  @ApiModelProperty(value = "")
  public BigDecimal getInputDatasetId() {
    return inputDatasetId;
  }

  public void setInputDatasetId(BigDecimal inputDatasetId) {
    this.inputDatasetId = inputDatasetId;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    FlatScore flatScore = (FlatScore) o;
    return Objects.equals(this.value, flatScore.value) &&
        Objects.equals(this.studyId, flatScore.studyId) &&
        Objects.equals(this.metricId, flatScore.metricId) &&
        Objects.equals(this.challengerId, flatScore.challengerId) &&
        Objects.equals(this.patientId, flatScore.patientId) &&
        Objects.equals(this.inputDatasetId, flatScore.inputDatasetId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(value, studyId, metricId, challengerId, patientId, inputDatasetId);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class FlatScore {\n");
    
    sb.append("    value: ").append(toIndentedString(value)).append("\n");
    sb.append("    studyId: ").append(toIndentedString(studyId)).append("\n");
    sb.append("    metricId: ").append(toIndentedString(metricId)).append("\n");
    sb.append("    challengerId: ").append(toIndentedString(challengerId)).append("\n");
    sb.append("    patientId: ").append(toIndentedString(patientId)).append("\n");
    sb.append("    inputDatasetId: ").append(toIndentedString(inputDatasetId)).append("\n");
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

