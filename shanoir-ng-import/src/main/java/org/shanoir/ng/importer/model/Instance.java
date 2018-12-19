package org.shanoir.ng.importer.model;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * This class represents an instance based on Dicom as used in Shanoir.
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
