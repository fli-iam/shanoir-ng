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

package org.shanoir.ng.study.rights;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.validator.constraints.NotBlank;
import org.shanoir.ng.shared.security.rights.StudyUserRight;

@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "studyId", "userId" }, name = "study_user_idx") })
public class StudyUser implements StudyUserInterface {

	/**
	 * UID
	 */
	private static final long serialVersionUID = 5813071870148636187L;
	
	@Id
	private Long id;

	/** is the anonymization report to be sent to the user. */
	private boolean receiveAnonymizationReport;

	/** Advice the user when new import done in the study. */
	private boolean receiveNewImportReport;

	/** Study id. */
	private Long studyId;
	
	/** User id. */
	private Long userId;

	/** Type of the relationship. */
	@ElementCollection
	private List<Integer> studyUserRights;
	
	/** User name. Duplicate: master record in ms users. */
	@NotBlank
	private String userName;

	/**
	 * @return the receiveAnonymizationReport
	 */
	public boolean isReceiveAnonymizationReport() {
		return receiveAnonymizationReport;
	}

	/**
	 * @param receiveAnonymizationReport
	 *            the receiveAnonymizationReport to set
	 */
	public void setReceiveAnonymizationReport(boolean receiveAnonymizationReport) {
		this.receiveAnonymizationReport = receiveAnonymizationReport;
	}

	/**
	 * @return the receiveNewImportReport
	 */
	public boolean isReceiveNewImportReport() {
		return receiveNewImportReport;
	}

	/**
	 * @param receiveNewImportReport
	 *            the receiveNewImportReport to set
	 */
	public void setReceiveNewImportReport(boolean receiveNewImportReport) {
		this.receiveNewImportReport = receiveNewImportReport;
	}

	/**
	 * @return the studyId
	 */
	public Long getStudyId() {
		return studyId;
	}

	/**
	 * @param studyId
	 *            the studyId to set
	 */
	public void setStudyId(Long studyId) {
		this.studyId = studyId;
	}

	/**
	 * @return the studyUserRight
	 */
	public List<StudyUserRight> getStudyUserRights() {
		List<StudyUserRight> list = new ArrayList<>();
		for (Integer id : studyUserRights) list.add(StudyUserRight.getType(id));
		return list;
	}

	/**
	 * @param studyUserRight the studyUserRight to set
	 */
	public void setStudyUserRights(List<StudyUserRight> studyUserRights) {
		this.studyUserRights = new ArrayList<>();
		if (studyUserRights != null) {
			for (StudyUserRight sur : studyUserRights)  {
				this.studyUserRights.add(sur.getId());
			}
		}
	}

	/**
	 * @return the userId
	 */
	public Long getUserId() {
		return userId;
	}

	/**
	 * @param userId
	 *            the userId to set
	 */
	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;		
	}

}
