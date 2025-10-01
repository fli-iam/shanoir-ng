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

package org.shanoir.ng.study.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.GenericGenerator;
import org.shanoir.ng.center.model.Center;
import org.shanoir.ng.shared.core.model.AbstractEntity;
import org.shanoir.ng.shared.security.rights.StudyUserRight;
import org.shanoir.ng.study.rights.StudyUserInterface;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "study_user", uniqueConstraints = { @UniqueConstraint(columnNames = { "study_id", "userId" }, name = "study_user_idx") })
@GenericGenerator(name = "IdOrGenerate", strategy = "org.shanoir.ng.shared.model.UseIdOrGenerate")
public class StudyUser extends AbstractEntity implements StudyUserInterface {

	/**
	 * UID
	 */
	private static final long serialVersionUID = 5813071870148636187L;

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
	@ManyToOne
	@JsonIgnore
	private Study study;
		
	/** User id. */
	private Long userId;

	/** Type of the relationship. */
	@ElementCollection(fetch = FetchType.EAGER)
	@Fetch(FetchMode.JOIN)
	@CollectionTable(name = "study_user_study_user_rights", joinColumns = @JoinColumn(name = "study_user_id"))
	private List<Integer> studyUserRights;
	
	/** User name. Duplicate: master record in ms users. */
	@NotBlank
	private String userName;

	@ManyToMany(fetch = FetchType.EAGER)
	@Fetch(FetchMode.JOIN)
	@JoinTable(name = "study_user_center", joinColumns = @JoinColumn(name = "study_user_id"), inverseJoinColumns = @JoinColumn(name = "center_id"))
	private List<Center> centers;

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
	@JsonInclude
	@Transient
	public Long getStudyId() {
		return study.getId();
	}

	public Study getStudy() {
		return study;
	}

	public void setStudy(Study study) {
		this.study = study;
	}

	/**
	 * @return the studyUserRight
	 */
	@Override
	public List<StudyUserRight> getStudyUserRights() {
		List<StudyUserRight> list = new ArrayList<>();
		for (Integer id : studyUserRights) {
			list.add(StudyUserRight.getType(id));
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

	@JsonIgnore
	public List<Center> getCenters() {
		return centers;
	}

	public List<Long> getCenterIds() {
		if (CollectionUtils.isEmpty(this.centers)) {
			return Collections.emptyList();
		}
		return centers.stream().map(center -> center.getId()).collect(Collectors.toList());
	}

	@JsonIgnore
	public void setCenters(List<Center> centers) {
		this.centers = centers;
	}
	
	public void setCenterIds(List<Long> ids) {
		centers = ids.stream().map(id -> {
			Center center = new Center();
			center.setId(id);
			return center;
		}).collect(Collectors.toList());
	}

}
