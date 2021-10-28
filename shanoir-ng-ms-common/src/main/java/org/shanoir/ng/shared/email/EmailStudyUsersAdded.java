package org.shanoir.ng.shared.email;

import java.util.List;

public class EmailStudyUsersAdded extends EmailBase {
	
	private List<Long> studyUsers;

	public List<Long> getStudyUsers() {
		return studyUsers;
	}

	public void setStudyUsers(List<Long> studyUsers) {
		this.studyUsers = studyUsers;
	}

}
