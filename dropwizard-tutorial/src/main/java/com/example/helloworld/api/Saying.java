package com.example.helloworld.api;

import org.hibernate.validator.constraints.Length;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author msimon
 *
 */
public class Saying {

	private long id;
	
	@Length(max = 3)
	private String content;
	
	public Saying() {
		// Jackson deserialization
	}
	
	public Saying(final long id, final String content) {
		this.id = id;
		this.content = content;
	}
	
	/**
	 * @return the id
	 */
	@JsonProperty
	public long getId() {
		return id;
	}
	
	/**
	 * @return the content
	 */
	@JsonProperty
	public String getContent() {
		return content;
	}
	
}
