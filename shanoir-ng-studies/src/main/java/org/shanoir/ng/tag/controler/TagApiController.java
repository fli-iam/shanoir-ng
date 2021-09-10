package org.shanoir.ng.tag.controler;

import java.util.List;

import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.tag.model.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;

import io.swagger.annotations.ApiParam;

public class TagApiController implements TagApi {

	@Override
	public ResponseEntity<List<Tag>> findTags (
			@ApiParam(value = "id of the subject study", required = true) @PathVariable("subjectId") Long subjectId) throws RestServiceException {
		return null;
	}

	@Override
	public ResponseEntity<List<Tag>> findTagsOfSubjectStudy(
			@ApiParam(value = "id of the subject study", required = true) @PathVariable("subjectStudyId") Long subjectStudyId) throws RestServiceException {
		return null;
	}

	@Override
	public ResponseEntity<List<Tag>> findTagsOfStudy(
			@ApiParam(value = "id of the study", required = true) @PathVariable("studyId") Long studyId) throws RestServiceException {
		return null;
	}
}
