package org.shanoir.ng.study;

import java.util.List;

import org.shanoir.ng.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StudyServiceImpl implements StudyService{
	
	@Autowired
	private StudyRepository studyRepository;
	
	@Override
	public List<Study> findAll() {
		return Utils.toList(studyRepository.findAll());
	}
	
	public Study createStudy(Study study){
		
		Study newStudy = studyRepository.save(study);
		return newStudy;
	}
	
	@Override
	public Study update(Study study) {
		final Study studyDb = studyRepository.findOne(study.getId());
		studyDb.setName(study.getName());
		studyDb.setEndDate(study.getEndDate());
		studyDb.setClinical(study.isClinical());
		studyDb.setWithExamination(study.isWithExamination());
		studyDb.setVisibleByDefault(study.isVisibleByDefault());
		studyDb.setDownloadableByDefault(study.isDownloadableByDefault());
		studyDb.setRefStudyStatus(study.getRefStudyStatus());
	
		studyRepository.save(studyDb);
		
		return studyDb;
	}

}
