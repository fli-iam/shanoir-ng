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

package org.shanoir.ng.email;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.shanoir.ng.email.model.RecipientGroup;
import org.shanoir.ng.shared.configuration.RabbitMQConfiguration;
import org.shanoir.ng.shared.exception.SecurityException;
import org.shanoir.ng.study.rights.StudyUser;
import org.shanoir.ng.study.rights.StudyUserRightsRepository;
import org.shanoir.ng.user.model.User;
import org.shanoir.ng.user.repository.UserRepository;
import org.shanoir.ng.user.utils.KeycloakClient;
import org.shanoir.ng.utils.KeycloakUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Implementation of mass email recipient resolution.
 *
 * @author afragkiadakis
 */
@Component
public class MassEmailServiceImpl implements MassEmailService {

    private static final Logger LOG = LoggerFactory.getLogger(MassEmailServiceImpl.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private KeycloakClient keycloakClient;

    @Autowired
    private StudyUserRightsRepository studyUserRightsRepository;

    @Autowired
    private EmailService emailService;

    @Override
    public List<User> resolveRecipients(final RecipientGroup recipientGroup) throws SecurityException {
        final List<User> emailableUsers = new ArrayList<>();
        for (User user : userRepository.findAll()) {
            if (isEmailable(user)) {
                emailableUsers.add(user);
            }
        }
        if (RecipientGroup.ALL == recipientGroup) {
            return emailableUsers;
        }
        final Map<String, Boolean> enabledByKeycloakId = keycloakClient.getUsersEnabledStatus();
        final List<User> recipients = new ArrayList<>();
        for (User user : emailableUsers) {
            if (isActive(user, enabledByKeycloakId) == (RecipientGroup.ACTIVE == recipientGroup)) {
                recipients.add(user);
            }
        }
        return recipients;
    }

    @Override
    public List<User> resolveStudyRecipients(final Long studyId) {
        final List<Long> memberIds = new ArrayList<>();
        for (StudyUser studyUser : studyUserRightsRepository.findByStudyId(studyId)) {
            memberIds.add(studyUser.getUserId());
        }
        final List<User> recipients = new ArrayList<>();
        if (memberIds.isEmpty()) {
            return recipients;
        }
        for (User user : userRepository.findAllById(memberIds)) {
            if (isEmailable(user)) {
                recipients.add(user);
            }
        }
        return recipients;
    }

    @Override
    public int countRecipients(final RecipientGroup recipientGroup) throws SecurityException {
        return resolveRecipients(recipientGroup).size();
    }

    @Override
    public User getSender() {
        final Long senderId = KeycloakUtil.getTokenUserId();
        return senderId == null ? null : userRepository.findById(senderId).orElse(null);
    }

    @Override
    public String getStudyName(final Long studyId) {
        try {
            return (String) rabbitTemplate.convertSendAndReceive(RabbitMQConfiguration.STUDY_NAME_QUEUE, studyId);
        } catch (Exception e) {
            // the email is still worth sending, only without the study name
            LOG.error("Could not read the name of the study {} from the studies microservice", studyId, e);
            return null;
        }
    }

    @Override
    @Async("massEmailExecutor")
    public void sendMassEmail(final List<User> recipients, final String subject, final String content,
            final User sender, final String studyName) {
        emailService.sendMassEmail(recipients, subject, content, sender, studyName);
    }

    /**
     * Users without an email address cannot be reached and users with a
     * pending account request were never approved: both are excluded from
     * every recipient group.
     */
    private boolean isEmailable(final User user) {
        return user.getEmail() != null && !user.getEmail().isBlank()
                && (user.isAccountRequestDemand() == null || !user.isAccountRequestDemand());
    }

    private boolean isActive(final User user, final Map<String, Boolean> enabledByKeycloakId) {
        return user.getKeycloakId() != null
                && Boolean.TRUE.equals(enabledByKeycloakId.get(user.getKeycloakId()));
    }

}
