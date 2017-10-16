package org.shanoir.ng.configuration.amqp;

import java.util.concurrent.CountDownLatch;

import org.shanoir.ng.examination.Examination;
import org.shanoir.ng.examination.ExaminationService;
import org.shanoir.ng.shared.exception.ShanoirExaminationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.gson.Gson;

/**
 * RabbitMQ message receiver.
 * 
 * @author ifakhfakh
 *
 */
public class RabbitMqReceiver {
	private static final Logger LOG = LoggerFactory.getLogger(RabbitMqReceiver.class);

	@Autowired
	private ExaminationService examinationService;

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
		final Examination examination = oGson.fromJson(message, Examination.class);

		try {
			examinationService.updateFromShanoirOld(examination);
			latch.countDown();
		} catch (ShanoirExaminationException e) {
			// Exception.
			// TODO: how to manage these exceptions to avoid messages loop
		}
	}

}
