package org.shanoir.challengeScores.controller;

import java.util.ArrayList;
import java.util.List;

import org.shanoir.challengeScores.data.access.service.PatientService;
import org.shanoir.challengeScores.data.model.Patient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import io.swagger.api.MetricApiController;
import io.swagger.model.Patients;

/**
 * Implement the logic for the generated Swagger server api : {@link MetricApiController}
 *
 * @author jlouis
 */
@Component
public class PatientApiDelegate {

	@Autowired
	private PatientService patientService;

	/**
	 * Constructor
	 */
	public PatientApiDelegate() {
	}


	public ResponseEntity<Void> deleteAll() {
		patientService.deleteAll();
		return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
	}


	public ResponseEntity<Void> savePatient(Long id, String name) {
		Patient patient = new Patient();
		patient.setId(id);
		patient.setName(name);
		patientService.save(patient);
		return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
	}


	public ResponseEntity<Void> updatePatients(Patients swaggerPatients) {
		List<Patient> patients = new ArrayList<Patient>();
		for (io.swagger.model.Patient swaggerPatient: swaggerPatients) {
			Patient patient = new Patient(swaggerPatient.getId().longValue());
			patient.setName(swaggerPatient.getName());
			patients.add(patient);
		}
		//patientService.deleteAll();
		patientService.saveAll(patients);
		return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
	}

}
