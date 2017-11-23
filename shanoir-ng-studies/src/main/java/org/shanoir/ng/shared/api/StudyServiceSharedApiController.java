package org.shanoir.ng.shared.api;


import org.shanoir.ng.study.dto.StudySubjectCenterNamesDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class StudyServiceSharedApiController implements StudyServiceSharedApi {

	@Autowired
	private StudyServiceShared studyServiceShared;
	
	
	@Override
	public ResponseEntity<StudySubjectCenterNamesDTO> findStudySubjectCenterNamesByIds(@PathVariable("studyId") final Long studyId, @PathVariable("subjectId") final Long subjectId,
			@PathVariable("centerId") final Long centerId) {
		final StudySubjectCenterNamesDTO names = studyServiceShared.findByIds(studyId, subjectId, centerId);
		if (names == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<StudySubjectCenterNamesDTO>(names, HttpStatus.OK);
		
	}
	

}
