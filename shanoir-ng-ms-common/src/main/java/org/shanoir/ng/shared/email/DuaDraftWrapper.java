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

public class DuaDraftWrapper {

	private String recipienEmailAddress;

	private Long senderUserId;

	private String duaLink;

    private String studyName;


    public String getRecipienEmailAddress() {
        return recipienEmailAddress;
    }

    public void setRecipienEmailAddress(String recipienEmailAddress) {
        this.recipienEmailAddress = recipienEmailAddress;
    }

    public Long getSenderUserId() {
        return senderUserId;
    }

    public void setSenderUserId(Long senderUserId) {
        this.senderUserId = senderUserId;
    }

    public String getDuaLink() {
        return duaLink;
    }

    public void setDuaLink(String duaLink) {
        this.duaLink = duaLink;
    }

    public String getStudyName() {
        return studyName;
    }

    public void setStudyName(String studyName) {
        this.studyName = studyName;
    }
}
