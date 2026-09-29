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

package org.shanoir.ng.scheduling;

import java.time.LocalDate;
import java.util.List;

import org.shanoir.ng.email.EmailService;
import org.shanoir.ng.shared.configuration.RabbitMQConfiguration;
import org.shanoir.ng.shared.core.model.IdName;
import org.shanoir.ng.study.rights.StudyUser;
import org.shanoir.ng.study.rights.StudyUserRightsRepository;
import org.shanoir.ng.user.model.User;
import org.shanoir.ng.user.service.UserService;
import org.shanoir.ng.user.utils.KeycloakClient;
import org.shanoir.ng.utils.SecurityContextUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import jakarta.persistence.EntityNotFoundException;

/**
 * Scheduled tasks.
 *
 * @author msimon
 *
 */
@Component
public class ScheduledTasks {

    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(ScheduledTasks.class);

    @Autowired
    private EmailService emailService;

    @Autowired
    private UserService userService;

    @Autowired
    private StudyUserRightsRepository studyUserRightsRepository;

    @Autowired
    private KeycloakClient keycloakClient;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * Check users expiration date every day at 8am.
     */
    @Scheduled(cron = "0 0 8 * * ?") // 0 0 8 * * ? = Every day at 8am
    public void checkExpirationDate() {
        SecurityContextUtil.initAuthenticationContext("ROLE_ADMIN");
        checkGlobalAccountExpirationDate();
        checkStudyUserExpirationDate();
        SecurityContextUtil.clearAuthentication();
    }

    private void checkGlobalAccountExpirationDate() {
        // Get list of users who have to receive first expiration notification
        List<User> usersToNotify = userService.getUsersToReceiveFirstExpirationNotification();
        for (User userToNotify : usersToNotify) {
            userToNotify.setFirstExpirationNotificationSent(true);
            try {
                userService.updateExpirationNotification(userToNotify, true);
                emailService.notifyAccountWillExpire(userToNotify);
            } catch (Exception e) {
                LOG.error("Error to send first expiration notification", e);
            }
        }

        // Get list of users who have to receive second expiration notification
        usersToNotify = userService.getUsersToReceiveSecondExpirationNotification();
        for (User userToNotify : usersToNotify) {
            userToNotify.setSecondExpirationNotificationSent(true);
            try {
                userService.updateExpirationNotification(userToNotify, false);
                emailService.notifyAccountWillExpire(userToNotify);
            } catch (Exception e) {
                LOG.error("Error to send second expiration notification", e);
            }
        }
        // Get list of expired users to expire them in keycloak too
        usersToNotify = userService.getExpiredUsers();
        for (User userToExpire : usersToNotify) {
            keycloakClient.updateUser(userToExpire);
        }
    }

    private void checkStudyUserExpirationDate() {
        LocalDate today = LocalDate.now();
        LocalDate limit = today.plusWeeks(1);

        List<StudyUser> studyUsers = studyUserRightsRepository.findByExpirationDateBetween(today, limit);
        for (StudyUser studyUser : studyUsers) {
            try {
                String studyName = (String) this.rabbitTemplate.convertSendAndReceive(RabbitMQConfiguration.STUDY_NAME_QUEUE, studyUser.getStudyId());
                IdName study = new IdName(studyUser.getStudyId(), studyName);
                User user = userService.findById(studyUser.getUserId());
                if (user == null) {
                    throw new EntityNotFoundException("User with id " + studyUser.getUserId() + " not found");
                }
                emailService.notifyStudyUserWillExpire(study, user, studyUser.getExpirationDate());
                studyUser.setReceivedExpirationNotification(true);
                studyUserRightsRepository.save(studyUser);
            } catch (MailException e) {
                LOG.error("Error to send study-user with id {} expiration notification. Will try again tomorrow.", studyUser.getId(), e);
            } catch (AmqpException e) {
                LOG.error("Error to get study name from study id {}. Therefore study-user with id {} expiration notification will try again tomorrow.",
                        studyUser.getStudyId(), studyUser.getId(), e);
            } catch (EntityNotFoundException e) {
                LOG.error("Error to get user with id {}. Therefore study-user with id {} expiration notification will try again tomorrow.",
                        studyUser.getUserId(), studyUser.getId(), e);
            } catch (Exception e) {
                LOG.error("Error to send study-user with id {} expiration notification. Will try again tomorrow.", studyUser.getId(), e);
            }
        }

    }

}
