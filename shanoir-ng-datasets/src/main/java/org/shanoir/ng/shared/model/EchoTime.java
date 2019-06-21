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

package org.shanoir.ng.shared.model;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.shanoir.ng.dataset.modality.MrDataset;
import org.shanoir.ng.shared.core.model.AbstractEntity;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * This class represents an echo time. It is used in the MR protocol to list and
 * rank all the echo times of the acquisition.
 * 
 * @author msimon
 *
 */
@Entity
public class EchoTime extends AbstractEntity {

	/**
	 * UID
	 */
	private static final long serialVersionUID = -6515796982839794497L;


	/** MR dataset. */
	@ManyToOne
	@JoinColumn(name = "mr_dataset_id")
	@JsonIgnore
	private MrDataset mrDataset;
	
	/**
	 * The echo number. Comes from dicom tag (0018,0086) VR=IS, VM=1-n Echo
	 * Number(s).
	 */
	@Min(value = 0)
	private Integer echoNumber;

	/**
	 * Comes from the dicom tag (0018,0081) VR=DS, VM=1 Echo Time. The unit of
	 * measure must be in millisec.
	 */
	@NotNull
	private Double echoTimeValue;

	/**
	 * @return the echoNumber
	 */
	public Integer getEchoNumber() {
		return echoNumber;
	}

	/**
	 * @param echoNumber
	 *            the echoNumber to set
	 */
	public void setEchoNumber(Integer echoNumber) {
		this.echoNumber = echoNumber;
	}

	/**
	 * @return the echoTimeValue
	 */
	public Double getEchoTimeValue() {
		return echoTimeValue;
	}

	/**
	 * @param echoTimeValue
	 *            the echoTimeValue to set
	 */
	public void setEchoTimeValue(Double echoTimeValue) {
		this.echoTimeValue = echoTimeValue;
	}
	
	
	public void setMrDataset(MrDataset mrDataset) {
		this.mrDataset = mrDataset;
	}

	@Override
	  public boolean equals(Object v) {
	        boolean retVal = false;
	        boolean retVal1 = false;
	
	        if (v instanceof EchoTime){
	        	EchoTime echoTime = (EchoTime) v;
	            retVal = echoTime.getEchoNumber() == this.echoNumber;
	            retVal1 = echoTime.getEchoTimeValue() == this.echoTimeValue;
	        }
	
	     return retVal && retVal1;
	  }
	
	    @Override
	    public int hashCode() {
	        int hash = 7;
	        hash = 17 * hash + (this.getEchoNumber() != null ? this.getEchoNumber().hashCode() : 0);
	        hash = 17 * hash + (this.getEchoTimeValue() != null ? this.getEchoTimeValue().hashCode() : 0);
	        return hash;
	    }

}
