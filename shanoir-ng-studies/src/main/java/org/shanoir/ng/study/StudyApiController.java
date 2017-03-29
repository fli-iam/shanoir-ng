package org.shanoir.ng.study;

import java.util.List;

import org.shanoir.ng.shared.exception.ShanoirStudiesException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import io.swagger.annotations.ApiParam;

@Controller
public class StudyApiController implements StudyApi {

	@Autowired
	private StudyService studyService;
	
	public ResponseEntity<List<Study>> findStudies() {

		List<Study> studies = studyService.findAll();
		if (studies.isEmpty()) {
			System.out.println("Empty");
			return new ResponseEntity<List<Study>>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<List<Study>>(studies, HttpStatus.OK);
	}
	
    public ResponseEntity<Void> deleteStudy(@ApiParam(value = "id of the study",required=true ) @PathVariable("studyId") Long studyId) {
    	try {
			studyService.deleteById(studyId);
		} catch (ShanoirStudiesException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return new ResponseEntity<Void>(HttpStatus.OK);
    }
    
    public ResponseEntity<List<Study>> findStudiesByUserId(@ApiParam(value = "id of the user",required=true ) @PathVariable("userId") Long userId) {
    	List<Study> userStudies = studyService.findAllForUser(userId);
        return new ResponseEntity<List<Study>>(userStudies, HttpStatus.OK);
    }

    public ResponseEntity<Study> findStudyById(@ApiParam(value = "id of the study",required=true ) @PathVariable("studyId") Long studyId) {
    	studyService.findById(studyId);
        return new ResponseEntity<Study>(HttpStatus.OK);
    }

    public ResponseEntity<Study> saveNewStudy(@ApiParam(value = "study to create" ,required=true ) @RequestBody Study study) {
    	studyService.createStudy(study);
        return new ResponseEntity<Study>(HttpStatus.OK);
    }

    public ResponseEntity<Void> updateStudy(@ApiParam(value = "id of the study",required=true ) @PathVariable("studyId") Long studyId,
        @ApiParam(value = "study to update" ,required=true ) @RequestBody Study study) {
    	studyService.update(study);
        return new ResponseEntity<Void>(HttpStatus.OK);
    }

}
