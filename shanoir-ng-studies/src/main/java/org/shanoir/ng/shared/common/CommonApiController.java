package org.shanoir.ng.shared.common;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
public class CommonApiController implements CommonApi {

	@Autowired
	private CommonService commonService;

	@Override
	public ResponseEntity<CommonIdNamesDTO> findStudySubjectCenterNamesByIds(
			@RequestBody final CommonIdsDTO commonIdDTO) {
		final CommonIdNamesDTO names = commonService.findByIds(commonIdDTO);
		if (names == null) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(names, HttpStatus.OK);

	}

}
