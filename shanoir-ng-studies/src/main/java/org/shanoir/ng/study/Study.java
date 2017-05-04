package org.shanoir.ng.study;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PostLoad;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.validator.constraints.NotBlank;
import org.shanoir.ng.shared.hateoas.HalEntity;
import org.shanoir.ng.shared.hateoas.Links;
import org.shanoir.ng.shared.validation.EditableOnlyBy;
import org.shanoir.ng.shared.validation.Unique;
import org.shanoir.ng.subject.Sex;
import org.shanoir.ng.subject.SubjectStudy;
import org.hibernate.validator.constraints.Length;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@Entity
@Table(name = "study")
@JsonPropertyOrder({ "_links", "id", "name" })
@GenericGenerator(name = "IdOrGenerate", strategy = "org.shanoir.ng.shared.model.UseIdOrGenerate")
public class Study extends HalEntity {

	/**
	 * UID
	 */
	private static final long serialVersionUID = 2182323766659913794L;

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
	@NotNull
	private boolean clinical;

	/** Is with examination. */
	private boolean withExamination;

	/** Is visible by default. */
	private boolean isVisibleByDefault;

	/** Is with downloadable by default. */
	private boolean isDownloadableByDefault;

	/** Users associated to the research study. */
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinColumn(name = "study")
	private List<RelStudyUser> relStudyUserList = new ArrayList<RelStudyUser>(0);

	@NotNull
	// ATO : @Column commented for this field
	//@Column(nullable = false, insertable = false, updatable = false)
	@Enumerated(EnumType.STRING)
	private StudyStatus studyStatus;

	/** Relations between the subjects and the studies. */

	@OneToMany(mappedBy = "study", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@Cascade( { org.hibernate.annotations.CascadeType.SAVE_UPDATE, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
	private List<SubjectStudy> subjectStudyList = new ArrayList<SubjectStudy>(0);

	@Override
	@GeneratedValue(strategy = GenerationType.IDENTITY, generator = "IdOrGenerate")
	@GenericGenerator(name = "IdOrGenerate", strategy = "org.shanoir.ng.shared.model.UseIdOrGenerate")
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
	 * @param name
	 *            the name to set
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
	 * @param startDate
	 *            the startDate to set
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
	 * @param endDate
	 *            the endDate to set
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
	 * @param clinical
	 *            the clinical to set
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
	 * @param withExamination
	 *            the withExamination to set
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
	 * @param isVisibleByDefault
	 *            the isVisibleByDefault to set
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
	 * @param isDownloadableByDefault
	 *            the isDownloadableByDefault to set
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
	 * @param studyStatus
	 *            the studyStatus to set
	 */
	public void setStudyStatus(StudyStatus studyStatus) {
		this.studyStatus = studyStatus;
	}

	/**
	 * Return the relStudyUserCollection as a list.
	 *
	 * @return the rel study user collection
	 */
	public List<RelStudyUser> getRelStudyUserList() {
		return relStudyUserList;
	}

	public void setRelStudyUserList(List<RelStudyUser> relStudyUserList) {
		this.relStudyUserList = relStudyUserList;
	}

	/**
	 * Init HATEOAS links
	 */
	@PostLoad
	public void initLinks() {
		this.addLink(Links.REL_SELF, "study/" + getId());
	}

	/*
	 * public static long getSerialversionuid() { return serialVersionUID; }
	 *
	 *
	 * /** Associated experimental groups of subjects.
	 */
	/*
	 * @AuditJoinTable
	 *
	 * @OneToMany(mappedBy = "study", fetch = FetchType.LAZY, cascade = {
	 * CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REMOVE })
	 *
	 * @JoinColumn(name = "STUDY_ID")
	 *
	 * @Cascade( { org.hibernate.annotations.CascadeType.SAVE_UPDATE,
	 * org.hibernate.annotations.CascadeType.DELETE_ORPHAN }) private
	 * List<ExperimentalGroupOfSubjects> experimentalGroupOfSubjectsList = new
	 * ArrayList<ExperimentalGroupOfSubjects>(0);
	 */
	/** Relations between the investigators, the centers and the studies. */
	/*
	 * @NotEmpty
	 *
	 * @OneToMany(fetch = FetchType.LAZY, mappedBy = "study", cascade =
	 * CascadeType.ALL)
	 *
	 * @JoinColumn(name = "STUDY_ID") private List<RelStudyCenter>
	 * relStudyCenterList = new ArrayList<RelStudyCenter>(0);
	 */

	/** Dataset list. */
	/*
	 * @OneToMany(cascade = CascadeType.ALL, mappedBy = "study")
	 *
	 * @Cascade( { org.hibernate.annotations.CascadeType.SAVE_UPDATE,
	 * org.hibernate.annotations.CascadeType.DELETE_ORPHAN }) private
	 * List<RelStudyDataset> relStudyDatasetList = new
	 * ArrayList<RelStudyDataset>(0);
	 */
	/** Users associated to the research study. */
	/*
	 * @OneToMany(cascade = CascadeType.ALL, mappedBy = "study", fetch =
	 * FetchType.EAGER)
	 *
	 * @Cascade( { org.hibernate.annotations.CascadeType.SAVE_UPDATE,
	 * org.hibernate.annotations.CascadeType.DELETE_ORPHAN }) private
	 * List<RelStudyUser> relStudyUserList = new ArrayList<RelStudyUser>(0);
	 */

	/** Relations between the subjects and the studies. */
	/*
	 * @AuditJoinTable
	 *
	 * @OneToMany(mappedBy = "study", fetch = FetchType.LAZY, cascade =
	 * CascadeType.ALL)
	 *
	 * @Cascade( { org.hibernate.annotations.CascadeType.SAVE_UPDATE,
	 * org.hibernate.annotations.CascadeType.DELETE_ORPHAN }) private
	 * List<RelSubjectStudy> relSubjectStudyList = new
	 * ArrayList<RelSubjectStudy>(0);
	 */
	/** Associated study card lists. */

	/*
	 * @OneToMany(mappedBy = "study", fetch = FetchType.LAZY, cascade = {
	 * CascadeType.MERGE, CascadeType.PERSIST })
	 *
	 * @JoinColumn(name = "STUDY_ID") private List<StudyCard> studyCardList =
	 * new ArrayList<StudyCard>(0);
	 */

	/** List of protocol files directly attached to the study. */
	/*
	 * @CollectionOfElements
	 *
	 * @JoinTable(name = "PROTOCOL_FILE_PATH", joinColumns = { @JoinColumn(name
	 * = "STUDY_ID", nullable = true) })
	 *
	 * @Column(name = "PATH") private List<String> protocolFilePathList = new
	 * ArrayList<String>();
	 */

}
