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

package org.shanoir.uploader.nominativeData;

import java.util.HashMap;
import java.util.Map;
import java.util.Observable;

import org.apache.log4j.Logger;

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
