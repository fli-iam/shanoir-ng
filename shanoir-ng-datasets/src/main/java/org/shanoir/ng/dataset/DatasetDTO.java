package org.shanoir.ng.dataset;

import java.time.LocalDate;
import java.util.List;
import org.shanoir.ng.shared.model.EchoTime;
import org.shanoir.ng.shared.model.FlipAngle;
import org.shanoir.ng.shared.model.InversionTime;
import org.shanoir.ng.shared.model.RepetitionTime;


/**
 * DTO for dataset.
 * 
 * @author msimon
 *
 */
public class DatasetDTO {

	private LocalDate creationDate;

	private Long groupOfSubjectsId;

	private Long id;

	private DatasetMetadataDTO originMetadata;

	private Long studyId;

	private Long subjectId;

	private DatasetMetadataDTO updatedMetadata;
	
	private String name;
	
	private String type;

	private List<EchoTime> echoTime;

	private List<FlipAngle> flipAngle;

	private List<InversionTime> inversionTime;
	
	private List<RepetitionTime> repetitionTime;


	/**
	 * @return the creationDate
	 */
	public LocalDate getCreationDate() {
		return creationDate;
	}

	/**
	 * @param creationDate
	 *            the creationDate to set
	 */
	public void setCreationDate(LocalDate creationDate) {
		this.creationDate = creationDate;
	}

	/**
	 * @return the groupOfSubjectsId
	 */
	public Long getGroupOfSubjectsId() {
		return groupOfSubjectsId;
	}

	/**
	 * @param groupOfSubjectsId
	 *            the groupOfSubjectsId to set
	 */
	public void setGroupOfSubjectsId(Long groupOfSubjectsId) {
		this.groupOfSubjectsId = groupOfSubjectsId;
	}

	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * @return the originMetadata
	 */
	public DatasetMetadataDTO getOriginMetadata() {
		return originMetadata;
	}

	/**
	 * @param originMetadata
	 *            the originMetadata to set
	 */
	public void setOriginMetadata(DatasetMetadataDTO originMetadata) {
		this.originMetadata = originMetadata;
	}

	/**
	 * @return the studyId
	 */
	public Long getStudyId() {
		return studyId;
	}

	/**
	 * @param studyId
	 *            the studyId to set
	 */
	public void setStudyId(Long studyId) {
		this.studyId = studyId;
	}

	/**
	 * @return the subjectId
	 */
	public Long getSubjectId() {
		return subjectId;
	}

	/**
	 * @param subjectId
	 *            the subjectId to set
	 */
	public void setSubjectId(Long subjectId) {
		this.subjectId = subjectId;
	}

	/**
	 * @return the updatedMetadata
	 */
	public DatasetMetadataDTO getUpdatedMetadata() {
		return updatedMetadata;
	}

	/**
	 * @param updatedMetadata
	 *            the updatedMetadata to set
	 */
	public void setUpdatedMetadata(DatasetMetadataDTO updatedMetadata) {
		this.updatedMetadata = updatedMetadata;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	

	public List<EchoTime> getEchoTime() {
		return echoTime;
	}

	public void setEchoTime(List<EchoTime> echoTime) {
		this.echoTime = echoTime;
	}

	public List<FlipAngle> getFlipAngle() {
		return flipAngle;
	}

	public void setFlipAngle(List<FlipAngle> flipAngle) {
		this.flipAngle = flipAngle;
	}

	public List<InversionTime> getInversionTime() {
		return inversionTime;
	}

	public void setInversionTime(List<InversionTime> inversionTime) {
		this.inversionTime = inversionTime;
	}

	public List<RepetitionTime> getRepetitionTime() {
		return repetitionTime;
	}

	public void setRepetitionTime(List<RepetitionTime> repetitionTime) {
		this.repetitionTime = repetitionTime;
	}
	
	
	

}
