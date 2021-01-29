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

import org.shanoir.ng.processing.model.DatasetProcessing;
import org.shanoir.ng.shared.event.ShanoirEvent;

/**
 * @author amasson
 *
 */
public class ProcessedDatasetImportJob implements Serializable {

	private static final long serialVersionUID = 8804929608059674037L;

	private String workFolder;

	private List<Patient> patients;

	private Long examinationId;

	private Long studyId;

	private String archive;

	private String subjectName;

	private String studyName;

	private String datasetType;

	private DatasetProcessing datasetProcessing;

	private ShanoirEvent shanoirEvent;

	public String getArchive() {
		return archive;
	}

	public void setArchive(final String archive) {
		this.archive = archive;
	}

	public List<Patient> getPatients() {
		return patients;
	}

	public void setPatients(final List<Patient> patients) {
		this.patients = patients;
	}

	public Long getExaminationId() {
		return examinationId;
	}

	public void setExaminationId(final Long examinationId) {
		this.examinationId = examinationId;
	}

	public String getWorkFolder() {
		return workFolder;
	}

	public void setWorkFolder(String workFolder) {
		this.workFolder = workFolder;
	}

	public Long getStudyId() {
		return studyId;
	}

	public void setStudyId(final Long StudyId) {
		this.studyId = StudyId;
	}

	public String getSubjectName() {
		return subjectName;
	}

	public void setSubjectName(String subjectName) {
		this.subjectName = subjectName;
	}

	public String getStudyName() {
		return studyName;
	}

	public void setStudyName(String studyName) {
		this.studyName = studyName;
	}

	public DatasetProcessing getDatasetProcessing() {
		return this.datasetProcessing;
	}

	public void setDatasetProcessing(DatasetProcessing datasetProcessing) {
		this.datasetProcessing = datasetProcessing;
	}

	public String getDatasetType() {
		return this.datasetType;
	}

	public void setDatasetType(String datasetType) {
		this.datasetType = datasetType;
	}

	public ShanoirEvent getShanoirEvent() {
		return shanoirEvent;
	}

	public void setShanoirEvent(ShanoirEvent shanoirEvent) {
		this.shanoirEvent = shanoirEvent;
	}

}
