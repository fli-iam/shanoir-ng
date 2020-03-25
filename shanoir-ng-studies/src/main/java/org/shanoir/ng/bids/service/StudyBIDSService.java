package org.shanoir.ng.bids.service;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.study.model.Study;
import org.shanoir.ng.subject.model.Subject;
import org.springframework.stereotype.Service;

@Service
public interface StudyBIDSService {

	void createBidsFolder(Study createdStudy);

	void updateBidsFolder(Study studyToChange);

	File exportAsBids(Study studyToExport);

	void deleteBids(Long studyDeletedId);

	void deleteSubjectBids(Long subjectId);

	void updateSubjectBids(Long subjectId, Subject subject);

	File getStudyFolder(Study studyToCreate);

	File createBidsFolderFromScratch(Study study);

	List<Subject> participantsDeserializer(File participantsTsv) throws IOException, ShanoirException;
}
