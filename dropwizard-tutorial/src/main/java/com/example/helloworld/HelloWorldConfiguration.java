package com.example.helloworld;

import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.dropwizard.Configuration;

/**
 * @author msimon
 *
 */
public class HelloWorldConfiguration extends Configuration {

	@NotEmpty
	private String template;
	
	@NotEmpty
	private String defaultName = "Stranger";

	/**
	 * @return the template
	 */
	@JsonProperty
	public String getTemplate() {
		return template;
	}

	/**
	 * @param template the template to set
	 */
	@JsonProperty
	public void setTemplate(String template) {
		this.template = template;
	}

	/**
	 * @return the defaultName
	 */
	@JsonProperty
	public String getDefaultName() {
		return defaultName;
	}

	/**
	 * @param defaultName the defaultName to set
	 */
	@JsonProperty
	public void setDefaultName(String defaultName) {
		this.defaultName = defaultName;
	}
	
}
