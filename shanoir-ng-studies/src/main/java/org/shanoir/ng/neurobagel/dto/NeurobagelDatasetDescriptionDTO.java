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

package org.shanoir.ng.neurobagel.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Neurobagel dataset description of a study (input of 'bagel pheno --dataset-description').
 * Not the BIDS dataset_description.json: Neurobagel has its own schema, with PascalCase keys.
 * See https://neurobagel.org/user_guide/dataset_description/
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "Name", "Keywords", "ReferencesAndLinks", "AccessType", "AccessInstructions", "AccessLink" })
public class NeurobagelDatasetDescriptionDTO {

    /** Access requires a Shanoir account and the study's approval. */
    public static final String ACCESS_TYPE_RESTRICTED = "restricted";

    @JsonProperty("Name")
    private String name;

    @JsonProperty("Keywords")
    private List<String> keywords;

    @JsonProperty("ReferencesAndLinks")
    private List<String> referencesAndLinks;

    @JsonProperty("AccessType")
    private String accessType;

    @JsonProperty("AccessInstructions")
    private String accessInstructions;

    @JsonProperty("AccessLink")
    private String accessLink;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }

    public List<String> getReferencesAndLinks() {
        return referencesAndLinks;
    }

    public void setReferencesAndLinks(List<String> referencesAndLinks) {
        this.referencesAndLinks = referencesAndLinks;
    }

    public String getAccessType() {
        return accessType;
    }

    public void setAccessType(String accessType) {
        this.accessType = accessType;
    }

    public String getAccessInstructions() {
        return accessInstructions;
    }

    public void setAccessInstructions(String accessInstructions) {
        this.accessInstructions = accessInstructions;
    }

    public String getAccessLink() {
        return accessLink;
    }

    public void setAccessLink(String accessLink) {
        this.accessLink = accessLink;
    }

}
