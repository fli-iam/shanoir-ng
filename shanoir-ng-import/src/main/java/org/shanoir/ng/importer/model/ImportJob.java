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

package org.shanoir.ng.importer.model;

import java.io.Serializable;
import java.util.List;

import org.shanoir.ng.shared.event.ShanoirEvent;

/**
 * @author atouboul
 * @author mkain
 */
public class ImportJob implements Serializable {

	private static final long serialVersionUID = 8804929608059674037L;

    private boolean fromDicomZip;

    private boolean fromShanoirUploader;

    private boolean fromPacs;
    
	private String workFolder;

    private List<Patient> patients;
    
    private Long examinationId;
    
    private Long studyCardId;
    
    private Long converterId;
    
    private Long studyId;
    
	private String studyCardName;
	
	// todo: remove this later, when front end uses StudyCards
    private Long acquisitionEquipmentId;
	
	private String anonymisationProfileToUse;

    private String archive;

	private String subjectName;

	private String studyName;

	private ShanoirEvent shanoirEvent;
    
	public String getArchive() {
		return archive;
	}

	public void setArchive(final String archive) {
		this.archive = archive;
	}

	public boolean isFromDicomZip() {
		return fromDicomZip;
	}

	public void setFromDicomZip(final boolean fromDicomZip) {
		this.fromDicomZip = fromDicomZip;
	}

	public boolean isFromShanoirUploader() {
		return fromShanoirUploader;
	}

	public void setFromShanoirUploader(final boolean fromShanoirUploader) {
		this.fromShanoirUploader = fromShanoirUploader;
	}

	public boolean isFromPacs() {
		return fromPacs;
	}

	public void setFromPacs(final boolean fromPacs) {
		this.fromPacs = fromPacs;
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

	public void setStudyId(final Long studyId) {
		this.studyId = studyId;
	}

	public String getStudyCardName() {
		return studyCardName;
	}

	public void setStudyCardName(String studyCardName) {
		this.studyCardName = studyCardName;
	}

	public Long getAcquisitionEquipmentId() {
		return acquisitionEquipmentId;
	}

	public void setAcquisitionEquipmentId(final Long acquisitionEquipmentId) {
		this.acquisitionEquipmentId = acquisitionEquipmentId;

	}

	public Long getStudyCardId() {
		return studyCardId;
	}

	public void setStudyCardId(Long studyCardId) {
		this.studyCardId = studyCardId;
	}

	public Long getConverterId() {
		return converterId;
	}

	public void setConverterId(Long converterId) {
		this.converterId = converterId;
	}

    public String getAnonymisationProfileToUse() {
		return anonymisationProfileToUse;
	}

	public void setAnonymisationProfileToUse(String anonymisationProfileToUse) {
		this.anonymisationProfileToUse = anonymisationProfileToUse;
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

	public ShanoirEvent getShanoirEvent() {
		return shanoirEvent;
	}

	public void setShanoirEvent(ShanoirEvent shanoirEvent) {
		this.shanoirEvent = shanoirEvent;
	}
}

