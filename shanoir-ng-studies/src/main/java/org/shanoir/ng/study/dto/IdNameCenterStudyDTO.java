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

package org.shanoir.ng.study.dto;

import java.util.ArrayList;
import java.util.List;

import org.shanoir.ng.profile.model.Profile;
import org.shanoir.ng.shared.core.model.IdName;
import org.shanoir.ng.studycenter.StudyCenterDTO;
import org.shanoir.ng.tag.model.TagDTO;

/**
 * Simple DTO for studies.
 * 
 * @author msimon
 *
 */
public class IdNameCenterStudyDTO extends IdName {
	
	private List<StudyCenterDTO> studyCenterList;
	
	private List<TagDTO> tags;

	private Profile profile;

	private Integer studyCardPolicy;

	/**
	 * Simple constructor.
	 */
	public IdNameCenterStudyDTO() {
	}

	/**
	 * Constructor.
	 */
	public IdNameCenterStudyDTO(final Long id, final String name) {
		super(id, name);
		this.setStudyCenterList(new ArrayList<>());
	}

	/**
	 * @return the studyCenterList
	 */
	public List<StudyCenterDTO> getStudyCenterList() {
		return studyCenterList;
	}

	/**
	 * @param studyCenterList the studyCenterList to set
	 */
	public void setStudyCenterList(List<StudyCenterDTO> studyCenterList) {
		this.studyCenterList = studyCenterList;
	}

	public List<TagDTO> getTags() {
		return tags;
	}

	public void setTags(List<TagDTO> tags) {
		this.tags = tags;
	}

	public Profile getProfile() {
		return profile;
	}

	public void setProfile(Profile profile) {
		this.profile = profile;
	}

	public Integer getStudyCardPolicy() {
		return studyCardPolicy;
	}

	public void setStudyCardPolicy(Integer studyCardPolicy) {
		this.studyCardPolicy = studyCardPolicy;
	}

}
