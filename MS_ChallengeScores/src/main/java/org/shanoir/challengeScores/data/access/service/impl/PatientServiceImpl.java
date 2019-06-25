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

import org.shanoir.challengeScores.data.access.repository.PatientRepository;
import org.shanoir.challengeScores.data.access.service.PatientService;
import org.shanoir.challengeScores.data.model.Patient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author jlouis
 */
@Service
public class PatientServiceImpl implements PatientService {

	@Autowired
	private PatientRepository patientRepository;

	@Override
	public void save(Patient patient) {
		patientRepository.save(patient);
	}

	@Override
	public void saveAll(Iterable<Patient> patients) {
		patientRepository.save(patients);
	}

	@Override
	public void deleteAll() {
		patientRepository.deleteAll();
	}
}
