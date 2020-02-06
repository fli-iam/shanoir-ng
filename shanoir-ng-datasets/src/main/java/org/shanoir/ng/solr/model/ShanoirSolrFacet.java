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

package org.shanoir.ng.solr.model;

import java.time.LocalDate;
import java.util.Collection;

/**
 * @author yyao
 *
 */
public class ShanoirSolrFacet {
	
	private Collection<String> studyName;
	
	private Collection<String> subjectName;
	
	private Collection<String> examinationComment;
	
	private	Collection<String> datasetName;
	
	private Collection<Long> studyId; 
	
	private LocalDate datasetStartDate;
	
	private LocalDate datasetEndDate;
	
	/**
	 * @return the studyName
	 */
	public Collection<String> getStudyName() {
		return studyName;
	}

	/**
	 * @param studyName the studyName to set
	 */
	public void setStudyName(Collection<String> studyName) {
		this.studyName = studyName;
	}

	/**
	 * @return the subjectName
	 */
	public Collection<String> getSubjectName() {
		return subjectName;
	}

	/**
	 * @param subjectName the subjectName to set
	 */
	public void setSubjectName(Collection<String> subjectName) {
		this.subjectName = subjectName;
	}

	/**
	 * @return the examinationComment
	 */
	public Collection<String> getExaminationComment() {
		return examinationComment;
	}

	/**
	 * @param examinationComment the examinationComment to set
	 */
	public void setExaminationComment(Collection<String> examinationComment) {
		this.examinationComment = examinationComment;
	}

	/**
	 * @return the datasetName
	 */
	public Collection<String> getDatasetName() {
		return datasetName;
	}

	/**
	 * @param datasetName the datasetName to set
	 */
	public void setDatasetName(Collection<String> datasetName) {
		this.datasetName = datasetName;
	}

	/**
	 * @return the studyId
	 */
	public Collection<Long> getStudyId() {
		return studyId;
	}

	/**
	 * @param studyId the studyId to set
	 */
	public void setStudyId(Collection<Long> studyId) {
		this.studyId = studyId;
	}

	/**
	 * @return the datasetStartDate
	 */
	public LocalDate getDatasetStartDate() {
		return datasetStartDate;
	}

	/**
	 * @param datasetStartDate the datasetStartDate to set
	 */
	public void setDatasetStartDate(LocalDate datasetStartDate) {
		this.datasetStartDate = datasetStartDate;
	}

	/**
	 * @return the datasetEndDate
	 */
	public LocalDate getDatasetEndDate() {
		return datasetEndDate;
	}

	/**
	 * @param datasetEndDate the datasetEndDate to set
	 */
	public void setDatasetEndDate(LocalDate datasetEndDate) {
		this.datasetEndDate = datasetEndDate;
	}

//	private	Long datasetId;
//	
//	// DatasetModalityType: MR, CT, PET etc..
//	private	String datasetType;
//	
//	// T1, T2, Diff, etc..
//	private String datasetNature;
//	
//	private Date datasetCreationDate;
//	
//	
//	private Date examinationDate;
//	
//	
//	
//	private Long studyId;

}
