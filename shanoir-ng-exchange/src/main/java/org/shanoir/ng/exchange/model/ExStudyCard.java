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

package org.shanoir.ng.exchange.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * If the study card is already existing in Shanoir, the name is set,
 * if not the name == null. So we have to create a new study card in sh-ng.
 *
 * @author mkain
 *
 */
public class ExStudyCard {

    /**
     * If this name is set, an existing study card shall be used for data exchange.
     * This is in case of an import, the study card with the name shall be used.
     * So e.g. with ShUp I would set this name and this.studyCard == null.
     */
    @JsonProperty("name")
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * If the id == null a complete study card object as used within MS Datasets
     * shall be added here. This can be used by an export to write a study card
     * object into and to transfer it to another Shanoir server. The id is
     * null, as the new server will generate a new id for this study card during
     * the import.
     */
//    @JsonProperty("studyCard")
//    private StudyCard studyCard;

    //todo: extend here for depending objects to complete the export/import

}
