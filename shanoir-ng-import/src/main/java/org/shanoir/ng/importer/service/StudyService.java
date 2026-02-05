package org.shanoir.ng.importer.service;

import org.shanoir.ng.shared.configuration.RabbitMQConfiguration;
import org.shanoir.ng.shared.exception.ErrorModel;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class StudyService {

    private static final Logger LOG = LoggerFactory.getLogger(StudyService.class);

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public boolean isDraft(Long studyId) throws RestServiceException {
        try {
            String response = (String) rabbitTemplate.convertSendAndReceive(
                    RabbitMQConfiguration.STUDY_DRAFT_STATE_QUEUE,
                    String.valueOf(studyId)
            );

            if (response == null || "NOT_FOUND".equals(response)) {
                throw new RestServiceException(
                        new ErrorModel(HttpStatus.NOT_FOUND.value(), "Cannot find study.", null));
            }

            if ("ERROR".equals(response)) {
                throw new RestServiceException(
                        new ErrorModel(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Error checking study state.", null));
            }

            return Boolean.parseBoolean(response);
        } catch (AmqpException e) {
            LOG.error("Failed to communicate with Study MS", e);
            throw new RestServiceException(
                    new ErrorModel(HttpStatus.SERVICE_UNAVAILABLE.value(), "Study service unavailable.", null));
        }
    }
}
