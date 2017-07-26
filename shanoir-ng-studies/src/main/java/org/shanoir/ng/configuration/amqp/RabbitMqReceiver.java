package org.shanoir.ng.configuration.amqp;

import java.util.concurrent.CountDownLatch;

import org.shanoir.ng.shared.exception.ShanoirStudiesException;
import org.shanoir.ng.study.Study;
import org.shanoir.ng.study.StudyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
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
	private StudyService studyService;

	private CountDownLatch latch = new CountDownLatch(1);

	/**
	 * Receive message.
	 *
	 * @param message
	 *            message.
	 */
	@RabbitListener(queues = "study_queue_to_ng")
	public void receiveStudyMessage(final String message) {
		LOG.debug(" [x] Received Study Update/Create '" + message + "'");
		final Gson oGson = new Gson();
		final Study study = oGson.fromJson(message, Study.class);
		try {
			studyService.updateFromShanoirOld(study);
			latch.countDown();
		} catch (ShanoirStudiesException e) {
			// Exception.
			// TODO: how to manage these exceptions to avoid messages loop
			LOG.error("Cannot create/update study " + study.getName() + " : ", e);
		}
	}

	/**
	 * Receive message.
	 *
	 * @param message
	 *            message.
	 */
 	@RabbitListener(queues = "study_delete_queue_to_ng")
	public void receiveStudyDeleteMessage(final String message) {
		LOG.debug(" [x] Received Study Delete'" + message + "'");
		final Gson oGson = new Gson();
		final Study study = oGson.fromJson(message, Study.class);
		try {
			studyService.deleteFromShanoirOld(study);
			latch.countDown();
		} catch (ShanoirStudiesException e) {
			// Exception.
			// TODO: how to manage these exceptions to avoid messages loop
			LOG.error("Cannot delete study " + study.getId() + " : ", e);
		}
	}

}
