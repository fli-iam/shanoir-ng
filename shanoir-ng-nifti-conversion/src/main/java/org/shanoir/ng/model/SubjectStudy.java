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

package org.shanoir.ng.model;

import org.shanoir.ng.shared.core.model.IdName;

public class SubjectStudy {

    private IdName subject;

    private IdName study;

    public SubjectStudy(IdName subject, IdName study) {
        this.subject = subject;
        this.study = study;
    }

    /**
     * @return the subject
     */
    public IdName getSubject() {
        return subject;
    }

    /**
     * @param subject the subject to set
     */
    public void setSubject(IdName subject) {
        this.subject = subject;
    }

    /**
     * @return the study
     */
    public IdName getStudy() {
        return study;
    }

    /**
     * @param study the study to set
     */
    public void setStudy(IdName study) {
        this.study = study;
    }


}
