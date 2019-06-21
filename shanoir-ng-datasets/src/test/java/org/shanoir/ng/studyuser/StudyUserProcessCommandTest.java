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

package org.shanoir.ng.studyuser;

import static org.mockito.BDDMockito.given;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.shanoir.ng.shared.security.rights.StudyUserRight;
import org.shanoir.ng.study.rights.StudyUser;
import org.shanoir.ng.study.rights.StudyUserRightsRepository;
import org.shanoir.ng.study.rights.ampq.StudyUserUpdateService;
import org.shanoir.ng.study.rights.command.CommandType;
import org.shanoir.ng.study.rights.command.StudyUserCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class StudyUserProcessCommandTest {

	@Autowired
	StudyUserUpdateService service;
	
	@MockBean
	StudyUserRightsRepository studyUserRepository;
	
	@Test
    public void processCommandsTest() {
		given(studyUserRepository.findAll(Mockito.anyList())).willReturn(new ArrayList<>());
        
		List<StudyUserCommand> commands = new ArrayList<>();
		commands.add(new StudyUserCommand(CommandType.DELETE, 1L));
		commands.add(new StudyUserCommand(CommandType.CREATE, makeSU()));
		service.processCommands(commands);
        
    }
	
	private StudyUser makeSU() {
		StudyUser su = new StudyUser();
		su.setId(2L);
		su.setUserId(1L);
		su.setStudyId(1L);
		su.setUserName("Jeannot");
		List<StudyUserRight> rights = new ArrayList<>();
		rights.add(StudyUserRight.CAN_ADMINISTRATE);
		rights.add(StudyUserRight.CAN_IMPORT);
		rights.add(StudyUserRight.CAN_DOWNLOAD);
		rights.add(StudyUserRight.CAN_SEE_ALL);
		su.setStudyUserRights(rights);
		return su;
	}
}