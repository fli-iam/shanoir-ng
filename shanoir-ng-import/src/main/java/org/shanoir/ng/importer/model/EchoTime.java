/**
 * Shanoir NG - Import, manage and share neuroimaging data
 * Copyright (C) 2009-2019 Inria - https://www.inria.fr/
 * Contact us on https://project.inria.fr/shanoir/
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/gpl-3.0.html
 */

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
