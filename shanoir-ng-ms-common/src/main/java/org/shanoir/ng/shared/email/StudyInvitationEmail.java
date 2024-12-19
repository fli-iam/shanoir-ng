package org.shanoir.ng.shared.email;

public class StudyInvitationEmail extends EmailBase {
	
	private String invitedMail = null;

	private String invitationIssuer = null;

	private String role = null;
	
	public String getInvitedMail() {
		return invitedMail;
	}

	public void setInvitedMail(String invitedMail) {
		this.invitedMail = invitedMail;
	}

    public String getInvitationIssuer() {
        return invitationIssuer;
    }

    public void setInvitationIssuer(String invitationIssuer) {
        this.invitationIssuer = invitationIssuer;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

}
