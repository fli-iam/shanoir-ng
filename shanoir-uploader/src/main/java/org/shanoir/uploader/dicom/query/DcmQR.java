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

package org.shanoir.uploader.dicom.query;

import java.util.List;

import org.apache.log4j.Logger;
import org.dcm4che2.data.DicomObject;

/**
 * This class wrapps the usage of the DcmQR object
 * of dcm4che2.
 * @author mkain
 *
 */
public class DcmQR implements IDcmQR {

	private static Logger logger = Logger.getLogger(DcmQR.class);

	public void main(final String[] args) throws Exception {
	}

	public List<DicomObject> query(final String[] args) throws Exception {

		try{
			return org.dcm4che2.tool.dcmqr.DcmQR.query(args);
		}

		catch (final Exception e) {
			logger.error("Error stack trace : ");
			e.printStackTrace();
			logger.error("\n\n Message : "+e.getMessage()
					+"\n toString : "+e.toString()
					+"\n LocalizedMessage : "+e.getLocalizedMessage()
					+"\n Cause :  "+e.getCause()+"\n");
			logger.error(e);
			return null ;
		}
	}

	public void remove() {
	}

}
