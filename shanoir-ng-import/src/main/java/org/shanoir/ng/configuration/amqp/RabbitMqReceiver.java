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

package org.shanoir.ng.configuration.amqp;

import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RabbitMQ message receiver.
 * 
 * @author ifakhfakh
 *
 */
public class RabbitMqReceiver {
	private static final Logger LOG = LoggerFactory.getLogger(RabbitMqReceiver.class);

	private CountDownLatch latch = new CountDownLatch(1);

	/**
	 * Receive message.
	 * 
	 * @param message
	 *            message.
	 */
	public void receiveMessage(final String message) {
		LOG.debug(" [x] Received '" + message + "'");

		latch.countDown();
	}

}
