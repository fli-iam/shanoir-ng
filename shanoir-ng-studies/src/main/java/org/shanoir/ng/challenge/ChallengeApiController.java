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

package org.shanoir.ng.challenge;

import java.util.ArrayList;
import java.util.List;

import org.shanoir.ng.shared.core.model.IdName;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.study.dto.mapper.StudyMapper;
import org.shanoir.ng.study.model.Study;
import org.shanoir.ng.study.service.StudyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
public class ChallengeApiController implements ChallengeApi {

    @Autowired
    private StudyService studyService;

    @Autowired
    private StudyMapper studyMapper;

    @Override
    public    ResponseEntity<List<IdName>> findChallenges() throws RestServiceException {
        List<IdName> studiesDTO = new ArrayList<>();
        final List<Study> studies = studyService.findChallenges();
        if (studies.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        for (Study study : studies) {
            studiesDTO.add(studyMapper.studyToIdNameDTO(study));
        }
        return new ResponseEntity<>(studiesDTO, HttpStatus.OK);
    }

}
