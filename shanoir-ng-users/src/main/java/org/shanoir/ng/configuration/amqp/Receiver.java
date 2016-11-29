package org.shanoir.ng.configuration.amqp;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import org.shanoir.ng.service.UserService;
import org.shanoir.ng.service.impl.AuthenticationServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.gson.Gson;

public class Receiver {
	private static final Logger LOG = LoggerFactory.getLogger(AuthenticationServiceImpl.class);
	
	@Autowired
	private UserService userService;
	
	private CountDownLatch latch = new CountDownLatch(1);
	
	public void receiveMessage(String message) throws IOException {
		LOG.debug(" [x] Received '" + message + "'");
        
        Gson oGson = new Gson();
        org.shanoir.ng.model.User user = oGson.fromJson(message, org.shanoir.ng.model.User.class);
        
        userService.updateFromShanoirOld(user);
        latch.countDown();
    }
	
}
