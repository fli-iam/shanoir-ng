package org.shanoir.ng.shared.email;

import org.shanoir.ng.shared.core.model.IdName;

public class StudyInvitationEmail extends EmailBase {
	
	private String invitedMail;
	
	public String getInvitedMail() {
		return invitedMail;
	}

	public void setInvitedMail(String invitedMail) {
		this.invitedMail = invitedMail;
	}
}
