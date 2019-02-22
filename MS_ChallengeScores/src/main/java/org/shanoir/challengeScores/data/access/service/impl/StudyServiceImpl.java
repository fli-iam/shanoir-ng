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

/**
 *
 */
package org.shanoir.challengeScores.data.access.service.impl;

import org.shanoir.challengeScores.data.access.repository.StudyRepository;
import org.shanoir.challengeScores.data.access.service.StudyService;
import org.shanoir.challengeScores.data.model.Study;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author jlouis
 */
@Service
public class StudyServiceImpl implements StudyService {

	@Autowired
	private StudyRepository studyRepository;

	@Override
	public void save(Study study) {
		studyRepository.save(study);
	}

	@Override
	public void saveAll(Iterable<Study> studies) {
		studyRepository.save(studies);
	}

	@Override
	public void deleteAll() {
		studyRepository.deleteAll();
	}
}
