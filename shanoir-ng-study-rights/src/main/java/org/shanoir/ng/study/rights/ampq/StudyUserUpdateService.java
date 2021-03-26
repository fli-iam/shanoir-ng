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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.shanoir.ng.shared.security.rights.StudyUserRight;
import org.shanoir.ng.study.rights.StudyUser;
import org.shanoir.ng.study.rights.StudyUserRightsRepository;
import org.shanoir.ng.study.rights.command.CommandType;
import org.shanoir.ng.study.rights.command.StudyUserCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.keyvalue.core.IterableConverter;
import org.springframework.stereotype.Service;

@Service
public class StudyUserUpdateService {
	
	private static final Logger LOG = LoggerFactory.getLogger(StudyUserUpdateService.class);
	
	@Autowired
	private StudyUserRightsRepository studyUserRepository;

    public void processCommands(Iterable<StudyUserCommand> commands) {
        
        List<StudyUser> toBeCreated = new ArrayList<>();
        Map<Long, StudyUser> toBeUpdated = new HashMap<>();
        Set<Long> toBeDeleted = new HashSet<>();
        for (StudyUserCommand command : commands) {
    		LOG.debug("command : {}, id : {}/{}",
    				 command.getStudyUser() != null && command.getStudyUser().getId() != null ? command.getStudyUser().getId().toString() : "null",
    				 command.getStudyUserId() != null ? command.getStudyUserId().toString() : "null",
    				 command.getType());
        	if (CommandType.CREATE.equals(command.getType())) {
				toBeCreated.add((StudyUser) command.getStudyUser());
			} else if (CommandType.UPDATE.equals(command.getType())) {
				toBeUpdated.put(command.getStudyUser().getId(), (StudyUser) command.getStudyUser());
			} else if (CommandType.DELETE.equals(command.getType())) {
				toBeDeleted.add(command.getStudyUserId());
			}
        }
        
        Iterable<StudyUser> toBeUpdatedDb = studyUserRepository.findAll(toBeUpdated.keySet());
        for (StudyUser existingSu : toBeUpdatedDb) {
        	StudyUser replacingSu = toBeUpdated.get(existingSu.getId());
			existingSu.setReceiveAnonymizationReport(replacingSu.isReceiveAnonymizationReport());
			existingSu.setReceiveNewImportReport(replacingSu.isReceiveNewImportReport());
			existingSu.setStudyUserRights(replacingSu.getStudyUserRights());
			existingSu.setConfirmed(replacingSu.isConfirmed());
        }
        
        if (!toBeCreated.isEmpty()) {
        	LOG.debug("Saving {} new study-user(s)", toBeCreated.size());
        	for (StudyUser su : toBeCreated) {
        		LOG.debug("getId : {}", su.getId());
        		LOG.debug("getUserName : {}", su.getUserName());
        		LOG.debug("getStudyId : {}", su.getStudyId());
        		LOG.debug("getUserId : {}", su.getUserId());
        		LOG.debug("getStudyUserRights :{}",  su.getStudyUserRights() == null ? "null" : su.getStudyUserRights().size());
        		if (su.getStudyUserRights() != null) {
        			for (StudyUserRight right : su.getStudyUserRights()) {
        				LOG.debug("    ---> : {}", right);
        			}
        		}
        	}
        	studyUserRepository.save(toBeCreated);
        }
        int updateSize = IterableConverter.toList(toBeUpdatedDb).size();
        if (updateSize > 0) {
        	LOG.debug("Updating {} study-user(s)", updateSize);
        	studyUserRepository.save(toBeUpdatedDb);
        }
        if (!toBeDeleted.isEmpty()) {
        	LOG.debug("Deleting {} study-user(s)", toBeDeleted.size());
        	studyUserRepository.deleteByIdIn(toBeDeleted);
        }
    }
}