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
