package org.shanoir.uploader.test.datasets.studycard;

import org.apache.log4j.Logger;
import org.junit.jupiter.api.Test;
import org.shanoir.uploader.test.AbstractTest;

public class StudyCardTest extends AbstractTest {

	private static Logger logger = Logger.getLogger(StudyCardTest.class);
	
	@Test
	public void applyStudyCardOnStudy() throws Exception {
        shUpClient.applyStudyCardOnStudy(Long.valueOf(2));
    }
}
