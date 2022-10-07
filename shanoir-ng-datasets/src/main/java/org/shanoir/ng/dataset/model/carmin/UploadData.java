package org.shanoir.ng.dataset.model.carmin;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;
import java.util.Objects;

/**
 * UploadData
 */
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2021-10-28T14:56:55.987Z[GMT]")


public class UploadData {
  @JsonProperty("base64Content")
  private String base64Content = null;

  /**
   * Gets or Sets type
   */
  public enum TypeEnum {
    FILE("File"),
    
    ARCHIVE("Archive");

    private String value;

    TypeEnum(String value) {
      this.value = value;
    }

    @Override
    @JsonValue
    public String toString() {
      return String.valueOf(value);
    }

    @JsonCreator
    public static TypeEnum fromValue(String text) {
      for (TypeEnum b : TypeEnum.values()) {
        if (String.valueOf(b.value).equals(text)) {
          return b;
        }
      }
      return null;
    }
  }
  @JsonProperty("type")
  private TypeEnum type = null;

  @JsonProperty("md5")
  private String md5 = null;

  public UploadData base64Content(String base64Content) {
    this.base64Content = base64Content;
    return this;
  }

  /**
   * If the type is \"File\", the base64 string will be decoded to a single raw file. If the type is \"Archive\", the base64 string must corresponds to an encoded zip file that will be decoded to create directory and its content.
   * @return base64Content
   **/
  @NotNull
  public String getBase64Content() {
    return base64Content;
  }

  public void setBase64Content(String base64Content) {
    this.base64Content = base64Content;
  }

  public UploadData type(TypeEnum type) {
    this.type = type;
    return this;
  }

  /**
   * Get type
   * @return type
   **/
  @NotNull
  public TypeEnum getType() {
    return type;
  }

  public void setType(TypeEnum type) {
    this.type = type;
  }

  public UploadData md5(String md5) {
    this.md5 = md5;
    return this;
  }

  /**
   * Get md5
   * @return md5
   **/
  public String getMd5() {
    return md5;
  }

  public void setMd5(String md5) {
    this.md5 = md5;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    UploadData uploadData = (UploadData) o;
    return Objects.equals(this.base64Content, uploadData.base64Content) &&
        Objects.equals(this.type, uploadData.type) &&
        Objects.equals(this.md5, uploadData.md5);
  }

  @Override
  public int hashCode() {
    return Objects.hash(base64Content, type, md5);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class UploadData {\n");
    
    sb.append("    base64Content: ").append(toIndentedString(base64Content)).append("\n");
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
    sb.append("    md5: ").append(toIndentedString(md5)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}
