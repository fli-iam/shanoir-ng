package org.shanoir.ng.study;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class StudyApiController implements StudyAPI {

	@Autowired
	private StudyService studyService;

	// @RequestMapping("/listOfStudies")
	@ResponseBody
	public ResponseEntity<List<Study>> findStudies() {

		List<Study> studies = studyService.findAll();
		if (studies.isEmpty()) {
			System.out.println("Empty");
			return new ResponseEntity<List<Study>>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<List<Study>>(studies, HttpStatus.OK);
	}

}
