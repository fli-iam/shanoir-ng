package org.shanoir.ng.configuration.amqp;

import java.util.concurrent.CountDownLatch;

import org.shanoir.ng.shared.exception.ShanoirStudyException;
import org.shanoir.ng.study.Study;
import org.shanoir.ng.study.StudyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import java.io.IOException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.annotation.RabbitListener;


import org.springframework.amqp.core.Message;

import com.google.gson.Gson;

/**
 * RabbitMQ message receiver with RPC Callback.
 *
 * @author atouboul
 *
 */
public class RPCMqReceiver {
	private static final Logger LOG = LoggerFactory.getLogger(RPCMqReceiver.class);

	// private CountDownLatch latch = new CountDownLatch(1);

	@RabbitListener(queues = "subject_queue_with_RPC_to_ng")
		public String receiveAndReply(byte[] msg) {
		String message = null;
		try{
			message = new String(msg,"UTF-8");
		}catch(IOException ioe){
			System.out.println(" IO EXCEPTION " + ioe );
		}


		LOG.info(" [x] Received request for " + message);
		String result = message;
		LOG.info(" [.] Returned " + result);
		return result;
	}

}
