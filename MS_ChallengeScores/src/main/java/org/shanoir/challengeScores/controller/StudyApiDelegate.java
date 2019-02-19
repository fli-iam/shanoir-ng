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

package org.shanoir.challengeScores.controller;

import java.util.ArrayList;
import java.util.List;

import org.shanoir.challengeScores.data.access.service.StudyService;
import org.shanoir.challengeScores.data.model.Study;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import io.swagger.api.MetricApiController;
import io.swagger.model.Studies;

/**
 * Implement the logic for the generated Swagger server api : {@link MetricApiController}
 *
 * @author jlouis
 */
@Component
public class StudyApiDelegate {

	@Autowired
	private StudyService studyService;

	/**
	 * Constructor
	 */
	public StudyApiDelegate() {
	}


	public ResponseEntity<Void> deleteAll() {
		studyService.deleteAll();
		return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
	}


	public ResponseEntity<Void> saveStudy(Long id, String name) {
		Study study = new Study();
		study.setId(id);
		study.setName(name);
		studyService.save(study);
		return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
	}


	public ResponseEntity<Void> updateStudies(Studies swaggerStudies) {
		List<Study> studies = new ArrayList<Study>();
		for (io.swagger.model.Study swaggerStudy : swaggerStudies) {
			Study study = new Study(swaggerStudy.getId().longValue());
			study.setName(swaggerStudy.getName());
			studies.add(study);
		}
		//studyService.deleteAll();
		studyService.saveAll(studies);
		return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
	}
}
