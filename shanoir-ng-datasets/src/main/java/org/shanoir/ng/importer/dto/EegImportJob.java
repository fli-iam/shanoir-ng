package org.shanoir.ng.importer.dto;

import java.util.List;

import org.shanoir.ng.eeg.model.Channel;
import org.shanoir.ng.eeg.model.Event;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Communication object to carry EEG data to be loaded in Shanoir.
 * @author JcomeD
 *
 */
public class EegImportJob {

	/** Folder where source data is stored. */
	@JsonProperty("workFolder")
	private String workFolder;
	
	/** Corresponding examination. */
    @JsonProperty("examinationId")
    private Long examinationId;
    
    /** Corresponding study. */
    @JsonProperty("frontStudyId")
    private Long frontStudyId;
    
    /** Corresponding subject. */
    @JsonProperty("subjectId")
    private Long subjectId;
    
    /** Not mandatyory, acquisition equipement. */
    @JsonProperty("frontAcquisitionEquipmentId")
    private Long frontAcquisitionEquipmentId;

    /** List of channels for the eeg dataset. */
    @JsonProperty("channels")
    private List<Channel> channels;

    /** List of events for the eeg dataset. */
    @JsonProperty("events")
    private List<Event> events;

	/** Name of the file -> name of the dataset created. */
	@JsonProperty("name")
	private String name;
	
	@JsonProperty("files")
	private List<String> files;

	public List<String> getFiles() {
		return files;
	}

	public void setFiles(List<String> files) {
		this.files = files;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Long getSubjectId() {
		return subjectId;
	}

	public void setSubjectId(Long subjectId) {
		this.subjectId = subjectId;
	}
	
	public Long getExaminationId() {
		return examinationId;
	}

	public void setExaminationId(Long examinationId) {
		this.examinationId = examinationId;
	}

	public Long getFrontStudyId() {
		return frontStudyId;
	}

	public void setFrontStudyId(Long frontStudyId) {
		this.frontStudyId = frontStudyId;
	}

	public Long getFrontAcquisitionEquipmentId() {
		return frontAcquisitionEquipmentId;
	}

	public void setFrontAcquisitionEquipmentId(Long frontAcquisitionEquipmentId) {
		this.frontAcquisitionEquipmentId = frontAcquisitionEquipmentId;
	}

    public String getWorkFolder() {
		return workFolder;
	}

	public void setWorkFolder(String workFolder) {
		this.workFolder = workFolder;
	}
	
	public List<Channel> getChannels() {
		return channels;
	}
	
	public void setChannels(List<Channel> channels) {
		this.channels = channels;
	}
	
	public List<Event> getEvents() {
		return events;
	}
	
	public void setEvents(List<Event> events) {
		this.events = events;
	}
}
