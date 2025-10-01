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

package org.shanoir.ng.dicom.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * This class represents a DICOM-INSTANCE used for the DICOMweb protocol.
 *
 * See for the standard:
 * https://dicom.nema.org/medical/dicom/current/output/html/part18.html#sect_10.4
 *
 * Goal is to support OHIF viewer, v2.x
 *
 * @author mkain
 *
 */
public class InstanceDTO {

    /**
     * INSTANCE: 2 attributes
     */
    @JsonProperty("metadata")
    private MetadataDTO metadata;

    @JsonProperty("url")
    private String url;

    public MetadataDTO getMetadata() {
        return metadata;
    }

    public void setMetadata(MetadataDTO metadata) {
        this.metadata = metadata;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

}
