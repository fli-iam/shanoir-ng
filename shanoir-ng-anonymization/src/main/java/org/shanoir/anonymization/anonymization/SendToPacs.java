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

package org.shanoir.anonymization.anonymization;

import java.io.IOException;

import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.tool.storescu.StoreSCU;

/**
 * TEMPORARY CLASS.
 * Used to test anonymization process.
 * 
 * @author msimon
 *
 */
public class SendToPacs {
	
	public void processSendToPacs(final String folderPath) {
		ApplicationEntity  ae = new ApplicationEntity();
		try {
			StoreSCU sc = new StoreSCU(ae);
			String[] args = {"-c", "DCM4CHEE@localhost:11112", folderPath };		
			sc.main(args);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
