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

package org.shanoir.ng.subject.dto;

import java.time.LocalDate;

import org.shanoir.ng.subject.model.PseudonymusHashValues;

public class SubjectFromShupDTO {

    private Long id;
    
    private String name;
    
    private String identifier;

    private LocalDate birthDate;

    private Integer languageHemisphericDominance;

    private Integer manualHemisphericDominance;

    private Integer imagedObjectCategory;

    private Integer sex;
    
    private Long studyId;
    
    private Long studyCardId;
    
    private Integer subjectType;
    
    private Boolean physicallyInvolved;
    
    private String subjectStudyIdentifier;
    
    private PseudonymusHashValues pseudonymusHashValues;
    
    private boolean preclinical;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public LocalDate getBirthDate() {
		return birthDate;
	}

	public void setBirthDate(LocalDate birthDate) {
		this.birthDate = birthDate;
	}

	public Integer getLanguageHemisphericDominance() {
		return languageHemisphericDominance;
	}

	public void setLanguageHemisphericDominance(Integer languageHemisphericDominance) {
		this.languageHemisphericDominance = languageHemisphericDominance;
	}

	public Integer getManualHemisphericDominance() {
		return manualHemisphericDominance;
	}

	public void setManualHemisphericDominance(Integer manualHemisphericDominance) {
		this.manualHemisphericDominance = manualHemisphericDominance;
	}

	public Integer getImagedObjectCategory() {
		return imagedObjectCategory;
	}

	public void setImagedObjectCategory(Integer imagedObjectCategory) {
		this.imagedObjectCategory = imagedObjectCategory;
	}

	public Integer getSex() {
		return sex;
	}

	public void setSex(Integer sex) {
		this.sex = sex;
	}

	public Long getStudyId() {
		return studyId;
	}

	public void setStudyId(Long studyId) {
		this.studyId = studyId;
	}

	public Long getStudyCardId() {
		return studyCardId;
	}

	public void setStudyCardId(Long studyCardId) {
		this.studyCardId = studyCardId;
	}

	public Integer getSubjectType() {
		return subjectType;
	}

	public void setSubjectType(Integer subjectType) {
		this.subjectType = subjectType;
	}

	public Boolean getPhysicallyInvolved() {
		return physicallyInvolved;
	}

	public void setPhysicallyInvolved(Boolean physicallyInvolved) {
		this.physicallyInvolved = physicallyInvolved;
	}

	public String getSubjectStudyIdentifier() {
		return subjectStudyIdentifier;
	}

	public void setSubjectStudyIdentifier(String subjectStudyIdentifier) {
		this.subjectStudyIdentifier = subjectStudyIdentifier;
	}

	public boolean isPreclinical() {
		return preclinical;
	}

	public void setPreclinical(boolean preclinical) {
		this.preclinical = preclinical;
	}

	public PseudonymusHashValues getPseudonymusHashValues() {
		return pseudonymusHashValues;
	}

	public void setPseudonymusHashValues(PseudonymusHashValues pseudonymusHashValues) {
		this.pseudonymusHashValues = pseudonymusHashValues;
	}
	
}
