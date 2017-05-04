package org.shanoir.ng.configuration.amqp;

import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;


public class RabbitMqClient {

	private static final Logger LOG = LoggerFactory.getLogger(RabbitMqClient.class);

	@Autowired
	private RabbitTemplate template;

	// @Autowired
	// private DirectExchange exchange;


	public void send(String message) {
		System.out.println(" [x] Send subject to sh_old with content (" + message + ")");
		byte[] response = (byte[]) template.convertSendAndReceive(RabbitMqConfiguration.subjectRPCQueueOut().getName(), message);
		String msg = null;
		try{
			msg = new String(response,"UTF-8");
		}catch(IOException ioe){
			System.out.println(" IO EXCEPTION " + ioe );
		}
		LOG.info("NEW RPC MESSAGE");
		System.out.println(" [x] Received '" + msg + "'");
		System.out.println(" [.] Got '" + response + "'");
	}

}
