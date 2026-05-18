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
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.dcm4che3.data.Attributes;
import org.shanoir.ng.dicom.WADOURLHandler;

/**
 * The parameterized type is the type for the uid keys
 */
public class ExaminationAttributes<T> {

    private ConcurrentMap<T, Optional<AcquisitionAttributes<T>>> acquisitionMap = new ConcurrentHashMap<>();

    private WADOURLHandler wadoURLHandler;

    public ExaminationAttributes(WADOURLHandler wadoURLHandler) {
        this.wadoURLHandler = wadoURLHandler;
    }

    public AcquisitionAttributes<T> getAcquisitionAttributes(T id) {
        return acquisitionMap.get(id).orElse(null);
    }

    public Attributes getDatasetAttributes(T acquisitionId, T datasetId) {
        if (acquisitionMap.containsKey(acquisitionId)) {
            if (acquisitionMap.get(acquisitionId).isPresent()) {
                return acquisitionMap.get(acquisitionId).get().getDatasetAttributes(datasetId);
            } else {
                return null;
            }
        } else return null;
    }

    public List<Attributes> getAllDatasetAttributes() {
        List<Attributes> res = new ArrayList<>();
        for (Optional<AcquisitionAttributes<T>> acqAttributes : acquisitionMap.values()) {
            if (acqAttributes.isPresent()) {
                for (Attributes attr : acqAttributes.get().getAllDatasetAttributes()) {
                    res.add(attr);
                }
            }
        }
        return res;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (T acqId : acquisitionMap.keySet()) {
            sb.append("acquisition ").append(acqId).append("\n");
            for (String line : acquisitionMap.get(acqId).toString().split("\n")) {
                sb.append("\t").append(line).append("\n");
            }
        }
        return sb.toString();
    }

    public Set<T> getAcquisitionIds() {
        return acquisitionMap.keySet();
    }

    public void addAcquisitionAttributes(T acquisitionId, AcquisitionAttributes<T> dicomAcquisitionAttributes) {
        if (acquisitionMap.containsKey(acquisitionId) && acquisitionMap.get(acquisitionId).isPresent()) {
            acquisitionMap.get(acquisitionId).get().merge(dicomAcquisitionAttributes);
        } else {
            acquisitionMap.put(acquisitionId, Optional.ofNullable(dicomAcquisitionAttributes));
        }
    }

    public boolean has(T acqId) {
        return acquisitionMap.containsKey(acqId);
    }

}
