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

package org.shanoir.ng.download;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.dataset.model.DatasetExpression;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.datasetfile.DatasetFile;
import org.shanoir.ng.examination.model.Examination;

public class ExaminationAttributes {

	private ConcurrentMap<Long, AcquisitionAttributes> acquisitionMap = new ConcurrentHashMap<>();

    public ExaminationAttributes() {}

    public AcquisitionAttributes getAcquisitionAttributes(long id) {
		return acquisitionMap.get(id);
	}

    public Attributes getDatasetAttributes(long acquisitionId, long datasetId) {
        if (acquisitionMap.containsKey(acquisitionId)) {
            return acquisitionMap.get(acquisitionId).getDatasetAttributes(datasetId);
        } else return null;
	}

    public List<Attributes> getAllDatasetAttributes() {
        List<Attributes> res = new ArrayList<>();
        for (AcquisitionAttributes acqAttributes : acquisitionMap.values()) {
            for (Attributes attr : acqAttributes.getAllDatasetAttributes()) {
                res.add(attr);
            }
        }
        return res;
	}

	public void addDatasetAttributes(long acquisitionId, long datasetId, Attributes attributes) {
		if (!acquisitionMap.containsKey(acquisitionId)) {
            acquisitionMap.put(acquisitionId, new AcquisitionAttributes());
        }
        acquisitionMap.get(acquisitionId).addDatasetAttributes(datasetId, attributes);
	}

    public void addDatasetAttributes(Examination examination, Attributes singleImageAttributes) {
        //String serieUID = singleImageAttributes.getString(Tag.SeriesInstanceUID);
        String sopUID = singleImageAttributes.getString(Tag.SOPInstanceUID);
        if (sopUID != null && examination != null && examination.getDatasetAcquisitions() != null) {
            for (DatasetAcquisition acquisition : examination.getDatasetAcquisitions()) {
                if (acquisition.getDatasets() != null) {
                    for (Dataset dataset : acquisition.getDatasets()) {
                        if (dataset.getDatasetExpressions() != null) {
                            for (DatasetExpression expression : dataset.getDatasetExpressions()) {
                                if (expression.getDatasetFiles() != null) {
                                    for (DatasetFile file : expression.getDatasetFiles()) {
                                        if (file.getPath() != null) {
                                            String datasetSopUID =  WADODownloaderService.extractInstanceUID(file.getPath());
                                            if (sopUID.equals(datasetSopUID)) {
                                                this.addDatasetAttributes(acquisition.getId(), dataset.getId(), singleImageAttributes);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Long acqId : acquisitionMap.keySet()) {
            sb.append("acquisition ").append(acqId).append("\n");
            for(String line : acquisitionMap.get(acqId).toString().split("\n")) {
                sb.append("\t").append(line).append("\n");
            }
        }
        return sb.toString();
    }

    public Set<Long> getAcquisitionIds() {
        return acquisitionMap.keySet();
    }

    public void addAcquisitionAttributes(long acquisitionId, AcquisitionAttributes dicomAcquisitionAttributes) {
        if (acquisitionMap.containsKey(acquisitionId)) {
            acquisitionMap.get(acquisitionId).merge(dicomAcquisitionAttributes);
        } else {
            acquisitionMap.put(acquisitionId, dicomAcquisitionAttributes);
        }
    }

}