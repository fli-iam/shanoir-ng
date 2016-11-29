package org.shanoir.ng.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModelProperty;

/**
 * ErrorModel
 */
public class ErrorModel {
	
	@JsonProperty("code")
	private Integer code = null;

	@JsonProperty("message")
	private String message = null;

	public ErrorModel code(Integer code) {
		this.code = code;
		return this;
	}

	/**
	 * Get code
	 *
	 * @return code
	 **/
	@ApiModelProperty(required = true, value = "")
	public Integer getCode() {
		return code;
	}

	public void setCode(Integer code) {
		this.code = code;
	}

	public ErrorModel message(String message) {
		this.message = message;
		return this;
	}

	/**
	 * Get message
	 *
	 * @return message
	 **/
	@ApiModelProperty(required = true, value = "")
	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

}
