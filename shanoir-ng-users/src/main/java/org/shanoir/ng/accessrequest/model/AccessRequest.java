package org.shanoir.ng.accessrequest.model;

import org.shanoir.ng.shared.hateoas.HalEntity;
import org.shanoir.ng.user.model.User;

/**
 * This class is used by a user to ask an access to a given study.
 * There are two possibilities, directly using a study id if "visible publicly" or using a study key / "invitation".
 * @author jcome
 *
 */
public class AccessRequest extends HalEntity {

	private static final long serialVersionUID = 4662874539537675259L;

	private Long studyId;
	
	private User user;
	
	private String motivation;
	
	private String invitationKey;

	/**
	 * @return the studyId
	 */
	public Long getStudyId() {
		return studyId;
	}

	/**
	 * @param studyId the studyId to set
	 */
	public void setStudyId(Long studyId) {
		this.studyId = studyId;
	}

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

	/**
	 * @return the invitationKey
	 */
	public String getInvitationKey() {
		return invitationKey;
	}

	/**
	 * @param invitationKey the invitationKey to set
	 */
	public void setInvitationKey(String invitationKey) {
		this.invitationKey = invitationKey;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}
}
