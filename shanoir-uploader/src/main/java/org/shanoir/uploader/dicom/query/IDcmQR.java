package org.shanoir.uploader.dicom.query;

import java.util.List;

import org.dcm4che2.data.DicomObject;

public interface IDcmQR {
	
	/**
	 * Call the main method of DcmQr.
	 *
	 * @param args
	 *            the args
	 *
	 * @throws Exception
	 *             the exception
	 */
	void main(final String[] args) throws Exception;

	/**
	 * Perform a C-FIND the query method of DcmQr.
	 *
	 * @param args
	 *            the args
	 *
	 * @return the list< dicom object>
	 *
	 * @throws Exception
	 *             the exception
	 */
	List<DicomObject> query(final String[] args) throws Exception;

	/**
	 * Removes.
	 */
	void remove();
	
}
