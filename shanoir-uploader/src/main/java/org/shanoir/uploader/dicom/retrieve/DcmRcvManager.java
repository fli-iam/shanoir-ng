package org.shanoir.uploader.dicom.retrieve;

import java.io.File;

import org.apache.log4j.Logger;
import org.shanoir.uploader.dicom.query.ConfigBean;
import org.weasis.dicom.param.AdvancedParams;
import org.weasis.dicom.param.ConnectOptions;
import org.weasis.dicom.param.DicomNode;
import org.weasis.dicom.param.ListenerParams;
import org.weasis.dicom.tool.DicomListener;

/**
 * The DcmRcvHelper handles the download of DICOM files.
 * It's a local service / server which is started, when
 * the ShanoirUploader is started.
 * 
 * @author mkain
 *
 */
public class DcmRcvManager {

	private static Logger logger = Logger.getLogger(DcmRcvManager.class);
	
	/**
	 * In the brackets '{ggggeeee}' the dicom attribute value is used to be replaced.
	 * We store in a folder with the SeriesInstanceUID and the file name of the SOPInstanceUID.
	 */
	private static final String STORAGE_PATTERN = "/{0020000E}/{00080018}";
	
	private static final String DICOM_FILE_SUFFIX = ".dcm";
	
	private DicomNode scpNode;
	
	private ListenerParams lParams;
	
	private DicomListener listener;

	public void configure(final ConfigBean configBean) {
		logger.info("Configuring local DICOM server with params:"
				+ " AET title: " + configBean.getLocalDicomServerAETCalling()
				+ ", AET host: " + configBean.getLocalDicomServerHost()
				+ ", AET port: " + configBean.getLocalDicomServerPort());
        scpNode = new DicomNode(configBean.getLocalDicomServerAETCalling(), configBean.getLocalDicomServerHost(), configBean.getLocalDicomServerPort());
		AdvancedParams params = new AdvancedParams();
        ConnectOptions connectOptions = new ConnectOptions();
        connectOptions.setConnectTimeout(30000);
        connectOptions.setAcceptTimeout(50000);
        // Concurrent DICOM operations
        connectOptions.setMaxOpsInvoked(15);
        connectOptions.setMaxOpsPerformed(15);
        params.setConnectOptions(connectOptions);
		this.lParams = new ListenerParams(params, true, STORAGE_PATTERN + DICOM_FILE_SUFFIX, null, null);
	}
	
	public void setDestination(final String folderPath) {
		try {
			if(this.listener != null)
				listener.stop();
			File storageDir = new File(folderPath);
	        if (!storageDir.exists()) {
	        	storageDir.mkdirs();
	        }
			this.listener = new DicomListener(storageDir);
		    this.listener.start(scpNode, lParams);
	        logger.info("DICOM SCP server successfully initialized: " + this.scpNode.toString() + ", " + folderPath);
		} catch (Exception e) {
			logger.error("Error during startup of DICOM server: " + e.getMessage() + "\n DICOM server is not started.");
			System.exit(0);
		}		
	}
	
}
