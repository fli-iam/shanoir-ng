package org.shanoir.ng.dataset.model.carmin;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;
import java.util.Objects;

/**
 * Path
 */
@Validated
public class Path implements OneOfGetPathResponse {
  @JsonProperty("platformPath")
  private String platformPath = null;

  @JsonProperty("lastModificationDate")
  private Long lastModificationDate = null;

  @JsonProperty("isDirectory")
  private Boolean isDirectory = null;

  @JsonProperty("size")
  private Long size = null;

  @JsonProperty("executionId")
  private String executionId = null;

  @JsonProperty("mimeType")
  private String mimeType = null;

  public Path platformPath(String platformPath) {
    this.platformPath = platformPath;
    return this;
  }

  /**
   * A valid path, slash-separated. It must be consistent with the path of files and directories uploaded and downloaded by clients. For instance, if a user uploads a directory structure \"dir/{file1.txt,file2.txt}\", it is expected that the path of the first file will be \"[prefix]/dir/file1.txt\" and that the path of the second file will be \"[prefix]/dir/file2.txt\" where [prefix] depends on the upload parameters, in particular destination directory.
   * @return platformPath
   **/
  @NotNull
  public String getPlatformPath() {
    return platformPath;
  }

  public void setPlatformPath(String platformPath) {
    this.platformPath = platformPath;
  }

  public Path lastModificationDate(Long lastModificationDate) {
    this.lastModificationDate = lastModificationDate;
    return this;
  }

  /**
   * Date of last modification, in seconds since the Epoch (UNIX timestamp).
   * @return lastModificationDate
   **/
  @NotNull
  public Long getLastModificationDate() {
    return lastModificationDate;
  }

  public void setLastModificationDate(Long lastModificationDate) {
    this.lastModificationDate = lastModificationDate;
  }

  public Path isDirectory(Boolean isDirectory) {
    this.isDirectory = isDirectory;
    return this;
  }

  /**
   * True if the path represents a directory.
   * @return isDirectory
   **/
  @NotNull
  public Boolean isIsDirectory() {
    return isDirectory;
  }

  public void setIsDirectory(Boolean isDirectory) {
    this.isDirectory = isDirectory;
  }

  public Path size(Long size) {
    this.size = size;
    return this;
  }

  /**
   * For a file, size in bytes. For a directory, sum of all the sizes of the files contained in the directory (recursively).
   * @return size
   **/
  public Long getSize() {
    return size;
  }

  public void setSize(Long size) {
    this.size = size;
  }

  public Path executionId(String executionId) {
    this.executionId = executionId;
    return this;
  }

  /**
   * Id of the Execution that produced the Path.
   * @return executionId
   **/
  public String getExecutionId() {
    return executionId;
  }

  public void setExecutionId(String executionId) {
    this.executionId = executionId;
  }

  public Path mimeType(String mimeType) {
    this.mimeType = mimeType;
    return this;
  }

  /**
   * mime type based on RFC 6838.
   * @return mimeType
   **/
  public String getMimeType() {
    return mimeType;
  }

  public void setMimeType(String mimeType) {
    this.mimeType = mimeType;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Path path = (Path) o;
    return Objects.equals(this.platformPath, path.platformPath) &&
        Objects.equals(this.lastModificationDate, path.lastModificationDate) &&
        Objects.equals(this.isDirectory, path.isDirectory) &&
        Objects.equals(this.size, path.size) &&
        Objects.equals(this.executionId, path.executionId) &&
        Objects.equals(this.mimeType, path.mimeType);
  }

  @Override
  public int hashCode() {
    return Objects.hash(platformPath, lastModificationDate, isDirectory, size, executionId, mimeType);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Path {\n");
    
    sb.append("    platformPath: ").append(toIndentedString(platformPath)).append("\n");
    sb.append("    lastModificationDate: ").append(toIndentedString(lastModificationDate)).append("\n");
    sb.append("    isDirectory: ").append(toIndentedString(isDirectory)).append("\n");
    sb.append("    size: ").append(toIndentedString(size)).append("\n");
    sb.append("    executionId: ").append(toIndentedString(executionId)).append("\n");
    sb.append("    mimeType: ").append(toIndentedString(mimeType)).append("\n");
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
