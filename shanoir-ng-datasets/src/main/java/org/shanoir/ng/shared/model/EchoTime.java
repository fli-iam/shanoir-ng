package org.shanoir.ng.shared.model;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.shanoir.ng.dataset.DatasetExpression;
import org.shanoir.ng.datasetacquisition.mr.MrProtocol;

/**
 * This class represents an echo time. It is used in the MR protocol to list and
 * rank all the echo times of the acquisition.
 * 
 * @author msimon
 *
 */
@Entity
public class EchoTime extends AbstractGenericItem {

	/**
	 * UID
	 */
	private static final long serialVersionUID = -6515796982839794497L;

	/** MR protocol. */
	@ManyToOne
	@JoinColumn(name = "mr_protocol_id")
	private MrProtocol mrProtocol;

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
	 * @return the mrProtocol
	 */
	public MrProtocol getMrProtocol() {
		return mrProtocol;
	}

	/**
	 * @param mrProtocol
	 *            the mrProtocol to set
	 */
	public void setMrProtocol(MrProtocol mrProtocol) {
		this.mrProtocol = mrProtocol;
	}

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
