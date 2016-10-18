package io.swagger.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import io.swagger.annotations.ApiModelProperty;

/**
 * MetricChallengers
 */
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.SpringCodegen", date = "2016-10-11T09:18:12.164Z")

public class MetricChallengers {
	private String metricName = null;

	private List<ChallengerSubjects> challengers = new ArrayList<ChallengerSubjects>();

	public MetricChallengers metricName(String metricName) {
		this.metricName = metricName;
		return this;
	}

	/**
	 * Get metricName
	 * 
	 * @return metricName
	 **/
	@ApiModelProperty(required = true, value = "")
	public String getMetricName() {
		return metricName;
	}

	public void setMetricName(String metricName) {
		this.metricName = metricName;
	}

	public MetricChallengers challengers(List<ChallengerSubjects> challengers) {
		this.challengers = challengers;
		return this;
	}

	public MetricChallengers addChallengersItem(ChallengerSubjects challengersItem) {
		this.challengers.add(challengersItem);
		return this;
	}

	/**
	 * Get challengers
	 * 
	 * @return challengers
	 **/
	@ApiModelProperty(required = true, value = "")
	public List<ChallengerSubjects> getChallengers() {
		return challengers;
	}

	public void setChallengers(List<ChallengerSubjects> challengers) {
		this.challengers = challengers;
	}

	@Override
	public boolean equals(java.lang.Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		MetricChallengers metricChallengers = (MetricChallengers) o;
		return Objects.equals(this.metricName, metricChallengers.metricName)
				&& Objects.equals(this.challengers, metricChallengers.challengers);
	}

	@Override
	public int hashCode() {
		return Objects.hash(metricName, challengers);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class MetricChallengers {\n");

		sb.append("    metricName: ").append(toIndentedString(metricName)).append("\n");
		sb.append("    challengers: ").append(toIndentedString(challengers)).append("\n");
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
