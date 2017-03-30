package org.shanoir.ng.configuration.amqp;

import java.util.concurrent.CountDownLatch;

import org.shanoir.ng.shared.exception.ShanoirStudyCardsException;
import org.shanoir.ng.studyCards.StudyCard;
import org.shanoir.ng.studyCards.StudyCardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.gson.Gson;

/**
 * RabbitMQ message receiver.
 * 
 * @author msimon
 *
 */
public class RabbitMqReceiver {
	private static final Logger LOG = LoggerFactory.getLogger(RabbitMqReceiver.class);

	@Autowired
	private StudyCardService studyCardService;

	private CountDownLatch latch = new CountDownLatch(1);

	/**
	 * Receive message.
	 * 
	 * @param message
	 *            message.
	 */
	public void receiveMessage(final String message) {
		LOG.debug(" [x] Received '" + message + "'");

		final Gson oGson = new Gson();
		final StudyCard studyCard = oGson.fromJson(message, StudyCard.class);

		try {
			studyCardService.updateFromShanoirOld(studyCard);
			latch.countDown();
		} catch (ShanoirStudyCardsException e) {
			// Exception.
			// TODO: how to manage these exceptions to avoid messages loop
		}
	}

}
