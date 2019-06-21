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

package org.shanoir.ng.study.rights.ampq;

import java.util.Arrays;

import org.shanoir.ng.study.rights.StudyUser;
import org.shanoir.ng.study.rights.StudyUserInterface;
import org.shanoir.ng.study.rights.command.StudyUserCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

@Service
public class StudyUserListener {
	
	private static final Logger LOG = LoggerFactory.getLogger(StudyUserListener.class);
	
	@Autowired
	private StudyUserUpdateService service;
	
    public void receiveMessageImport(String commandArrStr) throws AmqpRejectAndDontRequeueException  {
    	StudyUserCommand[] commands;
    	try {
    		LOG.debug("Received study-user commands : " + commandArrStr);
			ObjectMapper mapper = new ObjectMapper();
			
			SimpleModule module = new SimpleModule();
			module.addAbstractTypeMapping(StudyUserInterface.class, StudyUser.class);
			mapper.registerModule(module);
			
			commands = mapper.readValue(commandArrStr, StudyUserCommand[].class);
			service.processCommands(Arrays.asList(commands));
		} catch (Exception e) {
			throw new AmqpRejectAndDontRequeueException("Study User Update rejected !!!", e);
		}
    }
	
	
}