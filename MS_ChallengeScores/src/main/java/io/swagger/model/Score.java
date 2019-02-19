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
 * Score
 */
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.SpringCodegen", date = "2016-10-11T09:18:12.164Z")

public class Score {
	private String metric = null;

	private Float value = null;

	public Score metric(String metric) {
		this.metric = metric;
		return this;
	}

	/**
	 * Get metric
	 * 
	 * @return metric
	 **/
	@ApiModelProperty(required = true, value = "")
	public String getMetric() {
		return metric;
	}

	public void setMetric(String metric) {
		this.metric = metric;
	}

	public Score value(Float value) {
		this.value = value;
		return this;
	}

	/**
	 * Get value
	 * 
	 * @return value
	 **/
	@ApiModelProperty(required = true, value = "")
	public Float getValue() {
		return value;
	}

	public void setValue(Float value) {
		this.value = value;
	}

	@Override
	public boolean equals(java.lang.Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		Score score = (Score) o;
		return Objects.equals(this.metric, score.metric) && Objects.equals(this.value, score.value);
	}

	@Override
	public int hashCode() {
		return Objects.hash(metric, value);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class Score {\n");

		sb.append("    metric: ").append(toIndentedString(metric)).append("\n");
		sb.append("    value: ").append(toIndentedString(value)).append("\n");
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
