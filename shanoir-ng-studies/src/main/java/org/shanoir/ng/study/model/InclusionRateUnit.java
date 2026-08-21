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

public enum InclusionRateUnit {
    PER_DAY(1),
    PER_WEEK(2),
    PER_MONTH(3),
    PER_YEAR(4);

    private int id;

    private InclusionRateUnit(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static InclusionRateUnit getUnit(Integer id) {
        if (id == null) {
            return null;
        }
        for (InclusionRateUnit unit : InclusionRateUnit.values()) {
            if (id.equals(unit.getId())) {
                return unit;
            }
        }
        throw new IllegalArgumentException("No matching InclusionRateUnit for id " + id);
    }
}
