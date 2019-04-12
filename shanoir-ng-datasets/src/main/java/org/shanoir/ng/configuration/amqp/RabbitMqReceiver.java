package org.shanoir.ng.configuration.amqp;

import java.util.concurrent.CountDownLatch;

import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.dataset.service.DatasetService;
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
	private DatasetService datasetService;

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
		final Dataset template = oGson.fromJson(message, Dataset.class);


		//datasetService.updateFromShanoirOld(template);
		latch.countDown();

	}

}
