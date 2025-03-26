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

package org.shanoir.ng.download;

import java.util.ArrayList;
import java.util.List;

public class DatasetDownloadError {

    public static final String PARTIAL_FAILURE = "PARTIAL_FAILURE";
    public static final String ERROR = "ERROR";

    private List<String> messages= new ArrayList<>();

    private String status;

    public DatasetDownloadError(){
    }

    public DatasetDownloadError(String message, String status) {
        this.status = status;
        this.messages.add(message);
    }

    public void update(String message, String status) {
        this.messages.add(message);
        this.status = status;
    }

    public List<String> getMessages() {
        return messages;
    }

    public void setMessages(List<String> messages) {
        this.messages = messages;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
