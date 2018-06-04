package org.shanoir.ng.studyuser;

import java.io.Serializable;

/**
 * IdClass for @StudyUser.
 * 
 * @author msimon
 *
 */
public class StudyUserId implements Serializable {

	/**
	 * UID
	 */
	private static final long serialVersionUID = -1163841625556694657L;

	private Long studyId;
	private Long userId;

	public StudyUserId() {
	}

	public StudyUserId(final Long studyId, final Long userId) {
		this.studyId = studyId;
		this.userId = userId;
	}

	/**
	 * @return the studyId
	 */
	public Long getStudyId() {
		return studyId;
	}

	/**
	 * @return the userId
	 */
	public Long getUserId() {
		return userId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + studyId.intValue();
		result = prime * result + userId.intValue();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		StudyUserId other = (StudyUserId) obj;
		if (studyId != other.studyId)
			return false;
		if (userId != other.userId)
			return false;
		return true;
	}

}
