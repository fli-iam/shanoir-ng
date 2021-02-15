package org.shanoir.ng.challenge;

import java.util.ArrayList;
import java.util.List;

import org.shanoir.ng.shared.core.model.IdName;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.study.dto.mapper.StudyMapper;
import org.shanoir.ng.study.model.Study;
import org.shanoir.ng.study.service.StudyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
public class ChallengeApiController implements ChallengeApi {
	
	@Autowired
	private StudyService studyService;

	@Autowired
	private StudyMapper studyMapper;

    @Override
	public	ResponseEntity<List<IdName>> findChallenges() throws RestServiceException {
		List<IdName> studiesDTO = new ArrayList<>();
		final List<Study> studies = studyService.findChallenges();
		if (studies.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		for (Study study : studies) {
			studiesDTO.add(studyMapper.studyToIdNameDTO(study));
		}
		return new ResponseEntity<>(studiesDTO, HttpStatus.OK);
    }

}
