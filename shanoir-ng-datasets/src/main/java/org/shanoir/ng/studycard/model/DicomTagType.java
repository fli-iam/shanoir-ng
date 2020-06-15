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

	String, Long, Float, Double, Integer, Binary, Date;
	
	public static DicomTagType valueOf(VR vr) {
		if (vr.equals(VR.AE)
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
				|| vr.equals(VR.UN)) {
			return DicomTagType.Binary;
			
		} else if (vr.equals(VR.DA)
				|| vr.equals(VR.DT)
				|| vr.equals(VR.TM)) {
			return DicomTagType.Date;
			
		} else if (vr.equals(VR.DS)
				|| vr.equals(VR.FL)
				|| vr.equals(VR.OF)) {
			return DicomTagType.Float;
			
		} else if (vr.equals(VR.FD)
				|| vr.equals(VR.OD)) {
			return DicomTagType.Double;
			
		} else if (vr.equals(VR.IS)
				|| vr.equals(VR.SS)
				|| vr.equals(VR.US)) {
			return DicomTagType.Integer;
		
		} else if (vr.equals(VR.OL)
				|| vr.equals(VR.SL)
				|| vr.equals(VR.UL)) {
			return DicomTagType.Long;
		} else {
			LoggerFactory.getLogger(DicomTagType.class).error("VR with code " + vr.code() + " is not implemented");
			return null;
		}
	}
	
	public boolean isNumerical() {
		return this.equals(Date) || this.equals(Double) || this.equals(Float) || this.equals(Integer) || this.equals(Long);
	}
	
	public boolean isTextual() {
		return this.equals(String);
	}
}

