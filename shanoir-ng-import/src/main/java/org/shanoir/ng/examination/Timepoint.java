package org.shanoir.ng.examination;

import javax.persistence.Entity;
import javax.persistence.PostLoad;
import javax.persistence.Table;

import org.shanoir.ng.shared.hateoas.HalEntity;
import org.shanoir.ng.shared.hateoas.Links;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Timepoint.
 * 
 * @author ifakhfakh
 *
 */
@Entity
@Table(name = "time_point")
@JsonPropertyOrder({ "_links", "id", "name","days","comment","rank","studyId" })
public class Timepoint extends HalEntity {

	private String name;
	private Long days;
	private String comment;
	private Long rank;
	private Long studyId;
	

	/**
	 * Init HATEOAS links
	 */
	@PostLoad
	public void initLinks() {
		this.addLink(Links.REL_SELF, "examination/" + getId());
	}


	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}


	public Long getDays() {
		return days;
	}


	public void setDays(Long days) {
		this.days = days;
	}


	public String getComment() {
		return comment;
	}


	public void setComment(String comment) {
		this.comment = comment;
	}


	public Long getRank() {
		return rank;
	}


	public void setRank(Long rank) {
		this.rank = rank;
	}


	public Long getStudyId() {
		return studyId;
	}


	public void setStudyId(Long studyId) {
		this.studyId = studyId;
	}

	

}
