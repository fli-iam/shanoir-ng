package org.shanoir.ng.model.error;

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

	@JsonProperty("details")
	private Object details = null;

	public ErrorModel code(Integer code) {
		this.code = code;
		return this;
	}

	/**
	 * @param code
	 * @param message
	 * @param details
	 */
	public ErrorModel(Integer code, String message, Object details) {
		super();
		this.code = code;
		this.message = message;
		this.details = details;
	}

	/**
	 * @param code
	 * @param message
	 */
	public ErrorModel(int code, String message) {
		super();
		this.code = code;
		this.message = message;
		this.details = null;
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


	/**
	 * Get details
	 *
	 * @return details
	 **/
	@ApiModelProperty(value = "")
	public Object getDetails() {
		return details;
	}

	public void setDetails(Object details) {
		this.details = details;
	}

}
