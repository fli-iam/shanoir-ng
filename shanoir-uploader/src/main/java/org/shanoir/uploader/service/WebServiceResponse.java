package org.shanoir.uploader.service;

public class WebServiceResponse<T> {

	private T obj;

	private int statusCode;

	private String status;

	public WebServiceResponse() {
		this.statusCode = 0;
		this.status = "SUCCESS";
	}

	public T getObj() {
		return obj;
	}

	public void setObj(T obj) {
		this.obj = obj;
	}

	public int getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

}
