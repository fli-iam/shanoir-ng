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

package org.shanoir.uploader.model;

import java.util.Date;

import org.shanoir.ng.importer.model.PseudonymusHashValues;

public class Subject {

    private Long id;

    private Date birthDate;

    private String name;

    private String sex;

    private String imagedObjectCategory;

    private String languageHemisphericDominance;

    private String manualHemisphericDominance;

    private String identifier;

    private PseudonymusHashValues pseudonymusHashValues;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(Date birthDate) {
        this.birthDate = birthDate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public PseudonymusHashValues getPseudonymusHashValues() {
        return pseudonymusHashValues;
    }

    public void setPseudonymusHashValues(PseudonymusHashValues pseudonymusHashValues) {
        this.pseudonymusHashValues = pseudonymusHashValues;
    }

    public String getImagedObjectCategory() {
        return imagedObjectCategory;
    }

    public void setImagedObjectCategory(String imagedObjectCategory) {
        this.imagedObjectCategory = imagedObjectCategory;
    }

    public String getLanguageHemisphericDominance() {
        return languageHemisphericDominance;
    }

    public void setLanguageHemisphericDominance(String languageHemisphericDominance) {
        this.languageHemisphericDominance = languageHemisphericDominance;
    }

    public String getManualHemisphericDominance() {
        return manualHemisphericDominance;
    }

    public void setManualHemisphericDominance(String manualHemisphericDominance) {
        this.manualHemisphericDominance = manualHemisphericDominance;
    }

    @Override
    public String toString() {
        return "Subject [id=" + id + ", birthDate=" + birthDate + ", name = " + name + ", sex=" + sex + ", identifier="
                + identifier + ", pseudonymusHashValues=" + pseudonymusHashValues + "]";
    }

}
