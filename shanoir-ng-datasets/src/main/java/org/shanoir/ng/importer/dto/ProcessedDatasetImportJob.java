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

package org.shanoir.ng.importer.dto;

import java.io.Serializable;
import java.util.List;

import org.shanoir.ng.dataset.modality.ProcessedDatasetType;
import org.shanoir.ng.processing.model.DatasetProcessing;
import org.shanoir.ng.shared.event.ShanoirEvent;

/**
 * @author amasson
 *
 */
public class ProcessedDatasetImportJob implements Serializable {

	private static final long serialVersionUID = 8804929608059674037L;

	private Long studyId;

	private String studyName;

	private Long subjectId;

	private String subjectName;

	private String datasetType;

	private String processedDatasetFilePath;

	private ProcessedDatasetType processedDatasetType;

	private String processedDatasetName;

	private String processedDatasetComment;

	private DatasetProcessing datasetProcessing;

	private ShanoirEvent shanoirEvent;
	
	public String getProcessedDatasetFilePath() {
		return processedDatasetFilePath;
	}

	public void setProcessedDatasetFilePath(String processedDatasetFilePath) {
		this.processedDatasetFilePath = processedDatasetFilePath;
	}

	public Long getStudyId() {
		return studyId;
	}

	public void setStudyId(final Long studyId) {
		this.studyId = studyId;
	}

	public String getStudyName() {
		return studyName;
	}

	public void setStudyName(String studyName) {
		this.studyName = studyName;
	}

	public Long getSubjectId() {
		return subjectId;
	}

	public void setSubjectId(final Long subjectId) {
		this.subjectId = subjectId;
	}

	public String getSubjectName() {
		return subjectName;
	}

	public void setSubjectName(String subjectName) {
		this.subjectName = subjectName;
	}

	public String getDatasetType() {
		return this.datasetType;
	}

	public void setDatasetType(String datasetType) {
		this.datasetType = datasetType;
	}

	public ProcessedDatasetType getProcessedDatasetType() {
		return this.processedDatasetType;
	}
	
	public void setProcessedDatasetType(ProcessedDatasetType processedDatasetType) {
		this.processedDatasetType = processedDatasetType;
	}

	public String getProcessedDatasetName() {
		return this.processedDatasetName;
	}

	public void setProcessedDatasetName(String processedDatasetName) {
		this.processedDatasetName = processedDatasetName;
	}

	public String getProcessedDatasetComment() {
		return this.processedDatasetComment;
	}

	public void setProcessedDatasetComment(String processedDatasetComment) {
		this.processedDatasetComment = processedDatasetComment;
	}

	public DatasetProcessing getDatasetProcessing() {
		return this.datasetProcessing;
	}

	public void setDatasetProcessing(DatasetProcessing datasetProcessing) {
		this.datasetProcessing = datasetProcessing;
	}

	public ShanoirEvent getShanoirEvent() {
		return shanoirEvent;
	}

	public void setShanoirEvent(ShanoirEvent shanoirEvent) {
		this.shanoirEvent = shanoirEvent;
	}

}
