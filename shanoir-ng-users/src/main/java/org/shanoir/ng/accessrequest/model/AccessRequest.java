package org.shanoir.ng.accessrequest.model;

import javax.persistence.Entity;
import javax.persistence.Transient;

import org.shanoir.ng.shared.core.model.IdName;
import org.shanoir.ng.shared.hateoas.HalEntity;
import org.shanoir.ng.user.model.User;

/**
 * This class is used by a user to ask an access to a given study.
 * There are two possibilities, directly using a study id if "visible publicly" or using a study key / "invitation".
 * @author jcome
 *
 */
@Entity
public class AccessRequest extends HalEntity {

	private static final long serialVersionUID = 4662874539537675259L;

	@Transient
	private IdName study;
	
	private Long studyId;
	
	private User user;
	
	private String motivation;

	private Boolean status;

	/**
	 * @return the motivation
	 */
	public String getMotivation() {
		return motivation;
	}

	/**
	 * @param motivation the motivation to set
	 */
	public void setMotivation(String motivation) {
		this.motivation = motivation;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Boolean getStatus() {
		return status;
	}

	public void setStatus(Boolean status) {
		this.status = status;
	}

	public IdName getStudy() {
		return study;
	}

	public void setStudy(IdName study) {
		this.study = study;
		this.studyId = this.study.getId();
	}

	public Long getStudyId() {
		return studyId;
	}

	public void setStudyId(Long studyId) {
		this.studyId = studyId;
	}
}
