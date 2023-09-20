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

public class ExaminationAttributes {

	private ConcurrentMap<Long, AcquisitionAttributes> acquisitionMap = new ConcurrentHashMap<>();

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