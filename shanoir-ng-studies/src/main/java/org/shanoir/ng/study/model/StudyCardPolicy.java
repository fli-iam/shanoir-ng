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

package org.shanoir.ng.study.model;

public enum StudyCardPolicy {

    /**
     * Study card is mandatory during import
     */
    MANDATORY(1),

    /**
     * Study card is disabled during import
     */
    DISABLED(2);

    private int id;

    /**
     * Constructor.
     *
     * @param id
     *            id
     */
    private StudyCardPolicy(final int id) {
        this.id = id;
    }

    /**
     * Get the study card policy type by its id.
     *
     * @param id
     *            type id.
     * @return dataset modality type.
     */
    public static StudyCardPolicy getType(final Integer id) {
        if (id == null) {
            return null;
        }
        for (StudyCardPolicy type : StudyCardPolicy.values()) {
            if (id.equals(type.getId())) {
                return type;
            }
        }
        throw new IllegalArgumentException("No matching study card policy type for id " + id);
    }

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

}
