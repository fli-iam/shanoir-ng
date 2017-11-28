package org.shanoir.ng.configuration.amqp;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * RabbitMq RPC Client.
 *
 * @author atouboul
 *
 */

public class RabbitMqRPCClient {

	private static final Logger LOG = LoggerFactory.getLogger(RabbitMqRPCClient.class);

	@Autowired
	private RabbitTemplate template;

	public void send(String message) {
		LOG.debug("[x] Send subject to sh_old with content (" + message + ")");
		byte[] response = (byte[]) template.convertSendAndReceive(RabbitMqConfiguration.subjectRPCQueueOut().getName(),
				message);
		String msg = null;
		try {
			msg = new String(response, "UTF-8");
		} catch (IOException ioe) {
			LOG.error(" IO EXCEPTION " + ioe);
		}
		LOG.debug(" [x] Received '" + msg + "'");
		LOG.debug(" [.] Got '" + response + "'");
	}

}
