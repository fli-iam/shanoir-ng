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

/**
 * The parametrized type is the type for the uid keys
 */
public class AcquisitionAttributes<T> {

	private ConcurrentMap<T, Attributes> datasetMap = new ConcurrentHashMap<>();

	public Attributes getDatasetAttributes(T id) {
		return datasetMap.get(id);
	}

	public List<Attributes> getAllDatasetAttributes() {
		return new ArrayList<>(datasetMap.values());
	}

	public void addDatasetAttributes(T id, Attributes attributes) {
		this.datasetMap.put(id, attributes);
	}

	@Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (T dsId : datasetMap.keySet()) {
            sb.append("dataset ").append(dsId).append("\n");
            for(String line : datasetMap.get(dsId).toString(1000, 1000).split("\n")) {
                sb.append("\t").append(line).append("\n");
            }
        }
        return sb.toString();
    }

	public Set<T> getDatasetIds() {
        return datasetMap.keySet();
    }

	public void merge(AcquisitionAttributes<T> dicomAcquisitionAttributes) {
		for (T datasetId : dicomAcquisitionAttributes.getDatasetIds()) {
			addDatasetAttributes(datasetId, dicomAcquisitionAttributes.getDatasetAttributes(datasetId));
		}
	}

    public Attributes getFirstDatasetAttributes() {
        if (datasetMap != null && datasetMap.size() > 0) {
			return datasetMap.entrySet().iterator().next().getValue();
		} else {
			return null;
		}
    }

	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof AcquisitionAttributes<?>)) {
			return false;
		} 
		try {
			@SuppressWarnings("unchecked")
			AcquisitionAttributes<T> other = (AcquisitionAttributes<T>) obj;
			for(T id : datasetMap.keySet()) {
				Attributes attributes = datasetMap.get(id);
				Attributes otherAttributes = other.getDatasetAttributes(id);
				if (otherAttributes == null || !otherAttributes.equals(attributes)) {
					return false;
				}
			}
			return true;
		} catch (ClassCastException e) {
			return false;
		}

	}

	public Class getParametrizedType() {
		if (this.datasetMap != null && !this.datasetMap.keySet().isEmpty()) {
			return this.datasetMap.keySet().iterator().next().getClass();
		} else {
			return null;
		}
	}
}