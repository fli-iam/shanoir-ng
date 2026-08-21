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

package org.shanoir.ng.study.rights.ampq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.shanoir.ng.shared.configuration.RabbitMQConfiguration;
import org.shanoir.ng.shared.security.rights.StudyUserRight;
import org.shanoir.ng.study.rights.StudyUser;
import org.shanoir.ng.study.rights.StudyUserInterface;
import org.shanoir.ng.study.rights.StudyUserRightsRepository;
import org.shanoir.ng.study.rights.command.StudyUserCommand;
import org.shanoir.ng.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RabbitMqStudyUserService {

    private static final Logger LOG = LoggerFactory.getLogger(RabbitMqStudyUserService.class);

    @Autowired
    private StudyUserUpdateService service;

    @Autowired
    private StudyUserRightsRepository studyUserRightsRepository;

    @Autowired
    private ObjectMapper mapper;

    public void receiveStudyUsers(String commandArrStr) throws AmqpRejectAndDontRequeueException {
        StudyUserCommand[] commands;
        try {
            LOG.debug("Received study-user commands : {}", commandArrStr);

            SimpleModule module = new SimpleModule();
            module.addAbstractTypeMapping(StudyUserInterface.class, StudyUser.class);
            mapper.registerModule(module);

            commands = mapper.readValue(commandArrStr, StudyUserCommand[].class);
            service.processCommands(Arrays.asList(commands));
        } catch (Exception e) {
            throw new AmqpRejectAndDontRequeueException("Study User Update rejected !!!", e);
        }
    }

    @RabbitListener(queues = RabbitMQConfiguration.STUDY_I_CAN_ADMIN_QUEUE, containerFactory = "multipleConsumersFactory")
    @RabbitHandler
    @Transactional
    public List<Long> getStudiesICanAdmin(Long userId) {
        List<StudyUser> sus = Utils.toList(this.studyUserRightsRepository.findByUserIdAndRight(userId, StudyUserRight.CAN_ADMINISTRATE.getId()));
        if (CollectionUtils.isEmpty(sus)) {
            return null;
        }
        return sus.stream().map(StudyUser::getStudyId
        ).collect(Collectors.toList());
    }

    @RabbitListener(queues = RabbitMQConfiguration.STUDY_ADMINS_QUEUE, containerFactory = "multipleConsumersFactory")
    @RabbitHandler
    @Transactional
    public List<Long> getStudyAdmins(Long studyId) {
        List<StudyUser> admins = Utils.toList(this.studyUserRightsRepository.findByStudyIdAndRight(studyId, StudyUserRight.CAN_ADMINISTRATE.getId()));
        if (CollectionUtils.isEmpty(admins)) {
            return null;
        }
        return admins.stream().map(studyUser ->
            studyUser.getUserId()
        ).collect(Collectors.toList());
    }
}
