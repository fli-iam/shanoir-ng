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

package org.shanoir.ng.study.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.shanoir.ng.messaging.StudyUserUpdateBroadcastService;
import org.shanoir.ng.shared.configuration.RabbitMQConfiguration;
import org.shanoir.ng.shared.event.ShanoirEvent;
import org.shanoir.ng.shared.security.rights.StudyUserRight;
import org.shanoir.ng.study.model.StudyUser;
import org.shanoir.ng.study.repository.StudyUserRepository;
import org.shanoir.ng.study.rights.command.CommandType;
import org.shanoir.ng.study.rights.command.StudyUserCommand;
import org.shanoir.ng.utils.KeycloakUtil;
import org.shanoir.ng.utils.SecurityContextUtil;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Implementation of study service.
 *
 * @author msimon
 *
 */
@Component
public class StudyUserServiceImpl implements StudyUserService {

    @Autowired
    private StudyUserRepository studyUserRepository;

    @Autowired
    private StudyUserUpdateBroadcastService studyUserUpdateBroadcastService;

    @Autowired
    private ObjectMapper mapper;

    @Override
    public List<StudyUserRight> getRightsForStudy(Long studyId) {
        Long userId = KeycloakUtil.getTokenUserId();
        StudyUser studyUser = studyUserRepository.findByUserIdAndStudy_Id(userId, studyId);
        if (studyUser != null) {
            return studyUser.getStudyUserRights();
        } else {
            return new ArrayList<>();
        }
    }

    @Override
    public Map<Long, List<StudyUserRight>> getRights() {
        Long userId = KeycloakUtil.getTokenUserId();
        List<StudyUser> studyUsers = studyUserRepository.findByUserId(userId);
        if (studyUsers != null) {
            Map<Long, List<StudyUserRight>> map = new HashMap<>();
            for (StudyUser studyUser : studyUsers) {
                map.put(studyUser.getStudyId(), studyUser.getStudyUserRights());
            }
            return map;
        } else {
            return new HashMap<>();
        }
    }

    @Override
    public boolean hasOneStudyToImport() {
        Long userId = KeycloakUtil.getTokenUserId();
        for (StudyUser studyUser : studyUserRepository.findByUserId(userId)) {
            if (studyUser.getStudyUserRights().contains(StudyUserRight.CAN_IMPORT) && studyUser.isConfirmed()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public List<StudyUser> findStudyUsersByStudyId(Long studyId) {
        List<StudyUser> studyUsers = studyUserRepository.findByStudy_Id(studyId);
        // two bags contraint on EntityGraph expression in findByStudy_Id: load centers manually
        studyUsers.stream().forEach(su -> su.setCenters(studyUserRepository.findDistinctCentersByStudyId(studyId)));
        return studyUsers;
    }

    @RabbitListener(queues = RabbitMQConfiguration.DELETE_USER_QUEUE, containerFactory = "singleConsumerFactory")
    @RabbitHandler
    @Transactional
    @Override
    public void deleteUser(String eventAsString) throws AmqpRejectAndDontRequeueException {
        SecurityContextUtil.initAuthenticationContext("ROLE_ADMIN");
        try {
            ShanoirEvent event = mapper.readValue(eventAsString, ShanoirEvent.class);
            Long userId = Long.valueOf(event.getObjectId());
            List<StudyUser> sus =  this.studyUserRepository.findByUserId(userId);
            List<StudyUserCommand> commands = new ArrayList<>();
            for (StudyUser su : sus) {
                StudyUserCommand command = new StudyUserCommand(CommandType.DELETE, su.getId());
                commands.add(command);
            }
            this.studyUserUpdateBroadcastService.broadcast(commands);
            this.studyUserRepository.deleteAll(sus);
        } catch (Exception e) {
            throw new AmqpRejectAndDontRequeueException(e);
        }
    }
}
