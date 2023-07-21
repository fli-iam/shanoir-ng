package org.shanoir.uploader.dicom.retrieve;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.dcm4che2.tool.dcmrcv.DcmRcv;
import org.shanoir.uploader.dicom.query.ConfigBean;

/**
 * The DcmRcvHelper handles the download of DICOM files.
 * It's a local service / server which is started, when
 * the ShanoirUploader is started.
 * @author mkain
 *
 */
public class DcmRcvManager {

	private static Logger logger = Logger.getLogger(DcmRcvManager.class);

	/** The DICOM Server itself. */
	private DcmRcv dcmrcv = new DcmRcv();
	
	public void start(final ConfigBean configBean, final String folderPath) {
		try {
			logger.info("Start running local server DcmRcv with params:"
					+ " AET title: " + configBean.getLocalDicomServerAETCalling()
					+ ", AET host: " + configBean.getLocalDicomServerHost()
					+ ", AET port: " + configBean.getLocalDicomServerPort());
			dcmrcv.setAEtitle(configBean.getLocalDicomServerAETCalling());
			dcmrcv.setHostname(configBean.getLocalDicomServerHost());
			dcmrcv.setPort(configBean.getLocalDicomServerPort());
			dcmrcv.setDestination(folderPath);
			dcmrcv.setPackPDV(true);
			dcmrcv.setTcpNoDelay(true);
			dcmrcv.initTransferCapability();
			dcmrcv.start();
			logger.info("Started DcmRcv service successfully.");
		} catch (IOException e) {
			logger.error("Error during startup of DcmRcv: " + e.getMessage() + "\n DcmRcv is not started.");
			System.exit(0);
		}
	}

	public void stop() {
		dcmrcv.stop();
	}
	
	public void setDestination(final String folderPath) {
		dcmrcv.setDestination(folderPath);
	}
	
}
