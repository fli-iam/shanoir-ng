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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Observable;

import org.shanoir.ng.importer.model.ImportJob;
import org.shanoir.ng.importer.model.UploadState;
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

    private static final Logger LOG = LoggerFactory.getLogger(CurrentNominativeDataModel.class);
    // Hash key = folder name;
    Map<String, ImportJob> currentUploads = null;

    String hashKey = null;

    public HashMap<String, ImportJob> getCurrentUploads() {
        if (currentUploads == null) {
            return new LinkedHashMap<String, ImportJob>();
        }
        return (LinkedHashMap<String, ImportJob>) currentUploads;
    }

    public void setCurrentUploads(Map<String, ImportJob> currentUploads) {
        this.currentUploads = currentUploads;
        String[] msg = {"fill"};
        setChanged();
        notifyObservers(msg);
    }

    public void addUpload(String absolutePath, ImportJob nominativeDataImportJob) {
        getCurrentUploads().put(absolutePath, nominativeDataImportJob);
        if (nominativeDataImportJob.getUploadPercentage().equals(UploadState.FINISHED.toString())) { // TODO : delete this
            nominativeDataImportJob.setUploadPercentage(UploadState.FINISHED.toString());
        }
        String[] msg = {"add", absolutePath};
        setChanged();
        notifyObservers(msg);
    }

    public void updateUploadPercentage(String absolutePath, String percentage) {
        currentUploads.get(absolutePath).setUploadPercentage(percentage);
        String[] msg = {"UpdatePercent", absolutePath, percentage};
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
