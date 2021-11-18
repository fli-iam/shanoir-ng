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

package org.shanoir.ng.dicom.web;

import org.shanoir.ng.dicom.web.dto.Study;
import org.shanoir.ng.examination.dto.mapper.ExaminationMapper;
import org.shanoir.ng.examination.model.Examination;
import org.shanoir.ng.examination.service.ExaminationService;
import org.shanoir.ng.exporter.service.BIDSService;
import org.shanoir.ng.shared.event.ShanoirEventService;
import org.shanoir.ng.shared.repository.StudyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
public class StudiesApiController implements StudiesApi {

	private static final Logger LOG = LoggerFactory.getLogger(StudiesApiController.class);

	@Autowired
	private ExaminationMapper examinationMapper;

	@Autowired
	private ExaminationService examinationService;

	@Autowired
	BIDSService bidsService;

	@Autowired
	ShanoirEventService eventService;

	@Autowired
	StudyRepository studyRepository;

	@Override
	public ResponseEntity<Page<Study>> findExaminations(final Pageable pageable) {
		Page<Examination> examinations = examinationService.findPage(pageable, false);
		if (examinations.getContent().isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
//		return new ResponseEntity<>(examinationMapper.examinationsToExaminationDTOs(examinations), HttpStatus.OK);
		return null;
	}

}
