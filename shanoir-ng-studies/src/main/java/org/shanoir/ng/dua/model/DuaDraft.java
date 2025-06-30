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

package org.shanoir.ng.dua.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class DuaDraft {

    @Id
    private String id;

    private Long studyId;

    private String studyName;

    String url;
    
    String funding;

    String thanks;
    
    String papers;

    String logoUrl;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getStudyId() {
        return studyId;
    }

    public void setStudyId(Long studyId) {
        this.studyId = studyId;
    }

    public String getStudyName() {
        return studyName;
    }

    public void setStudyName(String studyName) {
        this.studyName = studyName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getFunding() {
        return funding;
    }

    public void setFunding(String funding) {
        this.funding = funding;
    }

    public String getThanks() {
        return thanks;
    }

    public void setThanks(String thanks) {
        this.thanks = thanks;
    }

    public String getPapers() {
        return papers;
    }

    public void setPapers(String papers) {
        this.papers = papers;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }
}