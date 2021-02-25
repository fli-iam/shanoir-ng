package org.shanoir.ng.study.dua;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.shanoir.ng.messaging.StudyUserUpdateBroadcastService;
import org.shanoir.ng.shared.exception.MicroServiceCommunicationException;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.study.model.Study;
import org.shanoir.ng.study.model.StudyUser;
import org.shanoir.ng.study.repository.StudyUserRepository;
import org.shanoir.ng.study.rights.command.CommandType;
import org.shanoir.ng.study.rights.command.StudyUserCommand;
import org.shanoir.ng.utils.KeycloakUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * This service manages data user agreements. It returns a list of all DUAs waiting for one user
 * and accepts the DUA of one user for one study with a specific id. When accepted it broadcasts
 * the StudyUser update to the other microservices.
 * 
 * @author mkain
 *
 */
@Service
public class DataUserAgreementService {
	
	private static final Logger LOG = LoggerFactory.getLogger(DataUserAgreementService.class);

	@Autowired
	private DataUserAgreementRepository repository;
	
	@Autowired
	private StudyUserRepository repositoryStudyUser;
	
	@Autowired
	private StudyUserUpdateBroadcastService studyUserCom;
	
	public List<DataUserAgreement> getDataUserAgreementsByUserId(Long userId) {
		return repository.findByUserIdAndTimestampOfAcceptedIsNull(userId);
	}
	
	public void acceptDataUserAgreement(Long duaId) throws ShanoirException {
		Long userId = KeycloakUtil.getTokenUserId();
		DataUserAgreement dataUserAgreement = repository.findOne(duaId);
		// assure that only the user itself can accept its DUA
		if (dataUserAgreement.getUserId().compareTo(userId) == 0) {
			dataUserAgreement.setTimestampOfAccepted(new Date());
			repository.save(dataUserAgreement);
			StudyUser studyUser = repositoryStudyUser.findByUserIdAndStudy_Id(dataUserAgreement.getUserId(), dataUserAgreement.getStudy().getId());
			if (studyUser == null) {
				throw new ShanoirException("Could not validate the data user agreement acceptance. The user does not seem to be a member of the study.");
			}
			studyUser.setConfirmed(true);
			repositoryStudyUser.save(studyUser);
			// Send updates via RabbitMQ
			try {
				List<StudyUserCommand> commands = new ArrayList<>();
				commands.add(new StudyUserCommand(CommandType.UPDATE, studyUser));
				studyUserCom.broadcast(commands);
			} catch (MicroServiceCommunicationException e) {
				LOG.error("Could not transmit study-user update info through RabbitMQ");
			}
		} else {
			throw new ShanoirException("Could not validate the data user agreement acceptance. The user of the DUA and the used user for the request do not match.");
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
		if (dataUserAgreement != null) {
			repository.delete(dataUserAgreement.getId());
		}
	}

}