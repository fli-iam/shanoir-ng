package org.shanoir.uploader.nominativeData;

import java.util.HashMap;
import java.util.Map;
import java.util.Observable;

import org.apache.log4j.Logger;
import org.shanoir.dicom.importer.UploadState;

/**
 * This class contains a hash key of nominative data extracted from the
 * workfolder.
 * 
 * @author atouboul
 *
 */

public class CurrentNominativeDataModel extends Observable {

	private static Logger logger = Logger.getLogger(CurrentNominativeDataModel.class);
	// Hash key = folder name;
	Map<String, NominativeDataUploadJob> currentUploads = null;

	String hashKey = null;

	public HashMap<String, NominativeDataUploadJob> getCurrentUploads() {
		if (currentUploads == null) {
			return new HashMap<String, NominativeDataUploadJob>();
		}
		return (HashMap<String, NominativeDataUploadJob>) currentUploads;
	}

	public void setCurrentUploads(Map<String, NominativeDataUploadJob> currentUploads) {
		this.currentUploads = currentUploads;
		String[] msg = { "fill" };
		setChanged();
		notifyObservers(msg);
	}

	public void addUpload(String absolutePath, NominativeDataUploadJob nominativeDataUploadJob) {
		getCurrentUploads().put(absolutePath, nominativeDataUploadJob);
		if (nominativeDataUploadJob.getUploadPercentage().equals("FINISHED_UPLOAD")) {
			nominativeDataUploadJob.setUploadPercentage("FINISHED");
		}
		String[] msg = { "add", absolutePath };
		setChanged();
		notifyObservers(msg);
	}

	public void updateUploadPercentage(String absolutePath, String percentage) {
		currentUploads.get(absolutePath).setUploadPercentage(percentage);
		String[] msg = { "UpdatePercent", absolutePath, percentage };
		setChanged();
		notifyObservers(msg);
	}

	public String getHashKey() {
		return hashKey;
	}

	public void setHashKey(String hashKey) {
		this.hashKey = hashKey;
	}

	public void reset() {
		this.currentUploads = null;
		this.hashKey = null;
	}

}
