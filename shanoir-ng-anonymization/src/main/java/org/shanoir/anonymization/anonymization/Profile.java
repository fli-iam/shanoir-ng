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

package org.shanoir.anonymization.anonymization;

import java.util.HashMap;
import java.util.Map;

public class Profile {

    private Integer profileColumn;

    private Map<String, String> anonymizationMap;

    public Profile(Integer profileColumn) {
        super();
        this.profileColumn = profileColumn;
        anonymizationMap = new HashMap<>();
    }

    public Integer getProfileColumn() {
        return profileColumn;
    }

    public Map<String, String> getAnonymizationMap() {
        return anonymizationMap;
    }

    public void setAnonymizationMap(Map<String, String> anonymizationMap) {
        this.anonymizationMap = anonymizationMap;
    }

}
