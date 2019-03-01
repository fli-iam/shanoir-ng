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

package io.swagger.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import io.swagger.annotations.ApiModelProperty;

/**
 * ChallengerSubjects
 */
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.SpringCodegen", date = "2016-10-11T09:18:12.164Z")

public class ChallengerSubjects {
	private String challengerName = null;

	private List<SubjectScore> subjects = new ArrayList<SubjectScore>();

	public ChallengerSubjects challengerName(String challengerName) {
		this.challengerName = challengerName;
		return this;
	}

	/**
	 * Get challengerName
	 * 
	 * @return challengerName
	 **/
	@ApiModelProperty(required = true, value = "")
	public String getChallengerName() {
		return challengerName;
	}

	public void setChallengerName(String challengerName) {
		this.challengerName = challengerName;
	}

	public ChallengerSubjects subjects(List<SubjectScore> subjects) {
		this.subjects = subjects;
		return this;
	}

	public ChallengerSubjects addSubjectsItem(SubjectScore subjectsItem) {
		this.subjects.add(subjectsItem);
		return this;
	}

	/**
	 * Get subjects
	 * 
	 * @return subjects
	 **/
	@ApiModelProperty(required = true, value = "")
	public List<SubjectScore> getSubjects() {
		return subjects;
	}

	public void setSubjects(List<SubjectScore> subjects) {
		this.subjects = subjects;
	}

	@Override
	public boolean equals(java.lang.Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		ChallengerSubjects challengerSubjects = (ChallengerSubjects) o;
		return Objects.equals(this.challengerName, challengerSubjects.challengerName)
				&& Objects.equals(this.subjects, challengerSubjects.subjects);
	}

	@Override
	public int hashCode() {
		return Objects.hash(challengerName, subjects);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class ChallengerSubjects {\n");

		sb.append("    challengerName: ").append(toIndentedString(challengerName)).append("\n");
		sb.append("    subjects: ").append(toIndentedString(subjects)).append("\n");
		sb.append("}");
		return sb.toString();
	}

	/**
	 * Convert the given object to string with each line indented by 4 spaces
	 * (except the first line).
	 */
	private String toIndentedString(java.lang.Object o) {
		if (o == null) {
			return "null";
		}
		return o.toString().replace("\n", "\n    ");
	}
}
