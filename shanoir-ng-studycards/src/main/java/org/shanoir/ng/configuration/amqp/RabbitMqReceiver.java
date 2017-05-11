package org.shanoir.ng.configuration.amqp;

import java.util.concurrent.CountDownLatch;

import org.shanoir.ng.shared.exception.ShanoirStudyCardsException;
import org.shanoir.ng.studycard.StudyCard;
import org.shanoir.ng.studycard.StudyCardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.gson.Gson;

/**ShanoirStudyCardException
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
	public void receiveStudyCardMessage(final String message) {
		LOG.debug(" [x] Received '" + message + "'");
		System.out.println(" [x] Received '" + message + "'");
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



	/**
	 * Receive message.
	 *
	 * @param message
	 *            message.
	 */
	public void receiveStudyCardDeleteMessage(final String message) {
		LOG.info("NEW MESSAGE");
		LOG.debug(" [x] Received StudyCard Delete'" + message + "'");
		System.out.println(" [x] Received '" + message + "'");

		final Gson oGson = new Gson();
		final StudyCard studyCard = oGson.fromJson(message, StudyCard.class);

		try {
			studyCardService.deleteFromShanoirOld(studyCard);
			latch.countDown();
		} catch (ShanoirStudyCardsException e) {
			// Exception.
			// TODO: how to manage these exceptions to avoid messages loop
		}
	}
}
