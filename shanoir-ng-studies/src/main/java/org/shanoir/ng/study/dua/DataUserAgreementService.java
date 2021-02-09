package org.shanoir.ng.study.dua;

import java.util.List;

import org.shanoir.ng.study.model.Study;
import org.shanoir.ng.study.model.StudyUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * @author mkain
 *
 */
@Service
public class DataUserAgreementService {

	@Autowired
	private DataUserAgreementRepository repository;
	
	public List<DataUserAgreement> getDataUserAgreementsByUserId(Long userId) {
		return repository.findByUserIdAndTimestampOfAcceptedIsNull(userId);
	}
	
	public void createDataUserAgreementsForStudy(Study study) {
		List<StudyUser> studyUserList = study.getStudyUserList();
		for (StudyUser studyUser : studyUserList) {
			DataUserAgreement dataUserAgreement = new DataUserAgreement();
			dataUserAgreement.setStudy(study);
			dataUserAgreement.setUserId(studyUser.getUserId());
			repository.save(dataUserAgreement);
		}
	}
	
	public void createDataUserAgreementForUserInStudy(Study study, Long userId) {
		DataUserAgreement dataUserAgreement = new DataUserAgreement();
		dataUserAgreement.setStudy(study);
		dataUserAgreement.setUserId(userId);
		repository.save(dataUserAgreement);
	}
	
	public void deleteIncompleteDataUserAgreementForUserInStudy(Study study, Long userId) {
		DataUserAgreement dataUserAgreement = repository.findByUserIdAndStudy_IdAndTimestampOfAcceptedIsNull(userId, study.getId());
		repository.delete(dataUserAgreement.getId());
	}

}