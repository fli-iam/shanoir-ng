package org.shanoir.uploader.dicom.anonymize;

import org.shanoir.uploader.action.DicomDataTransferObject;

public interface ISubjectIdentifierGenerator {

	/**
	 * new double Hash version: sha256(hashP1(prénom)||
	 * hashP1(nom_naissance)||hashP1(date_naissance)) hashP1: un appel de
	 * Pseudonymus avec le premier algorithme (qui a la valeur 0 ) sans
	 * traitement soundex.
	 */
	String generateSubjectIdentifierWithPseudonymus(DicomDataTransferObject dicomData);

	/**
	 * Generate on using: hash(firstName + lastName + birthDate)
	 * 
	 * @param lastName
	 * @param firstName
	 * @param birthDate
	 * @return
	 */
	String generateSubjectIdentifier(DicomDataTransferObject dicomData);

}