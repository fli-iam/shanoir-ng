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
package org.shanoir.ng.preclinical.extra_data.examination_extra_data;

import java.util.Objects;

import org.shanoir.ng.shared.hateoas.HalEntity;
import org.shanoir.ng.shared.hateoas.Links;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Entity;
import jakarta.persistence.PostLoad;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

/**
 * Examination Extra Data
 */
@Entity
@Table(name = "examination_extradata")
@JsonPropertyOrder({"_links", "examination_id", "filename", "extraDataType"})
public class ExaminationExtraData extends HalEntity {

    @NotNull
    private Long examinationId;

    @JsonProperty("filename")
    //@NotNull
    private String filename;

    //@NotNull
    @JsonIgnore
    @JsonProperty("filepath")
    private String filepath;

    @JsonProperty("extraDataType")
    @NotNull
    private String extraDataType;

    /**
     * Init HATEOAS links
     */
    @PostLoad
    public void initLinks() {
        this.addLink(Links.REL_SELF, "examination/" + examinationId + "/extradata/" + getId());
    }

    public ExaminationExtraData examinationId(Long id) {
        this.examinationId = id;
        return this;
    }

    @Schema(name = "none")
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

    @Schema(name = "none")
    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    @JsonIgnore
    @Schema(name = "none")
    public String getFilepath() {
        return filepath;
    }

    public void setFilepath(String filepath) {
        this.filepath = filepath;
    }

    public ExaminationExtraData extraDataType(String extraDataType) {
        this.extraDataType = extraDataType;
        return this;
    }

    @Schema(name = "none")
    public String getExtraDataType() {
        return extraDataType;
    }

    public void setExtraDataType(String extraDataType) {
        this.extraDataType = extraDataType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(examinationId, filename, filepath, extraDataType);
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
        ExaminationExtraData other = (ExaminationExtraData) obj;
        if (examinationId == null) {
            if (other.examinationId != null) {
                return false;
            }
        } else if (!examinationId.equals(other.examinationId)) {
            return false;
        }
        if (filename == null) {
            if (other.filename != null) {
                return false;
            }
        } else if (!filename.equals(other.filename)) {
            return false;
        }
        if (filepath == null) {
            if (other.filepath != null) {
                return false;
            }
        } else if (!filepath.equals(other.filepath)) {
            return false;
        }
        if (extraDataType == null) {
            if (other.extraDataType != null) {
                return false;
            }
        } else if (!extraDataType.equals(other.extraDataType)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ExaminationExtraData {\n");
        sb.append("    id: ").append(toIndentedString(getId())).append("\n");
        sb.append("    examination_id: ").append(toIndentedString(examinationId)).append("\n");
        sb.append("    filename: ").append(toIndentedString(filename)).append("\n");
        sb.append("    filepath: ").append(toIndentedString(filepath)).append("\n");
        sb.append("    extraDataType: ").append(toIndentedString(extraDataType)).append("\n");
        sb.append("}\n");
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
