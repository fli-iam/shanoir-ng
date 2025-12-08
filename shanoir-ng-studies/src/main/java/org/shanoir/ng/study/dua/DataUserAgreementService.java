/**
 * Shanoir NG - Import, manage and share neuroimaging data
 * Copyright (C) 2009-2019 Inria - https://www.inria.fr/
 * Contact us on https://project.inria.fr/shanoir/
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/gpl-3.0.html
 */

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
        DataUserAgreement dataUserAgreement = repository.findById(duaId).orElseThrow();
        dataUserAgreement.setTimestampOfAccepted(new Date());
        repository.save(dataUserAgreement);
        StudyUser studyUser = repositoryStudyUser.findByUserIdAndStudy_Id(dataUserAgreement.getUserId(), dataUserAgreement.getStudy().getId());
        if (studyUser == null) {
            throw new ShanoirException("Could not validate the data user agreement acceptation. The user don't seems to be a member of the study.");
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

    public DataUserAgreement createDataUserAgreementForUserInStudy(Study study, Long userId) {
        DataUserAgreement dataUserAgreement = new DataUserAgreement();
        dataUserAgreement.setStudy(study);
        dataUserAgreement.setUserId(userId);
        return repository.save(dataUserAgreement);
    }

    public void deleteIncompleteDataUserAgreementForUserInStudy(Study study, Long userId) {
        DataUserAgreement dataUserAgreement = repository.findByUserIdAndStudy_IdAndTimestampOfAcceptedIsNull(userId, study.getId());
        if (dataUserAgreement != null) {
            repository.deleteById(dataUserAgreement.getId());
        }
    }

    public List<DataUserAgreement> findDUAByStudyId(Long studyId) {
        return repository.findByStudyId(studyId);
    }

    public DataUserAgreement findDUAByUserIdAndStudyId(Long userId, Long studyId) {
        return repository.findByUserIdAndStudy_IdAndTimestampOfAcceptedIsNull(userId, studyId);
    }

    public void deleteAll(Iterable<DataUserAgreement> duas) {
        this.repository.deleteAll(duas);
    }

    public void update(DataUserAgreement dua) {
        this.repository.save(dua);
    }

}
