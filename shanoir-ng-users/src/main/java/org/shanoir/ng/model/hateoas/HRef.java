package org.shanoir.ng.model.hateoas;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class HRef {

	@JsonIgnore
	private String relativePath;

	/**
	 * @param relativePath
	 */
	public HRef(String relativePath) {
		this.relativePath = relativePath;
	}

	@JsonProperty("href")
	public String getHref() {
		return getBaseUrl() + relativePath;
	}

	/**
	 * @return the relativePath
	 */
	public String getRelativePath() {
		return relativePath;
	}

	/**
	 * @param relativePath the relativePath to set
	 */
	public void setRelativePath(String relativePath) {
		this.relativePath = relativePath;
	}

	/**
	 * Get the base url
	 * @return the base url
	 */
	private static String getBaseUrl() {
		return "http://localhost:9900/"; // TODO
	}


}
