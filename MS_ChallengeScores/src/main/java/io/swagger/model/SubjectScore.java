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

import java.util.Objects;

import io.swagger.annotations.ApiModelProperty;

/**
 * SubjectScore
 */
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.SpringCodegen", date = "2016-10-11T09:18:12.164Z")

public class SubjectScore {
	private String subjectName = null;

	private Float score = null;

	public SubjectScore subjectName(String subjectName) {
		this.subjectName = subjectName;
		return this;
	}

	/**
	 * Get subjectName
	 * 
	 * @return subjectName
	 **/
	@ApiModelProperty(required = true, value = "")
	public String getSubjectName() {
		return subjectName;
	}

	public void setSubjectName(String subjectName) {
		this.subjectName = subjectName;
	}

	public SubjectScore score(Float score) {
		this.score = score;
		return this;
	}

	/**
	 * Get score
	 * 
	 * @return score
	 **/
	@ApiModelProperty(required = true, value = "")
	public Float getScore() {
		return score;
	}

	public void setScore(Float score) {
		this.score = score;
	}

	@Override
	public boolean equals(java.lang.Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		SubjectScore subjectScore = (SubjectScore) o;
		return Objects.equals(this.subjectName, subjectScore.subjectName)
				&& Objects.equals(this.score, subjectScore.score);
	}

	@Override
	public int hashCode() {
		return Objects.hash(subjectName, score);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class SubjectScore {\n");

		sb.append("    subjectName: ").append(toIndentedString(subjectName)).append("\n");
		sb.append("    score: ").append(toIndentedString(score)).append("\n");
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
