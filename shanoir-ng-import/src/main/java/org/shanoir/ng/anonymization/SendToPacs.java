package org.shanoir.ng.anonymization;

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
