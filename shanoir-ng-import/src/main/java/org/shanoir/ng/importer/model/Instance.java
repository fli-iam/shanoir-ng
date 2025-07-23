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

package org.shanoir.ng.importer.model;

import java.util.Arrays;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * This class represents a DICOM instance/IMAGE.
 * 
 * @author mkain
 */
public class Instance implements Cloneable {

	@JsonProperty("sopInstanceUID")
	private String sopInstanceUID;

	@JsonProperty("sopClassUID")
	private String sopClassUID;

	@JsonProperty("instanceNumber")
	private String instanceNumber;

	// Used in DICOMDIR to hold SOPInstanceUID
	@JsonProperty("referencedSOPInstanceUIDInFile")
	private String referencedSOPInstanceUIDInFile;

	// Used in DICOMDIR to hold SOPClassUID
	@JsonProperty("referencedSOPClassUIDInFile")
	private String referencedSOPClassUIDInFile;

	// Used in DICOMDIR to reference DICOM file
	@JsonProperty("referencedFileID")
	private String[] referencedFileID;

	public Instance() {}

	public Instance(Attributes attributes) {
		sopInstanceUID = attributes.getString(Tag.SOPInstanceUID);
		// try to remove confusing spaces, in case DICOM server sends them wrongly
		if (sopInstanceUID != null)
			sopInstanceUID = sopInstanceUID.trim();
		sopClassUID = attributes.getString(Tag.SOPClassUID);
		instanceNumber = attributes.getString(Tag.InstanceNumber);
		// below code applies to reading a DICOMDIR, not Q/R
		referencedSOPInstanceUIDInFile = attributes.getString(Tag.ReferencedSOPInstanceUIDInFile);
		if (referencedSOPInstanceUIDInFile != null && sopInstanceUID == null) {
			sopInstanceUID = referencedSOPInstanceUIDInFile;
		}
		referencedSOPClassUIDInFile = attributes.getString(Tag.ReferencedSOPClassUIDInFile);
		if (referencedSOPClassUIDInFile != null && sopClassUID == null) {
			sopClassUID = referencedSOPClassUIDInFile;
		}
		referencedFileID = attributes.getStrings(Tag.ReferencedFileID);
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	public String getInstanceNumber() {
		return instanceNumber;
	}

	public String getReferencedSOPClassUIDInFile() {
		return referencedSOPClassUIDInFile;
	}

	public String[] getReferencedFileID() {
		return referencedFileID;
	}

	public void setInstanceNumber(String instanceNumber) {
		this.instanceNumber = instanceNumber;
	}

	public void setReferencedSOPClassUIDInFile(String referencedSOPClassUIDInFile) {
		this.referencedSOPClassUIDInFile = referencedSOPClassUIDInFile;
	}

	public void setReferencedFileID(String[] referencedFileID) {
		this.referencedFileID = referencedFileID;
	}

	public String getSopInstanceUID() {
		return sopInstanceUID;
	}

	public void setSopInstanceUID(String sopInstanceUID) {
		this.sopInstanceUID = sopInstanceUID;
	}

	public String getSopClassUID() {
		return sopClassUID;
	}

	public String getReferencedSOPInstanceUIDInFile() {
		return referencedSOPInstanceUIDInFile;
	}

	@Override
	public String toString() {
		return "Instance [sopInstanceUID=" + sopInstanceUID + ", sopClassUID=" + sopClassUID + ", instanceNumber="
				+ instanceNumber + ", referencedSOPInstanceUIDInFile=" + referencedSOPInstanceUIDInFile
				+ ", referencedSOPClassUIDInFile=" + referencedSOPClassUIDInFile + ", referencedFileID="
				+ Arrays.toString(referencedFileID) + "]";
	}

}
