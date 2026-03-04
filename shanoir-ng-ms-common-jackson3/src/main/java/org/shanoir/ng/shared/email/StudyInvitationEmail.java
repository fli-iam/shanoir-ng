/**
 * Shanoir NG - Import, manage and share neuroimaging data
 * Copyright (C) 2009-2019 Inria - https://www.inria.fr/
 * Contact us on https://project.inria.fr/shanoir/
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/gpl-3.0.html
 */

package org.shanoir.ng.shared.email;

public class StudyInvitationEmail extends EmailBase {

    private String invitedMail = null;

    private String invitationIssuer = null;

    private String function = null;

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

    public String getFunction() {
        return function;
    }

    public void setFunction(String function) {
        this.function = function;
    }

}
