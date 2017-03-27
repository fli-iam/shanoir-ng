package org.shanoir.ng.study;

import java.io.Serializable;
import java.sql.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.PostLoad;
import javax.persistence.Table;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import org.shanoir.ng.shared.hateoas.HalEntity;
import org.shanoir.ng.shared.hateoas.Links;
import org.hibernate.validator.constraints.Length;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@Entity
@Table(name = "study")
@JsonPropertyOrder({ "_links", "id", "name" })
public class Study extends HalEntity {

	/** The Constant serialVersionUID. */
	//private static final long serialVersionUID = -8001079069163353926L;

	/**
	 * Init HATEOAS links
	 */
	@PostLoad
	public void initLinks() {
		this.addLink(Links.REL_SELF, "template/" + getId());
	}

	/** Name. */
	@Length(min = 0, max = 255)
	@Column(name = "NAME", unique = true, nullable = false, updatable = true)
	private String name;

	/** Start date. */
	@Column(name = "START_DATE", nullable = true, updatable = true)
	private Date startDate;

	/** End date. */
	@Column(name = "END_DATE", nullable = true, updatable = true)
	private Date endDate;


	/** Is clinical. */
	@Column(name = "IS_CLINICAL", nullable = false, updatable = true)
	private boolean clinical = false;

	/** Is with examination. */
	@Column(name = "IS_WITH_EXAMINATION", nullable = true, updatable = true)
	private boolean withExamination = true;

	/** Is visible by default. */
	@Column(name = "IS_VISIBLE_BY_DEFAULT", nullable = true, updatable = true)
	private boolean isVisibleByDefault = false;

	/** Is with downloadable by default. */
	@Column(name = "IS_DOWNLOADABLE_BY_DEFAULT", nullable = true, updatable = true)
	private boolean isDownloadableByDefault = false;

	/** Coordinator. */
	/*@ManyToOne
	@JoinColumn(name = "COORDINATOR_ID", referencedColumnName = "INVESTIGATOR_ID", nullable = true, updatable = true)
	private Investigator coordinator;*/

	/** Study Status. */
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "REF_STUDY_STATUS_ID", referencedColumnName = "REF_STUDY_STATUS_ID", nullable = true, updatable = true)
	private RefStudyStatus refStudyStatus;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public boolean isClinical() {
		return clinical;
	}

	public void setClinical(boolean clinical) {
		this.clinical = clinical;
	}

	public boolean isWithExamination() {
		return withExamination;
	}

	public void setWithExamination(boolean withExamination) {
		this.withExamination = withExamination;
	}

	public boolean isVisibleByDefault() {
		return isVisibleByDefault;
	}

	public void setVisibleByDefault(boolean isVisibleByDefault) {
		this.isVisibleByDefault = isVisibleByDefault;
	}

	public boolean isDownloadableByDefault() {
		return isDownloadableByDefault;
	}

	public void setDownloadableByDefault(boolean isDownloadableByDefault) {
		this.isDownloadableByDefault = isDownloadableByDefault;
	}

	public RefStudyStatus getRefStudyStatus() {
		return refStudyStatus;
	}

	public void setRefStudyStatus(RefStudyStatus refStudyStatus) {
		this.refStudyStatus = refStudyStatus;
	}

	/*public static long getSerialversionuid() {
		return serialVersionUID;
	}


	/** Associated experimental groups of subjects. */
	/*@AuditJoinTable
	@OneToMany(mappedBy = "study", fetch = FetchType.LAZY, cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REMOVE })
	@JoinColumn(name = "STUDY_ID")
	@Cascade( { org.hibernate.annotations.CascadeType.SAVE_UPDATE, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
	private List<ExperimentalGroupOfSubjects> experimentalGroupOfSubjectsList = new ArrayList<ExperimentalGroupOfSubjects>(0);
*/
	/** Relations between the investigators, the centers and the studies. */
	/*@NotEmpty
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "study", cascade = CascadeType.ALL)
	@JoinColumn(name = "STUDY_ID")
	private List<RelStudyCenter> relStudyCenterList = new ArrayList<RelStudyCenter>(0);*/

	/** Dataset list. */
	/*@OneToMany(cascade = CascadeType.ALL, mappedBy = "study")
	@Cascade( { org.hibernate.annotations.CascadeType.SAVE_UPDATE, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
	private List<RelStudyDataset> relStudyDatasetList = new ArrayList<RelStudyDataset>(0);
*/
	/** Users associated to the research study. */
	/*@OneToMany(cascade = CascadeType.ALL, mappedBy = "study", fetch = FetchType.EAGER)
	@Cascade( { org.hibernate.annotations.CascadeType.SAVE_UPDATE, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
	private List<RelStudyUser> relStudyUserList = new ArrayList<RelStudyUser>(0);*/

	/** Relations between the subjects and the studies. */
	/*@AuditJoinTable
	@OneToMany(mappedBy = "study", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@Cascade( { org.hibernate.annotations.CascadeType.SAVE_UPDATE, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
	private List<RelSubjectStudy> relSubjectStudyList = new ArrayList<RelSubjectStudy>(0);
*/
	/** Associated study card lists. */

	/*@OneToMany(mappedBy = "study", fetch = FetchType.LAZY, cascade = { CascadeType.MERGE, CascadeType.PERSIST })
	@JoinColumn(name = "STUDY_ID")
	private List<StudyCard> studyCardList = new ArrayList<StudyCard>(0);*/

	/** List of protocol files directly attached to the study. */
	/*@CollectionOfElements
	@JoinTable(name = "PROTOCOL_FILE_PATH", joinColumns = { @JoinColumn(name = "STUDY_ID", nullable = true) })
	@Column(name = "PATH")
	private List<String> protocolFilePathList = new ArrayList<String>();
*/



}
