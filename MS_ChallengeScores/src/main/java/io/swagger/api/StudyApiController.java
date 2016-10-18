package io.swagger.api;

import org.shanoir.challengeScores.controller.StudyApiDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import io.swagger.annotations.ApiParam;
import io.swagger.model.Studies;

@javax.annotation.Generated(value = "class io.swagger.codegen.languages.SpringCodegen", date = "2016-10-11T09:18:12.164Z")

@Controller
public class StudyApiController implements StudyApi {

	@Autowired
	StudyApiDelegate studyApiDelegate;

	public ResponseEntity<Void> deleteAllStudies() {
        return studyApiDelegate.deleteAll();
    }

	public ResponseEntity<Void> saveStudy(
			@ApiParam(value = "id of the study", required = true) @RequestParam(value = "id", required = true) Long id,
			@ApiParam(value = "name of the study", required = true) @RequestParam(value = "name", required = true) String name) {

		return studyApiDelegate.saveStudy(id, name);
	}

	public ResponseEntity<Void> updateStudies(@ApiParam(value = "the studies to save", required = true) @RequestBody Studies studies) {
		return studyApiDelegate.updateStudies(studies);
	}
}
