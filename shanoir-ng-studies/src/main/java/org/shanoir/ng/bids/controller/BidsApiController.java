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

package org.shanoir.ng.bids.controller;

import io.swagger.v3.oas.annotations.Parameter;
import org.shanoir.ng.bids.service.BIDSService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;

import java.io.IOException;

@Controller
public class BidsApiController implements BidsApi {

    @Autowired
    private BIDSService bidsService;

    @Override
    public ResponseEntity<ByteArrayResource> generateParticipantsTsvByStudyId(
            @Parameter(description = "id of the study", required = true) @PathVariable("studyId") Long studyId) throws IOException {
        return bidsService.generateParticipantsTsv(studyId);
    }
}
