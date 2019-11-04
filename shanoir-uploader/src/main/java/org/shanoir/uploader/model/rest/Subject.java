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

package org.shanoir.uploader.model.rest;

import java.time.LocalDate;
import java.util.List;

import org.shanoir.uploader.model.PseudonymusHashValues;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

public class Subject {

	private Long id;

	private String name;

	private String identifier;

	@JsonDeserialize(using = LocalDateDeserializer.class)
	@JsonSerialize(using = LocalDateSerializer.class)
	private LocalDate birthDate;

	private HemisphericDominance languageHemisphericDominance;

	private HemisphericDominance manualHemisphericDominance;

	private ImagedObjectCategory imagedObjectCategory;
	
	private Sex sex;

	private List<SubjectStudy> subjectStudyList;
	
	private PseudonymusHashValues pseudonymusHashValues;

	private boolean preclinical;

	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the identifier
	 */
	public String getIdentifier() {
		return identifier;
	}

	/**
	 * @param identifier
	 *            the identifier to set
	 */
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	/**
	 * @return the birthDate
	 */
	public LocalDate getBirthDate() {
		return birthDate;
	}

	/**
	 * @param birthDate
	 *            the birthDate to set
	 */
	public void setBirthDate(LocalDate birthDate) {
		this.birthDate = birthDate;
	}

	/**
	 * @return the languageHemisphericDominance
	 */
	public HemisphericDominance getLanguageHemisphericDominance() {
		return languageHemisphericDominance;
	}

	/**
	 * @param languageHemisphericDominance
	 *            the languageHemisphericDominance to set
	 */
	public void setLanguageHemisphericDominance(HemisphericDominance languageHemisphericDominance) {
		this.languageHemisphericDominance = languageHemisphericDominance;
	}

	/**
	 * @return the manualHemisphericDominance
	 */
	public HemisphericDominance getManualHemisphericDominance() {
		return manualHemisphericDominance;
	}

	/**
	 * @param manualHemisphericDominance
	 *            the manualHemisphericDominance to set
	 */
	public void setManualHemisphericDominance(HemisphericDominance manualHemisphericDominance) {
		this.manualHemisphericDominance = manualHemisphericDominance;
	}

	/**
	 * @return the imagedObjectCategory
	 */
	public ImagedObjectCategory getImagedObjectCategory() {
		return imagedObjectCategory;
	}

	/**
	 * @param imagedObjectCategory
	 *            the imagedObjectCategory to set
	 */
	public void setImagedObjectCategory(ImagedObjectCategory imagedObjectCategory) {
		this.imagedObjectCategory = imagedObjectCategory;
	}

	/**
	 * @return the subjectStudyList
	 */
	public List<SubjectStudy> getSubjectStudyList() {
		return subjectStudyList;
	}

	/**
	 * @param subjectStudyList
	 *            the subjectStudyList to set
	 */
	public void setSubjectStudyList(List<SubjectStudy> subjectStudyList) {
		this.subjectStudyList = subjectStudyList;
	}

	public boolean isPreclinical() {
		return preclinical;
	}

	public void setPreclinical(boolean preclinical) {
		this.preclinical = preclinical;
	}

	public Sex getSex() {
		return sex;
	}

	public void setSex(Sex sex) {
		this.sex = sex;
	}

	public PseudonymusHashValues getPseudonymusHashValues() {
		return pseudonymusHashValues;
	}

	public void setPseudonymusHashValues(PseudonymusHashValues pseudonymusHashValues) {
		this.pseudonymusHashValues = pseudonymusHashValues;
	}

}
