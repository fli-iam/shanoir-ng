package org.shanoir.ng.importer.anonymization.uidGenaration;

/**
 * @author  dclunie
 */

public class DicomException extends Exception {

	private static final String identString = "@(#) $Header: /userland/cvs/pixelmed/imgbook/com/pixelmed/dicom/DicomException.java,v 1.4 2003/02/23 14:42:08 dclunie Exp $";

	/**
	 * @param msg
	 */
	public DicomException(String msg) {
		super(msg);
	}
}
