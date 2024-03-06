package org.shanoir.uploader.dicom.retrieve;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.shanoir.uploader.dicom.query.ConfigBean;
import org.weasis.dicom.param.AdvancedParams;
import org.weasis.dicom.param.ConnectOptions;
import org.weasis.dicom.param.DicomNode;
import org.weasis.dicom.param.ListenerParams;
import org.weasis.dicom.tool.DicomListener;

/**
 * The DcmRcvHelper handles the download of DICOM files.
 * 
 * @author mkain
 *
 */
public class DcmRcvManager {

	private static final Logger logger = LoggerFactory.getLogger(DcmRcvManager.class);
	
	private static final String SOP_CLASSES_PROPERTIES = "/sop-classes.properties";

	/**
	 * In the brackets '{ggggeeee}' the dicom attribute value is used to be replaced.
	 * We store in a folder with the SeriesInstanceUID and the file name of the SOPInstanceUID.
	 */
	private static final String STORAGE_PATTERN = "{0020000E}" + File.separator + "{00080018}";
	
	public static final String DICOM_FILE_SUFFIX = ".dcm";
	
	private DicomNode scpNode;
	
	private ListenerParams lParams;
	
	private DicomListener listener;

	public void configure(final ConfigBean configBean) throws MalformedURLException {
		logger.info("Configuring local DICOM server with params:"
				+ " AET title: " + configBean.getLocalDicomServerAETCalling()
				+ ", AET host: " + configBean.getLocalDicomServerHost()
				+ ", AET port: " + configBean.getLocalDicomServerPort());
        scpNode = new DicomNode(configBean.getLocalDicomServerAETCalling(), configBean.getLocalDicomServerHost(), configBean.getLocalDicomServerPort());
		AdvancedParams params = new AdvancedParams();
		params.setTsuidOrder(AdvancedParams.IVR_LE_ONLY);
        ConnectOptions connectOptions = new ConnectOptions();
        connectOptions.setConnectTimeout(30000);
        connectOptions.setAcceptTimeout(50000);
        // Concurrent DICOM operations
        connectOptions.setMaxOpsInvoked(15);
        connectOptions.setMaxOpsPerformed(15);
		params.setConnectOptions(connectOptions);
		URL sOPClassesPropertiesFileURL = this.getClass().getResource(SOP_CLASSES_PROPERTIES);
		lParams = new ListenerParams(params, true, STORAGE_PATTERN + DICOM_FILE_SUFFIX, sOPClassesPropertiesFileURL, null);
	}
	
	/**
	 * Called from a synchronized method only, so should not be a problem for multiple usages.
	 * 
	 * @param folderPath
	 */
	public void startSCPServer(final String folderPath) {
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
		}		
	}
	
}
