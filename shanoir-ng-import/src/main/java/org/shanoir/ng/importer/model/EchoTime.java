package org.shanoir.ng.importer.model;

public class EchoTime {

	/**
	 * The echo number. Comes from dicom tag (0018,0086) VR=IS, VM=1-n Echo
	 * Number(s).
	 */
	private Integer echoNumber;

	/**
	 * Comes from the dicom tag (0018,0081) VR=DS, VM=1 Echo Time. The unit of
	 * measure must be in millisec.
	 */
	private Double echoTime;

	public Integer getEchoNumber() {
		return echoNumber;
	}

	public void setEchoNumber(Integer echoNumber) {
		this.echoNumber = echoNumber;
	}

	public Double getEchoTime() {
		return echoTime;
	}

	public void setEchoTime(Double echoTime) {
		this.echoTime = echoTime;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + echoNumber;
		result = prime * result + echoTime.hashCode();
		return result;
	}

}
