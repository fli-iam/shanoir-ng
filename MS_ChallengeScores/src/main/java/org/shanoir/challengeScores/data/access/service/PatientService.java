package org.shanoir.challengeScores.data.access.service;

import org.shanoir.challengeScores.data.model.Patient;

/**
 * Service interface for patients.
 *
 * @author jlouis
 *
 */
public interface PatientService {

	void save(Patient patient);
	void saveAll(Iterable<Patient> patients);
	void deleteAll();
}
