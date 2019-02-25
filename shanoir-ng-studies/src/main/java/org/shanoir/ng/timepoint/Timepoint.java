package org.shanoir.ng.timepoint;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.GenericGenerator;
import org.shanoir.ng.shared.model.AbstractGenericItem;
import org.shanoir.ng.study.model.Study;

/**
 * Time point.
 * 
 * @author msimon
 *
 */
@Entity
@GenericGenerator(name = "IdOrGenerate", strategy = "org.shanoir.ng.shared.model.UseIdOrGenerate")
public class Timepoint extends AbstractGenericItem {

	/**
	 * UID
	 */
	private static final long serialVersionUID = 7218326283361790541L;

	/** Personnal comment. */
	private String comment;

	/** The name. */
	private Long days;

	/** The name. */
	@NotNull
	private String name;

	/** The rank. */
	@NotNull
	private Long rank;

	/** Study. */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "study_id")
	@NotNull
	private Study study;

	/**
	 * @return the comment
	 */
	public String getComment() {
		return comment;
	}

	/**
	 * @param comment
	 *            the comment to set
	 */
	public void setComment(String comment) {
		this.comment = comment;
	}

	/**
	 * @return the days
	 */
	public Long getDays() {
		return days;
	}

	/**
	 * @param days
	 *            the days to set
	 */
	public void setDays(Long days) {
		this.days = days;
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
	 * @return the rank
	 */
	public Long getRank() {
		return rank;
	}

	/**
	 * @param rank
	 *            the rank to set
	 */
	public void setRank(Long rank) {
		this.rank = rank;
	}

	/**
	 * @return the study
	 */
	public Study getStudy() {
		return study;
	}

	/**
	 * @param study
	 *            the study to set
	 */
	public void setStudy(Study study) {
		this.study = study;
	}

}
