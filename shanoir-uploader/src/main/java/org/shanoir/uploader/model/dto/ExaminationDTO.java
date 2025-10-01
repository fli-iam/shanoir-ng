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

package org.shanoir.uploader.model.dto;

import javax.xml.datatype.XMLGregorianCalendar;

import org.shanoir.uploader.ShUpConfig;
import org.shanoir.uploader.utils.Util;

public class ExaminationDTO {

    private Long id;

    private XMLGregorianCalendar examinationDate;

    private String comment;

    public ExaminationDTO(Long id, XMLGregorianCalendar examinationDate, String comment) {
        super();
        this.id = id;
        this.examinationDate = examinationDate;
        this.comment = comment;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public XMLGregorianCalendar getExaminationDate() {
        return examinationDate;
    }

    public void setExaminationDate(XMLGregorianCalendar examinationDate) {
        this.examinationDate = examinationDate;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String toString() {
        final String examinationDate = ShUpConfig.FORMATTER.format(Util.toDate(this.getExaminationDate()));
        return examinationDate + ", " + this.getComment() + " (id = " + this.getId() + ")";
    }

}
