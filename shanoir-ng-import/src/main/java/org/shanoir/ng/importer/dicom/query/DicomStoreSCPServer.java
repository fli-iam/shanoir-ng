package org.shanoir.ng.importer.dicom.query;

import java.io.File;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.weasis.dicom.param.AdvancedParams;
import org.weasis.dicom.param.ConnectOptions;
import org.weasis.dicom.param.DicomNode;
import org.weasis.dicom.param.ListenerParams;
import org.weasis.dicom.tool.DicomListener;

/**
 * This class inits a DICOM server for StoreSCP within the microservice import.
 * As we have to use the same port to bind to, we can not initiate multiple instances
 * of a dicom server for each user/import ongoing in Shanoir. All files, send by the
 * server, that received the c-move, SCU in this case, arrive in our SCP in the same
 * folder: storageDirPath. The API of CStoreSCP does not provide a setStorageDir to
 * be changed during runtime, so we keep the same folder during the application runtime.
 * 
 * @author mkain
 *
 */
@Service
public class DicomStoreSCPServer {

	/**
	 * In the brackets '{ggggeeee}' the dicom attribute value is used to be replaced.
	 * We store in a folder with the SeriesInstanceUID and the file name of the SOPInstanceUID.
	 */
	private static final String STORAGE_PATTERN = "/{0020000E}/{00080018}";
	
	public static final String DICOM_FILE_SUFFIX = ".dcm";

	private static final Logger LOG = LoggerFactory.getLogger(DicomStoreSCPServer.class);
	
	@Value("${shanoir.import.pacs.store.aet.called.name}")
	private String calledName;

	@Value("${shanoir.import.pacs.store.aet.called.host}")
	private String calledHost;
	
	@Value("${shanoir.import.pacs.store.aet.called.port}")
	private Integer calledPort;
	
	@Value("${shanoir.import.pacs.store.folder}")
	private String storageDirPath;
	
	@PostConstruct
	private void initServer() {
        DicomNode scpNode = new DicomNode(calledName, calledHost, calledPort);
        File storageDir = new File(storageDirPath);
        if (!storageDir.exists()) {
        		storageDir.mkdirs();
        }
		AdvancedParams params = new AdvancedParams();
        ConnectOptions connectOptions = new ConnectOptions();
        connectOptions.setConnectTimeout(3000);
        connectOptions.setAcceptTimeout(5000);
        // Concurrent DICOM operations
        connectOptions.setMaxOpsInvoked(15);
        connectOptions.setMaxOpsPerformed(15);
        params.setConnectOptions(connectOptions);
        ListenerParams lparams = new ListenerParams(params, true, STORAGE_PATTERN + DICOM_FILE_SUFFIX, null, null);
        try {
        		DicomListener listener = new DicomListener(storageDir);
            listener.start(scpNode, lparams);
        } catch (Exception e) {
           LOG.error(e.getMessage(), e);
        }
        LOG.info("DicomStoreSCPServer successfully initialized: " + calledName + ", " + calledHost + ", " + calledPort + ", " + storageDirPath);
	}

	public String getStorageDirPath() {
		return storageDirPath;
	}

	public void setStorageDirPath(String storageDirPath) {
		this.storageDirPath = storageDirPath;
	}
	
}
