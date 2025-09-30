package org.shanoir.uploader.dicom.retrieve;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

import org.dcm4che3.net.TransferCapability;
import org.dcm4che3.net.TransferCapability.Role;
import org.dcm4che3.tool.storescp.StoreSCP;
import org.shanoir.uploader.dicom.query.ConfigBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.dicom.param.AdvancedParams;
import org.weasis.dicom.param.ConnectOptions;
import org.weasis.dicom.param.DicomNode;
import org.weasis.dicom.param.ListenerParams;
import org.weasis.dicom.tool.DicomListener;

/**
 * The DcmRcvManager handles the download of DICOM files and starts one DICOM
 * SCP server at run up of ShanoirUploader. We start only one and keep him up
 * and running, first to be more efficient and second to prepare for DICOM push
 * from outside, what requires one DICOM SCP server up and running all time as
 * long as ShUp has been started.
 *
 * @author mkain
 *
 */
public class DcmRcvManager {

	private static final Logger logger = LoggerFactory.getLogger(DcmRcvManager.class);

	private static final String PRIVATE_SIEMENS_CSA_NON_IMAGE_STORAGE = "1.3.12.2.1107.5.9.1";

	private static final String SOP_CLASSES_PROPERTIES = "/sop-classes.properties";

	/**
	 * In the brackets '{ggggeeee}' the dicom attribute value is used to be replaced.
	 * We store all images in folders by StudyInstanceUID / SeriesInstanceUID /
	 * SOPInstanceUID (as image file name). This allows us to support at the same
	 * time with one ShUp (up and running) to receive push images for an exam/study or
	 * to search in the pacs and download another exam.
	 */
	private static final String STORAGE_PATTERN = "{0020000D}" + File.separator + "{0020000E}" + File.separator + "{00080018}";

	public static final String DICOM_FILE_SUFFIX = ".dcm";

	public void configureAndStartSCPServer(final ConfigBean configBean, final String workFolderPath) throws MalformedURLException {
		logger.info("DICOM SCP server (mini-pacs) configured locally with params:"
				+ " AET title: " + configBean.getLocalDicomServerAETCalling()
				+ ", AET host: " + configBean.getLocalDicomServerHost()
				+ ", AET port: " + configBean.getLocalDicomServerPort());
		DicomNode scpNode = new DicomNode(configBean.getLocalDicomServerAETCalling(), configBean.getLocalDicomServerHost(), configBean.getLocalDicomServerPort());
		AdvancedParams params = new AdvancedParams();
		params.setTsuidOrder(AdvancedParams.IVR_LE_ONLY);
        ConnectOptions connectOptions = new ConnectOptions();
		// 0 is unlimited below
        connectOptions.setMaxOpsInvoked(0);
        connectOptions.setMaxOpsPerformed(0);
		params.setConnectOptions(connectOptions);
		URL sOPClassesPropertiesFileURL = this.getClass().getResource(SOP_CLASSES_PROPERTIES);
		ListenerParams lParams = new ListenerParams(params, true, STORAGE_PATTERN + DICOM_FILE_SUFFIX, sOPClassesPropertiesFileURL);
		startSCPServer(workFolderPath, scpNode, lParams);
	}

	/**
	 * Start, when running up ShanoirUploader only one internal mini-pacs,
	 * that is listening all time: to allow c-moves (DICOM push) all the
	 * time from outside into this folder and split now by studyDate and
	 * StudyInstanceUID and support query/c-move as well.
	 *
	 * @param folderPath
	 */
	private void startSCPServer(final String workFolderPath, DicomNode scpNode, ListenerParams lParams) {
		try {
			File storageDir = new File(workFolderPath);
			DicomListener listener = new DicomListener(storageDir);
		    listener.start(scpNode, lParams);
			StoreSCP storeSCP = listener.getStoreSCP();
	        logger.info("DICOM SCP server (mini-pacs) successfully initialized: " + scpNode.toString() + ", " + workFolderPath);
			TransferCapability tc = storeSCP.getApplicationEntity().getTransferCapabilityFor(PRIVATE_SIEMENS_CSA_NON_IMAGE_STORAGE, Role.SCP);
			String[] ts = tc.getTransferSyntaxes();
			logger.info("Transfer syntaxes for PrivateSiemensCSANonImageStorage (OT): {}", Arrays.toString(ts));
		} catch (Exception e) {
			logger.error("DICOM SCP server (mini-pacs): error (not started): " + e.getMessage(), e);
		}
	}

}
