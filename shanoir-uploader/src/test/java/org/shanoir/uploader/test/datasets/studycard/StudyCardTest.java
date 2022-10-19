package org.shanoir.uploader.test.datasets.studycard;

import java.util.List;

import org.apache.log4j.Logger;
import org.junit.jupiter.api.Test;
import org.shanoir.uploader.model.dto.StudyCardOnStudyResultDTO;
import org.shanoir.uploader.test.AbstractTest;
import org.shanoir.uploader.test.datasets.dicom.web.StowDicomSRTest;

public class StudyCardTest extends AbstractTest {

	private static Logger logger = Logger.getLogger(StowDicomSRTest.class);
	
	@Test
	public void applyStudyCardOnStudy() throws Exception {
		List<StudyCardOnStudyResultDTO> results = shUpClient.applyStudyCardOnStudy(Long.valueOf(2));
		logger.info(results);
    }

}
