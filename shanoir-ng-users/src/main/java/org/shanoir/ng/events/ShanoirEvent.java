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

package org.shanoir.ng.events;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

@Entity
@Table(name = "events",
        indexes = {
            @Index(name = "i_user_type", columnList = "userId, eventType"),
        }
    )
public class ShanoirEvent extends ShanoirEventLight {

    @Column(columnDefinition = "LONGTEXT")
    protected String report;

    public ShanoirEvent() {
        // Default empty constructor for json deserializer.
    }

    /**
     * @return the report
     */
    public String getReport() {
        return report;
    }

    /**
     * @param message the report to set
     */
    public void setReport(String report) {
        this.report = report;
    }

    /** also modifies the current object by setting report to null */
    public ShanoirEventLight toLightEvent() {
        ShanoirEventLight light = (ShanoirEventLight) this;
        light.setHasReport(getReport() != null && !getReport().isEmpty());
        setReport(null);
        return light;
    }

}
