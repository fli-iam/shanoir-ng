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
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
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

	private Long userId;
    
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

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	@Override
	public String toString() {
		String importType;
		if (fromDicomZip) {
			importType = "ZIP";
		} else if (fromShanoirUploader) {
			importType = "SHUP";
		} else if (fromPacs) {
			importType = "PACS";
		} else {
			importType = "UNSUPPORTED";
		}
		int numberOfSeries = 0;
		StringBuffer seriesNames = new StringBuffer();
		seriesNames.append("[");
		String modality = "unknown";
		boolean enhanced = false;
		if (CollectionUtils.isNotEmpty(patients)) {
			Patient patient = patients.get(0);
			if (CollectionUtils.isNotEmpty(patient.getStudies())) {
				Study study = patient.getStudies().get(0);
				List<Serie> series = study.getSeries();
				if (CollectionUtils.isNotEmpty(series)) {
					numberOfSeries = series.size(); // only selected series remain at the stage of the logging call
					Serie serie = study.getSeries().get(0);
					modality = serie.getModality();
					enhanced = serie.getIsEnhanced();
					for (Iterator iterator = series.iterator(); iterator.hasNext();) {
						serie = (Serie) iterator.next();
						if (iterator.hasNext()) {
							seriesNames.append(serie.getSequenceName() + ",");
						} else {
							seriesNames.append(serie.getSequenceName() + "]");
						}
					}
				}
			}
		}
		return 	"userId=" + userId + ",studyName=" + studyName + ",studyCardId=" + studyCardId + ",type=" + importType +
				",workFolder=" + workFolder + ",pseudoProfile=" + anonymisationProfileToUse + ",modality=" + modality + ",enhanced=" + enhanced +
				",subjectName=" + subjectName + ",examId=" + examinationId  + ",converterId=" + converterId + ",numberOfSeries=" + numberOfSeries +
				",seriesNames=" + seriesNames.toString();
	}
	
}

