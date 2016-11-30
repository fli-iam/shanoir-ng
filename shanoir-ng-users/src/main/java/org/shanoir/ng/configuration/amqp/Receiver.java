package org.shanoir.ng.configuration.amqp;

import java.util.concurrent.CountDownLatch;

import org.shanoir.ng.model.User;
import org.shanoir.ng.model.exception.ShanoirUsersException;
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
	
	public void receiveMessage(String message) {
		LOG.debug(" [x] Received '" + message + "'");
        
        Gson oGson = new Gson();
        User user = oGson.fromJson(message, User.class);
        
        try {
			userService.updateFromShanoirOld(user);
			latch.countDown();
		} catch (ShanoirUsersException e) {
			// Exception.
			// TODO: how to manage these exceptions to avoid messages loop
		}
    }
	
}
