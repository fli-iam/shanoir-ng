package org.shanoir.uploader.action;

import java.util.Date;

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;
import org.shanoir.uploader.dicom.query.Patient;
import org.shanoir.uploader.dicom.query.Study;
import org.shanoir.uploader.gui.MainWindow;
import org.shanoir.uploader.utils.Util;

/**
 * This class holds data received within the DICOM query,
 * but needed after for upload and anonymization. It checks
 * for this data after the query, because when this data
 * are missing the upload and anonymization are useless.
 * 
 * @author mkain
 *
 */
public class DicomDataTransferObject {

	private static Logger logger = Logger.getLogger(DicomDataTransferObject.class);
	
	private static final String DATE = "date";

	private static final String ID = "id";

	private static final String SEX = "sex";

	private static final String NAME = "name";

	private static final String BIRTH_DATE = "birthDate";
	
	private Date birthDate;
	
	private String sex;
	
	private String birthName;
	
	private String lastName;
	
	private String firstName;
	
	private String newPatientID;

	private String newPatientIDPseudonymus;
	
	private String IPP;
	
	private String subjectIdentifier;
	
	private String studyInstanceUID;
	
	private String studyDescription;

	private Date studyDate;
	
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
	
	/**
	 * Extracts data from Patient object.
	 * We do not extract here the patient name and transfer it with the upload-job.xml
	 * to the server as we anonymize the data already on the client.
	 * 
	 * @param mainWindow
	 * @param patient
	 * @throws Exception
	 */
	public DicomDataTransferObject(final MainWindow mainWindow, final Patient patient, final Study study) throws Exception {
		/**
		 * Extract from Patient object.
		 */
		// extract birth date of the patient of the first selected series
		// attention: the birth date is only taken from one patient, even
		// when the tree could display and select multiple patients
		final String dicomBirthDate = patient.getDescriptionMap().get(BIRTH_DATE);
		if (dicomBirthDate != null && !"".equals(dicomBirthDate)) {
			birthDate = Util.convertStringDicomDateToDate(dicomBirthDate);
		}
		final String name = patient.getDescriptionMap().get(NAME);
		firstName = Util.computeFirstName(name);
		lastName = Util.computeLastName(name);
		sex = patient.getDescriptionMap().get(SEX);
		IPP = patient.getDescriptionMap().get(ID);

		/**
		 * Extract from Study object.
		 */
		studyInstanceUID = study.getDescriptionMap().get(ID);
		String dicomStudyDate = study.getDescriptionMap().get(DATE);
		if (dicomStudyDate != null && !"".equals(dicomStudyDate)) {
			studyDate = Util.convertStringDicomDateToDate(dicomStudyDate);
		} else if (mainWindow != null) {
			logger.error("Study date could not be used for import.");
			JOptionPane.showMessageDialog(mainWindow.frame,
				    "Study date could not be used for import.",
				    "Data error",
				    JOptionPane.ERROR_MESSAGE);
			throw new Exception();
		}
		studyDescription = study.getStudyDescriptionOverwrite();
	}

	public String getIPP() {
		return IPP;
	}

	public void setIPP(String iPP) {
		IPP = iPP;
	}

	public Date getBirthDate() {
		return birthDate;
	}

	public String getNewPatientID() {
		return newPatientID;
	}

	public void setNewPatientID(String newPatientID) {
		this.newPatientID = newPatientID;
	}
	
	public String getSubjectIdentifier() {
		return subjectIdentifier;
	}

	/**
	 * @param subjectIdentifier the subjectIdentifier to set
	 */
	public void setSubjectIdentifier(String subjectIdentifier) {
		this.subjectIdentifier = subjectIdentifier;
	}

	public String getSex() {
		return sex;
	}

	public void setSex(String sex) {
		this.sex = sex;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	
	public String getBirthName() {
		return birthName;
	}

	public void setBirthName(String birthName) {
		this.birthName = birthName;
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

	public Date getStudyDate() {
		return studyDate;
	}

	public void setStudyDate(Date studyDate) {
		this.studyDate = studyDate;
	}

	public String getNewPatientIDPseudonymus() {
		return newPatientIDPseudonymus;
	}

	public void setNewPatientIDPseudonymus(String newPatientIDPseudonymus) {
		this.newPatientIDPseudonymus = newPatientIDPseudonymus;
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

	public void setBirthDate(Date birthDate) {
		this.birthDate = birthDate;
	}	
	
}
