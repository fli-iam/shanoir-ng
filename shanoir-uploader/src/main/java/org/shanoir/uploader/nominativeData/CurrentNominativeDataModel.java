package org.shanoir.uploader.nominativeData;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Observable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class contains a hash key of nominative data extracted from the
 * workfolder.
 * 
 * @author atouboul
 *
 */

@SuppressWarnings("deprecation")
public class CurrentNominativeDataModel extends Observable {

	private static final Logger logger = LoggerFactory.getLogger(CurrentNominativeDataModel.class);
	// Hash key = folder name;
	Map<String, NominativeDataUploadJob> currentUploads = null;

	String hashKey = null;

	public HashMap<String, NominativeDataUploadJob> getCurrentUploads() {
		if (currentUploads == null) {
			return new LinkedHashMap<String, NominativeDataUploadJob>();
		}
		return (LinkedHashMap<String, NominativeDataUploadJob>) currentUploads;
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
