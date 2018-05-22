package org.shanoir.ng.preclinical.extra_data.examination_extra_data;

import java.util.Objects;

import javax.persistence.Entity;
import javax.persistence.PostLoad;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.shanoir.ng.shared.hateoas.HalEntity;
import org.shanoir.ng.shared.hateoas.Links;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.swagger.annotations.ApiModelProperty;
/**
 * Examination Extra Data
 */
@Entity
@Table(name = "examination_extradata")
@JsonPropertyOrder({ "_links", "examination_id", "filename","extradatatype" })
public class ExaminationExtraData extends HalEntity   {
	
  @JsonProperty("examination_id")
  @NotNull
  private Long examinationId;
	
  @JsonProperty("filename")
  //@NotNull
  private String filename;
  
  //@NotNull
  @JsonIgnore
  @JsonProperty("filepath")
  private String filepath;
  
  @JsonProperty("extradatatype")
  @NotNull
  private String extradatatype;
  
  
  /**
	* Init HATEOAS links
  */
  @PostLoad
  public void initLinks() {
	  this.addLink(Links.REL_SELF, "examination/"+ examinationId +"/extradata/" + getId());
  }
  
  public ExaminationExtraData examinationId(Long id) {
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
  
  public ExaminationExtraData filename(String filename) {
    this.filename = filename;
    return this;
  }
  
  @ApiModelProperty(value = "none")
  public String getFilename() {
    return filename;
  }

  public void setFilename(String filename) {
    this.filename = filename;
  }
	   
  @JsonIgnore
  @ApiModelProperty(value = "none")
  public String getFilepath() {
    return filepath;
  }

  public void setFilepath(String filepath) {
    this.filepath = filepath;
  }
  
  public ExaminationExtraData extradatatype(String extradatatype) {
    this.extradatatype = extradatatype;
    return this;
  }
  
  @ApiModelProperty(value = "none")
  public String getExtradatatype() {
    return extradatatype;
  }

  public void setExtradatatype(String extradatatype) {
    this.extradatatype = extradatatype;
  }
    


  @Override
  public int hashCode() {
	return Objects.hash(examinationId, filename,filepath,extradatatype);
  }

  @Override
  public boolean equals(Object obj) {
	if (this == obj)
		return true;
	if (obj == null)
		return false;
	if (getClass() != obj.getClass())
		return false;
	ExaminationExtraData other = (ExaminationExtraData) obj;
	if (examinationId == null) {
		if (other.examinationId != null)
			return false;
	} else if (!examinationId.equals(other.examinationId))
		return false;
	if (filename == null) {
		if (other.filename != null)
			return false;
	} else if (!filename.equals(other.filename))
		return false;
	if (filepath == null) {
		if (other.filepath != null)
			return false;
	} else if (!filepath.equals(other.filepath))
		return false;
	if (extradatatype == null) {
		if (other.extradatatype != null)
			return false;
	} else if (!extradatatype.equals(other.extradatatype))
		return false;
	return true;
  }

@Override	
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ExaminationExtraData {\n");
    
    sb.append("    examination_id: ").append(toIndentedString(examinationId)).append("\n");
    sb.append("    filename: ").append(toIndentedString(filename)).append("\n");
    sb.append("    filepath: ").append(toIndentedString(filepath)).append("\n");
    sb.append("    extradatatype: ").append(toIndentedString(extradatatype)).append("\n");
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

