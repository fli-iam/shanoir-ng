package io.swagger.model;

import java.math.BigDecimal;
import java.util.Objects;

import io.swagger.annotations.ApiModelProperty;




/**
 * Metric
 */
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.SpringCodegen", date = "2016-09-27T07:56:59.603Z")

public class Metric   {
  private BigDecimal id = null;

  private String name = null;

  private String naN = null;

  private String posInf = null;

  private String negInf = null;

  public Metric id(BigDecimal id) {
    this.id = id;
    return this;
  }

   /**
   * The id of the metric
   * @return id
  **/
  @ApiModelProperty(value = "The id of the metric")
  public BigDecimal getId() {
    return id;
  }

  public void setId(BigDecimal id) {
    this.id = id;
  }

  public Metric name(String name) {
    this.name = name;
    return this;
  }

   /**
   * The name of the metric
   * @return name
  **/
  @ApiModelProperty(required = true, value = "The name of the metric")
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Metric naN(String naN) {
    this.naN = naN;
    return this;
  }

   /**
   * What to do if a value for this metric is NaN
   * @return naN
  **/
  @ApiModelProperty(value = "What to do if a value for this metric is NaN")
  public String getNaN() {
    return naN;
  }

  public void setNaN(String naN) {
    this.naN = naN;
  }

  public Metric posInf(String posInf) {
    this.posInf = posInf;
    return this;
  }

   /**
   * What to do if a value for this metric is a negative infinite
   * @return posInf
  **/
  @ApiModelProperty(value = "What to do if a value for this metric is a negative infinite")
  public String getPosInf() {
    return posInf;
  }

  public void setPosInf(String posInf) {
    this.posInf = posInf;
  }

  public Metric negInf(String negInf) {
    this.negInf = negInf;
    return this;
  }

   /**
   * What to do if a value for this metric a positive infinite
   * @return negInf
  **/
  @ApiModelProperty(value = "What to do if a value for this metric a positive infinite")
  public String getNegInf() {
    return negInf;
  }

  public void setNegInf(String negInf) {
    this.negInf = negInf;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Metric metric = (Metric) o;
    return Objects.equals(this.id, metric.id) &&
        Objects.equals(this.name, metric.name) &&
        Objects.equals(this.naN, metric.naN) &&
        Objects.equals(this.posInf, metric.posInf) &&
        Objects.equals(this.negInf, metric.negInf);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, naN, posInf, negInf);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Metric {\n");

    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    naN: ").append(toIndentedString(naN)).append("\n");
    sb.append("    posInf: ").append(toIndentedString(posInf)).append("\n");
    sb.append("    negInf: ").append(toIndentedString(negInf)).append("\n");
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

