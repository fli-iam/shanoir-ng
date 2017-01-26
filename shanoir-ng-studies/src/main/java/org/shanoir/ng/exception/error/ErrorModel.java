package org.shanoir.ng.exception.error;

/**
 * ErrorModel
 */
public class ErrorModel {

	private Integer code;

	private String message;

	private Object details;

	public ErrorModel code(Integer code) {
		this.code = code;
		return this;
	}

	/**
	 * Constructor.
	 * 
	 * @param code
	 */
	public ErrorModel(Integer code) {
		super();
		this.code = code;
	}

	/**
	 * Constructor.
	 * 
	 * @param message
	 */
	public ErrorModel message(String message) {
		this.message = message;
		return this;
	}

	/**
	 * Constructor.
	 * 
	 * @param code
	 * @param message
	 */
	public ErrorModel(Integer code, String message) {
		super();
		this.code = code;
		this.message = message;
	}

	/**
	 * Constructor.
	 * 
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
	 * @return the code
	 */
	public Integer getCode() {
		return code;
	}

	/**
	 * @param code the code to set
	 */
	public void setCode(Integer code) {
		this.code = code;
	}

	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * @param message the message to set
	 */
	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * @return the details
	 */
	public Object getDetails() {
		return details;
	}

	/**
	 * @param details the details to set
	 */
	public void setDetails(Object details) {
		this.details = details;
	}

}
