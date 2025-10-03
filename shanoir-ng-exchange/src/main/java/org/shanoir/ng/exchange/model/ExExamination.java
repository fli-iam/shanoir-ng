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
 * If the examination is already existing in Shanoir, the id is set,
 * if not the id == null. So we have to create a new examination in sh-ng.
 *
 * @author mkain
 *
 */
public class ExExamination {

    /**
     * If this id is set, an existing exam shall be used for data exchange.
     * This is in case of an import, the exam with the id shall be used.
     * So e.g. with ShUp I would set this id and this.examination == null.
     */
    @JsonProperty("id")
    private Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    /**
     * If the id == null a complete examination object as used within MS Studies
     * shall be added here. This can be used by an export to write a examination
     * object into and to transfer it to another Shanoir server. The id is
     * null, as the new server will generate a new id for this examination during
     * the import.
     */
//    @JsonProperty("examination")
//    private Examination examination;

    //todo: complete here later the tree with Acquisition etc. but in another .json
    // outside to avoid the increase to extreme of the first json.

}
