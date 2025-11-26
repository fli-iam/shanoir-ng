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

package org.shanoir.ng.shared.quality;

public enum QualityTag {

    VALID(1),
    WARNING(2),
    ERROR(3);

    private int id;

    private QualityTag(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public static QualityTag get(final Integer id) {
        if (id == null) {
            return null;
        }
        for (QualityTag tag : QualityTag.values()) {
            if (id.equals(tag.getId())) {
                return tag;
            }
        }
        throw new IllegalArgumentException("No matching calibration dataset type for id " + id);
    }

}
