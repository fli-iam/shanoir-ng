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