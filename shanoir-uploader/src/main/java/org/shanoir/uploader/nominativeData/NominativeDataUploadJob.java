package org.shanoir.uploader.nominativeData;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.shanoir.dicom.importer.UploadState;

/**
 * This class contains Nominative date that will be displayed in the current
 * uploads tab of ShanoirUploader.
 * 
 * @author ifakhfakh
 *
 */
@XmlType
@XmlRootElement
public class NominativeDataUploadJob {

	private String patientPseudonymusHash;

	private String patientName;

	private String IPP;

	private String studyDate;

	private String mriSerialNumber;

	private String uploadPercentage;

	private UploadState uploadState;

	public String getPatientPseudonymusHash() {
		return patientPseudonymusHash;
	}

	public void setPatientPseudonymusHash(String patientPseudonymusHash) {
		this.patientPseudonymusHash = patientPseudonymusHash;
	}

	public String getPatientName() {
		return patientName;
	}

	public void setPatientName(String patientName) {
		this.patientName = patientName;
	}

	public String getIPP() {
		return IPP;
	}

	public void setIPP(String iPP) {
		IPP = iPP;
	}

	public String getStudyDate() {
		return studyDate;
	}

	public void setStudyDate(String studyDate) {
		this.studyDate = studyDate;
	}

	public String getMriSerialNumber() {
		return mriSerialNumber;
	}

	public void setMriSerialNumber(String mriSerialNumber) {
		this.mriSerialNumber = mriSerialNumber;
	}

	public String getUploadPercentage() {
		return uploadPercentage;
	}

	public void setUploadPercentage(String uploadPercentage) {
		this.uploadPercentage = uploadPercentage;
	}

	public UploadState getUploadState() {
		return uploadState;
	}

	public void setUploadState(UploadState uploadState) {
		this.uploadState = uploadState;
	}

}
