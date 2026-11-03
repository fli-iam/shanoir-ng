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

package org.shanoir.ng.neurobagel.controller;

import java.util.List;

import org.shanoir.ng.neurobagel.dto.NeurobagelDatasetDescriptionDTO;
import org.shanoir.ng.neurobagel.dto.NeurobagelStudyDTO;
import org.shanoir.ng.neurobagel.service.NeurobagelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
public class NeurobagelApiController implements NeurobagelApi {

    @Autowired
    private NeurobagelService neurobagelService;

    @Override
    public ResponseEntity<List<NeurobagelStudyDTO>> findExportedStudies() {
        return new ResponseEntity<>(neurobagelService.findExportedStudies(), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<NeurobagelStudyDTO> findExportedStudy(Long studyId) {
        return neurobagelService.findExportedStudy(studyId)
                .map(study -> new ResponseEntity<>(study, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @Override
    public ResponseEntity<String> findParticipantsTsv(Long studyId) {
        return neurobagelService.findParticipantsTsv(studyId)
                .map(tsv -> ResponseEntity.ok().contentType(MediaType.parseMediaType(TSV + ";charset=UTF-8")).body(tsv))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @Override
    public ResponseEntity<String> getParticipantsDictionary() {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(neurobagelService.getParticipantsDictionary());
    }

    @Override
    public ResponseEntity<NeurobagelDatasetDescriptionDTO> findDatasetDescription(Long studyId) {
        return neurobagelService.findDatasetDescription(studyId)
                .map(description -> new ResponseEntity<>(description, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @Override
    public ResponseEntity<String> findImagingTsv(Long studyId) {
        return neurobagelService.findImagingTsv(studyId)
                .map(tsv -> ResponseEntity.ok().contentType(MediaType.parseMediaType(TSV + ";charset=UTF-8")).body(tsv))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

}
