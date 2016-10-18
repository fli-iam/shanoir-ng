package io.swagger.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import io.swagger.annotations.ApiModelProperty;




/**
 * ResetObject
 */
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.SpringCodegen", date = "2016-10-13T14:21:53.559Z")

public class ResetObject   {
  private List<FlatScore> scoreList = new ArrayList<FlatScore>();

  private List<Study> studyList = new ArrayList<Study>();

  private List<Metric> metricList = new ArrayList<Metric>();

  private List<Challenger> challengerList = new ArrayList<Challenger>();

  private List<Patient> patientList = new ArrayList<Patient>();

  public ResetObject scoreList(List<FlatScore> scoreList) {
    this.scoreList = scoreList;
    return this;
  }

  public ResetObject addScoreListItem(FlatScore scoreListItem) {
    this.scoreList.add(scoreListItem);
    return this;
  }

   /**
   * Get scoreList
   * @return scoreList
  **/
  @ApiModelProperty(required = true, value = "")
  public List<FlatScore> getScoreList() {
    return scoreList;
  }

  public void setScoreList(List<FlatScore> scoreList) {
    this.scoreList = scoreList;
  }

  public ResetObject studyList(List<Study> studyList) {
    this.studyList = studyList;
    return this;
  }

  public ResetObject addStudyListItem(Study studyListItem) {
    this.studyList.add(studyListItem);
    return this;
  }

   /**
   * Get studyList
   * @return studyList
  **/
  @ApiModelProperty(value = "")
  public List<Study> getStudyList() {
    return studyList;
  }

  public void setStudyList(List<Study> studyList) {
    this.studyList = studyList;
  }

  public ResetObject metricList(List<Metric> metricList) {
    this.metricList = metricList;
    return this;
  }

  public ResetObject addMetricListItem(Metric metricListItem) {
    this.metricList.add(metricListItem);
    return this;
  }

   /**
   * Get metricList
   * @return metricList
  **/
  @ApiModelProperty(value = "")
  public List<Metric> getMetricList() {
    return metricList;
  }

  public void setMetricList(List<Metric> metricList) {
    this.metricList = metricList;
  }

  public ResetObject challengerList(List<Challenger> challengerList) {
    this.challengerList = challengerList;
    return this;
  }

  public ResetObject addChallengerListItem(Challenger challengerListItem) {
    this.challengerList.add(challengerListItem);
    return this;
  }

   /**
   * Get challengerList
   * @return challengerList
  **/
  @ApiModelProperty(value = "")
  public List<Challenger> getChallengerList() {
    return challengerList;
  }

  public void setChallengerList(List<Challenger> challengerList) {
    this.challengerList = challengerList;
  }

  public ResetObject patientList(List<Patient> patientList) {
    this.patientList = patientList;
    return this;
  }

  public ResetObject addPatientListItem(Patient patientListItem) {
    this.patientList.add(patientListItem);
    return this;
  }

   /**
   * Get patientList
   * @return patientList
  **/
  @ApiModelProperty(value = "")
  public List<Patient> getPatientList() {
    return patientList;
  }

  public void setPatientList(List<Patient> patientList) {
    this.patientList = patientList;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ResetObject resetObject = (ResetObject) o;
    return Objects.equals(this.scoreList, resetObject.scoreList) &&
        Objects.equals(this.studyList, resetObject.studyList) &&
        Objects.equals(this.metricList, resetObject.metricList) &&
        Objects.equals(this.challengerList, resetObject.challengerList) &&
        Objects.equals(this.patientList, resetObject.patientList);
  }

  @Override
  public int hashCode() {
    return Objects.hash(scoreList, studyList, metricList, challengerList, patientList);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ResetObject {\n");
    
    sb.append("    scoreList: ").append(toIndentedString(scoreList)).append("\n");
    sb.append("    studyList: ").append(toIndentedString(studyList)).append("\n");
    sb.append("    metricList: ").append(toIndentedString(metricList)).append("\n");
    sb.append("    challengerList: ").append(toIndentedString(challengerList)).append("\n");
    sb.append("    patientList: ").append(toIndentedString(patientList)).append("\n");
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

