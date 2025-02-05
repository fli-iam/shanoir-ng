package org.shanoir.uploader.test.datasets.studycard;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.jupiter.api.Test;
import org.shanoir.uploader.model.dto.StudyCardOnStudyResultDTO;
import org.shanoir.uploader.test.AbstractTest;
import org.shanoir.uploader.test.datasets.dicom.web.StowDicomSRTest;

public class StudyCardTest extends AbstractTest {

	private static final Logger logger = LoggerFactory.getLogger(StudyCardTest.class);
	
	@Test
	public void applyStudyCardOnStudy() throws Exception {
		// @todo here: create SC example, import and verify apply after
//		List<StudyCardOnStudyResultDTO> results = shUpClient.applyStudyCardOnStudy(Long.valueOf(2));
//		logger.info(results);
    }

}
