package org.shanoir.ng.shared.email;

public class StudyInvitationEmail extends EmailBase {
	
	private String invitationKey;
	
	private String invitedMail;

	public String getInvitationKey() {
		return invitationKey;
	}

	public void setInvitationKey(String invitationKey) {
		this.invitationKey = invitationKey;
	}

	public String getInvitedMail() {
		return invitedMail;
	}

	public void setInvitedMail(String invitedMail) {
		this.invitedMail = invitedMail;
	}

}
