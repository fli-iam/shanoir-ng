package org.shanoir.ng.preclinical.references;

import java.util.Objects;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.PostLoad;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;
import org.shanoir.ng.shared.hateoas.HalEntity;
import org.shanoir.ng.shared.hateoas.Links;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.swagger.annotations.ApiModelProperty;
/**
 * Reference
 */
@Entity
@Table(name = "reference")
@JsonPropertyOrder({ "_links", "category" ,"reftype", "value" })
public class Reference extends HalEntity   {
	

  @JsonProperty("category")
  private String category = "common";
	
  @JsonProperty("reftype")
  private String reftype = null;

  @JsonProperty("value")
  private String value = null;

  
  /**
	 * Init HATEOAS links
	 */
  @PostLoad
	public void initLinks() {
		this.addLink(Links.REL_SELF, "reference/" + getId());
  }

  @Override
  @GeneratedValue(strategy = GenerationType.IDENTITY, generator = "IdOrGenerate")
  @GenericGenerator(name = "IdOrGenerate", strategy = "increment")
  public Long getId() {
	return super.getId();
  }
  
  public Reference category(String category) {
    this.category = category;
    return this;
  }

  @ApiModelProperty(value = "none")
  public String getCategory() {
    return category;
  }

  public void setCategory(String category) {
    this.category = category;
  }
 
  public Reference reftype(String reftype) {
    this.reftype = reftype;
    return this;
  }
  
    /**
   * none
   * @return type
  **/
  @ApiModelProperty(value = "none")
  public String getReftype() {
    return reftype;
  }

  public void setReftype(String reftype) {
    this.reftype = reftype;
  }

  public Reference value(String value) {
    this.value = value;
    return this;
  }

   /**
   * none
   * @return value
  **/
  @ApiModelProperty(value = "none")
  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Reference reference = (Reference) o;
    return Objects.equals(this.reftype, reference.reftype) &&
        Objects.equals(this.value, reference.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(reftype, value);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Reference {\n");
    
    sb.append("    category: ").append(toIndentedString(category)).append("\n");
    sb.append("    reftype: ").append(toIndentedString(reftype)).append("\n");
    sb.append("    value: ").append(toIndentedString(value)).append("\n");
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

