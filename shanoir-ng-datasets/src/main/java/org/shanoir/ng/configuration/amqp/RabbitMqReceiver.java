package org.shanoir.ng.configuration.amqp;

import java.util.concurrent.CountDownLatch;

import org.shanoir.ng.dataset.Dataset;
import org.shanoir.ng.dataset.DatasetService;
import org.shanoir.ng.shared.exception.ShanoirException;
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
	private DatasetService<Dataset> datasetService;

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

		try {
			datasetService.updateFromShanoirOld(template);
			latch.countDown();
		} catch (ShanoirException e) {
			// Exception.
			// TODO: how to manage these exceptions to avoid messages loop
		}
	}

}
