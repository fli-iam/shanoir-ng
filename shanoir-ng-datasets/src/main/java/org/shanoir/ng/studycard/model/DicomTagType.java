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

package org.shanoir.ng.studycard.model;

import org.dcm4che3.data.VR;
import org.slf4j.LoggerFactory;

public enum DicomTagType {

    /**
     * When adding ad new type, configure it's compatibles operations in Operation.java
     */
    String, Long, Float, Double, Integer, Binary, Date, FloatArray, IntArray;
    
    public static DicomTagType valueOf(VR vr, VM vm) {
        if (       vr.equals(VR.AE)
                || vr.equals(VR.AS) // Age special format
                || vr.equals(VR.CS)
                || vr.equals(VR.LO)
                || vr.equals(VR.LT)
                || vr.equals(VR.PN)
                || vr.equals(VR.SH)
                || vr.equals(VR.ST)
                || vr.equals(VR.UC)
                || vr.equals(VR.UI)
                || vr.equals(VR.UR)
                || vr.equals(VR.UT)) {
            return DicomTagType.String;
            
        } else if (vr.equals(VR.AT)
                || vr.equals(VR.OB)
                || vr.equals(VR.OW)
                || vr.equals(VR.SQ)
                || vr.equals(VR.UN)
                || vr.equals(VR.OV)) {
            return DicomTagType.Binary;
            
        } else if (vr.equals(VR.DA)
                || vr.equals(VR.DT)
                || vr.equals(VR.TM)) {
            return DicomTagType.Date;
            
        } else if (vr.equals(VR.FL)
                || vr.equals(VR.OF)
                || (vr.equals(VR.DS) && Cardinality.ONE.equals(vm.getMax()))) {
            return DicomTagType.Float;
            
        } else if (vr.equals(VR.FD)
                || vr.equals(VR.OD)) {
            return DicomTagType.Double;
            
        } else if (vr.equals(VR.SS)
                || vr.equals(VR.US)
                || (vr.equals(VR.IS) && Cardinality.ONE.equals(vm.getMax()))) {
            return DicomTagType.Integer;
        
        } else if (vr.equals(VR.OL)
                || vr.equals(VR.SL)
                || vr.equals(VR.UL)
                || vr.equals(VR.UV)
                || vr.equals(VR.SV)) {
            return DicomTagType.Long;

        } else if (vr.equals(VR.DS)) {
            return DicomTagType.FloatArray;

        } else if (vr.equals(VR.IS)) {
            return DicomTagType.IntArray;

        } else {
            LoggerFactory.getLogger(DicomTagType.class).error("VR with name " + vr.name() + " (code " + vr.code() + ") is not implemented");
            return null;
        }
    }
}

