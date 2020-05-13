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

package org.shanoir.ng.exchange.model.dicom;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * This class represents a DICOM instance in Shanoir.
 * 
 * @author mkain
 */
public class Instance {

    @JsonProperty("sopInstanceUID")
    private String sopInstanceUID;
	
	@JsonProperty("instanceNumber")
    private String instanceNumber;
    
    @JsonProperty("referencedSOPClassUIDInFile")
    private String referencedSOPClassUIDInFile;

    @JsonProperty("referencedFileID")
    private String[] referencedFileID;
    
    public Instance() {}
    
    public Instance(Attributes attributes) {
    		this.sopInstanceUID = attributes.getString(Tag.SOPInstanceUID);
    		this.instanceNumber = attributes.getString(Tag.InstanceNumber);
    		this.referencedSOPClassUIDInFile = attributes.getString(Tag.ReferencedSOPClassUIDInFile);
    		this.referencedFileID = attributes.getStrings(Tag.ReferencedFileID);
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
	
}
