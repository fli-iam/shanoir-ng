package io.swagger.api;

import org.shanoir.challengeScores.controller.PatientApiDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import io.swagger.annotations.ApiParam;
import io.swagger.model.Patients;

@javax.annotation.Generated(value = "class io.swagger.codegen.languages.SpringCodegen", date = "2016-10-11T09:18:12.164Z")

@Controller
public class PatientApiController implements PatientApi {

	@Autowired
	private PatientApiDelegate patientApiDelegate;

	public ResponseEntity<Void> deleteAllPatients() {
        return patientApiDelegate.deleteAll();
    }

	public ResponseEntity<Void> savePatient(
			@ApiParam(value = "id of the patient", required = true) @RequestParam(value = "id", required = true) Long id,
			@ApiParam(value = "name of the patient", required = true) @RequestParam(value = "name", required = true) String name) {

		return patientApiDelegate.savePatient(id, name);
	}

	public ResponseEntity<Void> updatePatients(@ApiParam(value = "the patients to save", required = true) @RequestBody Patients patients) {
		return patientApiDelegate.updatePatients(patients);
	}
}
