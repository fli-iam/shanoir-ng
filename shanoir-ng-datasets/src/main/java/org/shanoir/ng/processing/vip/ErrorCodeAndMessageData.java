package org.shanoir.ng.processing.vip;

import java.util.Map;

public class ErrorCodeAndMessageData {
	public int errorCode;
	public String errorMessage;
	public Map<String, String> errorDetails;

	public ErrorCodeAndMessageData() {
	}

	public ErrorCodeAndMessageData(int errorCode, String errorMessage, Map<String, String> errorDetails) {
		super();
		this.errorCode = errorCode;
		this.errorMessage = errorMessage;
		this.errorDetails = errorDetails;
	}

	/**
	 * @return the errorCode
	 */
	public int getErrorCode() {
		return errorCode;
	}

	/**
	 * @param errorCode the errorCode to set
	 */
	public void setErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}

	/**
	 * @return the errorMessage
	 */
	public String getErrorMessage() {
		return errorMessage;
	}

	/**
	 * @param errorMessage the errorMessage to set
	 */
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	/**
	 * @return the errorDetails
	 */
	public Map<String, String> getErrorDetails() {
		return errorDetails;
	}

	/**
	 * @param errorDetails the errorDetails to set
	 */
	public void setErrorDetails(Map<String, String> errorDetails) {
		this.errorDetails = errorDetails;
	}
	
	
}