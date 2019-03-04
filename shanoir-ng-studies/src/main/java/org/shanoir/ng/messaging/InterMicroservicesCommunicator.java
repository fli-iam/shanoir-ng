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

package org.shanoir.ng.messaging;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.shanoir.ng.studyuser.StudyUser;
import org.shanoir.ng.studyuser.StudyUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * RabbitMQ message receiver.
 *
 * @author msimon
 * @author mkain
 *
 */
public class InterMicroservicesCommunicator {
	
	private static final Logger LOG = LoggerFactory.getLogger(InterMicroservicesCommunicator.class);

	private static final String MS_USERS_TO_MS_STUDIES_USER_DELETE = "ms_users_to_ms_studies_user_delete";

	@Autowired
	private StudyUserRepository studyUserRepository;

    private CountDownLatch latch = new CountDownLatch(1);

    @RabbitListener(queues = MS_USERS_TO_MS_STUDIES_USER_DELETE)
	public void receiveUserDeleteMessage(final Long userId) {
		LOG.debug(" [x] Received User Delete Message: '" + userId + "'");
		List<StudyUser> studyUserList = studyUserRepository.findByUserId(userId);
		for (Iterator iterator = studyUserList.iterator(); iterator.hasNext();) {
			StudyUser studyUser = (StudyUser) iterator.next();
			studyUserRepository.delete(studyUser);
		}
		latch.countDown();
    }

    public CountDownLatch getLatch() {
        return latch;
    }

}
