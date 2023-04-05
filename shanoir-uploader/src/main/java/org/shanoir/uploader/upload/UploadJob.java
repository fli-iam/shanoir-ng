package org.shanoir.uploader.upload;

import java.util.Collection;
import java.util.Iterator;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.shanoir.uploader.dicom.PreImportData;
import org.shanoir.uploader.dicom.Serie;

/**
 * This class contains all informations concerning an upload.
 * The UploadService's state engine is implemented within here.
 * @author mkain
 *
 */
@XmlType
@XmlRootElement
public class UploadJob {

	private String patientID;
	
	private String patientBirthDate;
	
	private String patientSex;
	
	private String subjectIdentifier;
	
	private String studyInstanceUID;
	
	private String studyDescription;

	private String studyDate;
	
	private String uploadDate;
	
	private UploadState uploadState;
	
	private Collection<Serie> series;
	
	private String birthNameHash1;

	private String birthNameHash2;

	private String birthNameHash3;

	private String lastNameHash1;

	private String lastNameHash2;

	private String lastNameHash3;

	private String firstNameHash1;

	private String firstNameHash2;

	private String firstNameHash3;

	private String birthDateHash;
	
	private PreImportData preImportdata;

	public PreImportData getPreImportdata() {
		return preImportdata;
 	}

	public void setPreImportdata(PreImportData preImportdata) {
		this.preImportdata = preImportdata;
	}

	public UploadState getUploadState() {
		return uploadState;
	}

	public void setUploadState(UploadState state) {
		this.uploadState = state;
	}

	@XmlElementWrapper(name="series")
	@XmlElement(name="serie")
	public Collection<Serie> getSeries() {
		return series;
	}
	
	public Serie getSerie(final String id) {
		for (Iterator iterator = series.iterator(); iterator.hasNext();) {
			Serie serie = (Serie) iterator.next();
			if (id.equals(serie.getId())) {
				return serie;
			}
		}
		return null;
	}

	public void setSeries(Collection<Serie> series) {
		this.series = series;
	}

	public String getUploadDate() {
		return uploadDate;
	}

	public void setUploadDate(String uploadDate) {
		this.uploadDate = uploadDate;
	}

	public String getPatientID() {
		return patientID;
	}

	public void setPatientID(String patientID) {
		this.patientID = patientID;
	}

	public String getPatientBirthDate() {
		return patientBirthDate;
	}

	public void setPatientBirthDate(String patientBirthDate) {
		this.patientBirthDate = patientBirthDate;
	}

	public String getPatientSex() {
		return patientSex;
	}

	public void setPatientSex(String patientSex) {
		this.patientSex = patientSex;
	}
	
	public String getSubjectIdentifier() {
		return subjectIdentifier;
	}

	public void setSubjectIdentifier(String subjectIdentifier) {
		this.subjectIdentifier = subjectIdentifier;
	}

	public String getStudyInstanceUID() {
		return studyInstanceUID;
	}

	public void setStudyInstanceUID(String studyInstanceUID) {
		this.studyInstanceUID = studyInstanceUID;
	}

	public String getStudyDescription() {
		return studyDescription;
	}

	public void setStudyDescription(String studyDescription) {
		this.studyDescription = studyDescription;
	}

	public String getStudyDate() {
		return studyDate;
	}

	public void setStudyDate(String studyDate) {
		this.studyDate = studyDate;
	}

	public String getBirthNameHash1() {
		return birthNameHash1;
	}

	public void setBirthNameHash1(String birthNameHash1) {
		this.birthNameHash1 = birthNameHash1;
	}

	public String getBirthNameHash2() {
		return birthNameHash2;
	}

	public void setBirthNameHash2(String birthNameHash2) {
		this.birthNameHash2 = birthNameHash2;
	}

	public String getBirthNameHash3() {
		return birthNameHash3;
	}

	public void setBirthNameHash3(String birthNameHash3) {
		this.birthNameHash3 = birthNameHash3;
	}

	public String getLastNameHash1() {
		return lastNameHash1;
	}

	public void setLastNameHash1(String lastNameHash1) {
		this.lastNameHash1 = lastNameHash1;
	}

	public String getLastNameHash2() {
		return lastNameHash2;
	}

	public void setLastNameHash2(String lastNameHash2) {
		this.lastNameHash2 = lastNameHash2;
	}

	public String getLastNameHash3() {
		return lastNameHash3;
	}

	public void setLastNameHash3(String lastNameHash3) {
		this.lastNameHash3 = lastNameHash3;
	}

	public String getFirstNameHash1() {
		return firstNameHash1;
	}

	public void setFirstNameHash1(String firstNameHash1) {
		this.firstNameHash1 = firstNameHash1;
	}

	public String getFirstNameHash2() {
		return firstNameHash2;
	}

	public void setFirstNameHash2(String firstNameHash2) {
		this.firstNameHash2 = firstNameHash2;
	}

	public String getFirstNameHash3() {
		return firstNameHash3;
	}

	public void setFirstNameHash3(String firstNameHash3) {
		this.firstNameHash3 = firstNameHash3;
	}

	public String getBirthDateHash() {
		return birthDateHash;
	}

	public void setBirthDateHash(String birthDateHash) {
		this.birthDateHash = birthDateHash;
	}
	
}
