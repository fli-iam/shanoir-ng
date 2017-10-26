package org.shanoir.ng.examination;

import javax.persistence.Entity;
import javax.validation.constraints.NotNull;

import org.shanoir.ng.shared.model.AbstractGenericItem;

/**
 * Timepoint.
 * 
 * @author ifakhfakh
 *
 */
@Entity
public class Timepoint extends AbstractGenericItem {

	/**
	 * UID
	 */
	private static final long serialVersionUID = 4573187089337515044L;

	/** Personnal comment. */
	private String comment;

	/** The days. */
	private Long days;

	/** The name. */
	@NotNull
	private String name;

	/** The rank. */
	@NotNull
	private Long rank;

	/** Study. */
	@NotNull
	private Long studyId;

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

}
