package org.shanoir.ng.bids.service;

import java.io.File;
import org.shanoir.ng.study.model.Study;
import org.springframework.stereotype.Service;

@Service
public interface StudyBIDSService {

	void createBidsFolder(Study createdStudy);

	void updateBidsFolder(Study studyToChange);

	File exportAsBids(Study studyToExport);

	void deleteBids(Study studyDeleted);

	void deleteSubjectBids(Long subjectId);

	File getStudyFolder(Study studyToCreate);

	File createBidsFolderFromScratch(Study study);

}
