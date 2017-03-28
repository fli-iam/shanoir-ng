package org.shanoir.ng.study;

import java.sql.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.ManyToOne;
import javax.persistence.PostLoad;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.validator.constraints.NotBlank;
import org.shanoir.ng.shared.hateoas.HalEntity;
import org.shanoir.ng.shared.hateoas.Links;
import org.shanoir.ng.shared.validation.EditableOnlyBy;
import org.shanoir.ng.shared.validation.Unique;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@Entity
@JsonPropertyOrder({ "_links", "id", "name" })
@GenericGenerator(name = "IdOrGenerate", strategy="org.shanoir.ng.shared.model.UseIdOrGenerate")
public class Study extends HalEntity {

	/** The Constant serialVersionUID. */
	// private static final long serialVersionUID = -8001079069163353926L;

	/**
	 * Init HATEOAS links
	 */
	@PostLoad
	public void initLinks() {
		this.addLink(Links.REL_SELF, "template/" + getId());
	}

	/** Name. */
	@NotBlank
	@Column(unique = true)
	@Unique
	@EditableOnlyBy(roles = { "ROLE_ADMIN", "ROLE_EXPERT" })
	private String name;

	/** Start date. */
	private Date startDate;

	/** End date. */
	private Date endDate;

	/** Is clinical. */
	@NotBlank
	private boolean clinical;

	/** Is with examination. */
	private boolean withExamination;

	/** Is visible by default. */
	private boolean isVisibleByDefault;

	/** Is with downloadable by default. */
	private boolean isDownloadableByDefault;

	/** Study Status. */
	@ManyToOne(fetch = FetchType.EAGER)
	private StudyStatus studyStatus;

	@Override
	@GeneratedValue(strategy = GenerationType.IDENTITY, generator = "IdOrGenerate")
	@GenericGenerator(name = "IdOrGenerate", strategy="org.shanoir.ng.shared.model.UseIdOrGenerate")
	public Long getId() {
		return super.getId();
	}
	
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the startDate
	 */
	public Date getStartDate() {
		return startDate;
	}

	/**
	 * @param startDate the startDate to set
	 */
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	/**
	 * @return the endDate
	 */
	public Date getEndDate() {
		return endDate;
	}

	/**
	 * @param endDate the endDate to set
	 */
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	/**
	 * @return the clinical
	 */
	public boolean isClinical() {
		return clinical;
	}

	/**
	 * @param clinical the clinical to set
	 */
	public void setClinical(boolean clinical) {
		this.clinical = clinical;
	}

	/**
	 * @return the withExamination
	 */
	public boolean isWithExamination() {
		return withExamination;
	}

	/**
	 * @param withExamination the withExamination to set
	 */
	public void setWithExamination(boolean withExamination) {
		this.withExamination = withExamination;
	}

	/**
	 * @return the isVisibleByDefault
	 */
	public boolean isVisibleByDefault() {
		return isVisibleByDefault;
	}

	/**
	 * @param isVisibleByDefault the isVisibleByDefault to set
	 */
	public void setVisibleByDefault(boolean isVisibleByDefault) {
		this.isVisibleByDefault = isVisibleByDefault;
	}

	/**
	 * @return the isDownloadableByDefault
	 */
	public boolean isDownloadableByDefault() {
		return isDownloadableByDefault;
	}

	/**
	 * @param isDownloadableByDefault the isDownloadableByDefault to set
	 */
	public void setDownloadableByDefault(boolean isDownloadableByDefault) {
		this.isDownloadableByDefault = isDownloadableByDefault;
	}

	/**
	 * @return the studyStatus
	 */
	public StudyStatus getStudyStatus() {
		return studyStatus;
	}

	/**
	 * @param studyStatus the studyStatus to set
	 */
	public void setStudyStatus(StudyStatus studyStatus) {
		this.studyStatus = studyStatus;
	}

}
