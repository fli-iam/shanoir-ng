package org.shanoir.ng.study.controller.rest;

import java.util.List;
import java.util.ListIterator;

import org.shanoir.ng.study.model.Study;
import org.shanoir.ng.study.repository.StudyRepository;
import org.shanoir.ng.study.service.StudyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class StudyApiController implements StudyAPI{

    private static final Logger LOG = LoggerFactory.getLogger(StudyApiController.class);

    @Autowired
    private StudyService studyService;
	
	//@RequestMapping("/listOfStudies")	
	@ResponseBody
	public ResponseEntity<List<Study>> findStudies() {
		
		List<Study>  studies = studyService.findAll();
        if (studies.isEmpty()) {
        	System.out.println("Empty");
            return new ResponseEntity<List<Study>>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<List<Study>>(studies, HttpStatus.OK);
    }



}
