/**
 * 
 */
package org.shanoir.ng.solr.model;

import java.time.LocalDate;

import org.springframework.data.annotation.Id;
import org.springframework.data.solr.core.mapping.Indexed;
import org.springframework.data.solr.core.mapping.SolrDocument;

/**
 * @author yyao
 *
 */
@SolrDocument(solrCoreName = "shanoir")
public class ShanoirDocument {
	
	@Id
	@Indexed(name="datasetId", type="string")
	private	String datasetId;
	
	@Indexed(name="datasetName", type="string")
	private	String datasetName;
	
	@Indexed(name="datasetType", type="string")
	private	String datasetType;
	
	@Indexed(name="datasetNature", type="string")
	private String datasetNature;
	
	@Indexed(name="datasetCreationDate", type="Date")
	private LocalDate datasetCreationDate;
	
	@Indexed(name="examinationComment", type="string")
	private String examinationComment;
	
	@Indexed(name="examinationDate", type="Date") 
	private LocalDate examinationDate;
	
	@Indexed(name="subjectName", type="string")
	private String subjectName;
	
	@Indexed(name="studyName", type="string")
	private String studyName;
	
	@Indexed(name="studyId", type="string")
	private String studyId;
	
	/**
	 * @return the datasetId
	 */
	public String getDatasetId() {
		return datasetId;
	}

	/**
	 * @param datasetId the datasetId to set
	 */
	public void setDatasetId(String datasetId) {
		this.datasetId = datasetId;
	}

	/**
	 * @return the datasetName
	 */
	public String getDatasetName() {
		return datasetName;
	}

	/**
	 * @param datasetName the datasetName to set
	 */
	public void setDatasetName(String datasetName) {
		this.datasetName = datasetName;
	}

	/**
	 * @return the datasetType
	 */
	public String getDatasetType() {
		return datasetType;
	}

	/**
	 * @param datasetType the datasetType to set
	 */
	public void setDatasetType(String datasetType) {
		this.datasetType = datasetType;
	}

	/**
	 * @return the datasetNature
	 */
	public String getDatasetNature() {
		return datasetNature;
	}

	/**
	 * @param datasetNature the datasetNature to set
	 */
	public void setDatasetNature(String datasetNature) {
		this.datasetNature = datasetNature;
	}

	/**
	 * @return the datasetCreationDate
	 */
	public LocalDate getDatasetCreationDate() {
		return datasetCreationDate;
	}

	/**
	 * @param datasetCreationDate the datasetCreationDate to set
	 */
	public void setDatasetCreationDate(LocalDate datasetCreationDate) {
		this.datasetCreationDate = datasetCreationDate;
	}

	/**
	 * @return the examinationComment
	 */
	public String getExaminationComment() {
		return examinationComment;
	}

	/**
	 * @param examinationComment the examinationComment to set
	 */
	public void setExaminationComment(String examinationComment) {
		this.examinationComment = examinationComment;
	}

	/**
	 * @return the examinationDate
	 */
	public LocalDate getExaminationDate() {
		return examinationDate;
	}

	/**
	 * @param examinationDate the examinationDate to set
	 */
	public void setExaminationDate(LocalDate examinationDate) {
		this.examinationDate = examinationDate;
	}

	/**
	 * @return the subjectName
	 */
	public String getSubjectName() {
		return subjectName;
	}

	/**
	 * @param subjectName the subjectName to set
	 */
	public void setSubjectName(String subjectName) {
		this.subjectName = subjectName;
	}

	/**
	 * @return the studyName
	 */
	public String getStudyName() {
		return studyName;
	}

	/**
	 * @param studyName the studyName to set
	 */
	public void setStudyName(String studyName) {
		this.studyName = studyName;
	}

	/**
	 * @return the studyId
	 */
	public String getStudyId() {
		return studyId;
	}

	/**
	 * @param studyId the studyId to set
	 */
	public void setStudyId(String studyId) {
		this.studyId = studyId;
	}

}