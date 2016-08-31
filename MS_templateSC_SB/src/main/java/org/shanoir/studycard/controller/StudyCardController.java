package org.shanoir.studycard.controller;

import org.shanoir.studycard.model.StudyCard;
import org.shanoir.studycard.service.StudyCardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Controller for study cards.
 * 
 * @author msimon
 *
 */
@Controller
public class StudyCardController {

	@Autowired
	private StudyCardService studyCardService;
	
	@RequestMapping("/")
	@ResponseBody
	public Iterable<StudyCard> findAll() {
		return studyCardService.findAll();
	}
	
	@RequestMapping("/{id}")
	@ResponseBody
    public StudyCard findById(@PathVariable String id) {
        return studyCardService.findById(Long.valueOf(id));
    }
    
	@RequestMapping(value = "/", method = RequestMethod.POST)
	@ResponseBody
    public StudyCard createStudyCard(@RequestBody StudyCard newStudyCard) {
		newStudyCard.setId(0);
        return studyCardService.save(newStudyCard);
    }
    
	@RequestMapping(value = "/{id}", method = RequestMethod.PUT)
	@ResponseBody
    public StudyCard updateStudyCard(@PathVariable String id, @RequestBody StudyCard studyCard) {
        return studyCardService.update(Long.valueOf(id), studyCard);
    }
    
	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
	@ResponseBody
    public void deleteStudyCard(@PathVariable String id) {
        studyCardService.deleteById(Long.valueOf(id));
    }
    
}
