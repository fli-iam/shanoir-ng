package org.shanoir.ng.model.error;

public class FieldError {

	private String code;
	private String message;
	private Object givenValue;

	/**
	 * @param code
	 * @param message
	 */
	public FieldError(String code, String message, Object givenValue) {
		super();
		this.code = code;
		this.message = message;
		this.givenValue = givenValue;
	}

	/**
	 * @return the code
	 */
	public String getCode() {
		return code;
	}

	/**
	 * @param code the code to set
	 */
	public void setCode(String code) {
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
	 * @return the givenValue
	 */
	public Object getGivenValue() {
		return givenValue;
	}

	/**
	 * @param givenValue the givenValue to set
	 */
	public void setGivenValue(Object givenValue) {
		this.givenValue = givenValue;
	}
}
