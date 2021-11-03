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
	
	@Id
	private Long id;

	/** Inform an user about changes on study_user: creation */
	private boolean receiveStudyUserReport;

	/** Advice the user when new import done in the study. */
	private boolean receiveNewImportReport;
	
	/**
	 * With the introduction of a Data User Agreement form, a study
	 * responsible can add an user to a study, but his StudyUser is
	 * not confirmed as long, as the user has not validated the DUA.
	 * The default is true, in case no DUA is existing.
	 */
	private boolean confirmed = true;

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
	 * @return the receiveStudyUserReport
	 */
	@Override
	public boolean isReceiveStudyUserReport() {
		return receiveStudyUserReport;
	}

	/**
	 * @param receiveStudyUserReport
	 *            the receiveStudyUserReport to set
	 */
	@Override
	public void setReceiveStudyUserReport(boolean receiveStudyUserReport) {
		this.receiveStudyUserReport = receiveStudyUserReport;
	}

	/**
	 * @return the receiveNewImportReport
	 */
	@Override
	public boolean isReceiveNewImportReport() {
		return receiveNewImportReport;
	}

	/**
	 * @param receiveNewImportReport
	 *            the receiveNewImportReport to set
	 */
	@Override
	public void setReceiveNewImportReport(boolean receiveNewImportReport) {
		this.receiveNewImportReport = receiveNewImportReport;
	}

	public boolean isConfirmed() {
		return confirmed;
	}

	public void setConfirmed(boolean confirmed) {
		this.confirmed = confirmed;
	}

	/**
	 * @return the studyId
	 */
	@Override
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
	@Override
	public List<StudyUserRight> getStudyUserRights() {
		List<StudyUserRight> list = new ArrayList<>();
		for (Integer localId : studyUserRights) {
			list.add(StudyUserRight.getType(localId));
		}
		return list;
	}

	/**
	 * @param studyUserRight the studyUserRight to set
	 */
	@Override
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
	@Override
	public Long getUserId() {
		return userId;
	}

	/**
	 * @param userId
	 *            the userId to set
	 */
	@Override
	public void setUserId(Long userId) {
		this.userId = userId;
	}

	@Override
	public String getUserName() {
		return userName;
	}

	@Override
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
