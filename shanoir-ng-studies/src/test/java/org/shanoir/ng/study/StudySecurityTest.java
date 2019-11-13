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

package org.shanoir.ng.study;

import static org.junit.Assert.assertNotNull;
import static org.shanoir.ng.utils.assertion.AssertUtils.assertAccessAuthorized;
import static org.shanoir.ng.utils.assertion.AssertUtils.assertAccessDenied;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.shared.security.rights.StudyUserRight;
import org.shanoir.ng.study.model.Study;
import org.shanoir.ng.study.model.StudyUser;
import org.shanoir.ng.study.repository.StudyRepository;
import org.shanoir.ng.study.service.StudyService;
import org.shanoir.ng.utils.ModelsUtil;
import org.shanoir.ng.utils.usermock.WithMockKeycloakUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;

/**
 * User security service test.
 * 
 * @author jlouis
 * 
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@EnableGlobalMethodSecurity(prePostEnabled = true)
@ActiveProfiles("test")
@WebAppConfiguration
@DataJpaTest
public class StudySecurityTest {

	private static final long LOGGED_USER_ID = 2L;
	private static final String LOGGED_USER_USERNAME = "logged";

	@Autowired
	private StudyService service;
	
	@Autowired
	private StudyRepository repo;
	
	@Test
	@WithMockKeycloakUser(id = LOGGED_USER_ID, username = LOGGED_USER_USERNAME, authorities = { "ROLE_EXPERT" })
	public void testHackStudyUserRights() throws ShanoirException {
		
		assertNotNull(repo.save(buildStudyMock(null)));
		service.create(buildStudyMock(null));
		
		Study study = service.findById(1L);
		assertNotNull(study);
		assertAccessAuthorized(service::update, study);
		study.setId(3L);
		assertAccessDenied(service::update, study);
		study.setId(1L);
		
		Study hackedStudy = new Study();
		hackedStudy.setId(3L);
		assertAccessDenied(service::update, hackedStudy);
		
		StudyUser hackedSU = new StudyUser();
		hackedSU.setUserId(LOGGED_USER_ID);
		hackedSU.setUserName("HACKED");
		hackedSU.setStudy(hackedStudy);
		hackedSU.setStudyUserRights(Arrays.asList(StudyUserRight.CAN_ADMINISTRATE, StudyUserRight.CAN_IMPORT, StudyUserRight.CAN_DOWNLOAD, StudyUserRight.CAN_SEE_ALL));
				
		study.getStudyUserList().add(hackedSU);
		//assertAccessDenied(service::update, study);
		service.update(study);
		hackedSU.setId(666L);
		service.update(hackedStudy);
	}
	
	private Study buildStudyMock(Long id, StudyUserRight... rights) {
		Study study = ModelsUtil.createStudy();
		study.setId(id);
		List<StudyUser> studyUserList = new ArrayList<>();
		StudyUser studyUser = new StudyUser();
		studyUser.setUserId(LOGGED_USER_ID);
		studyUser.setUserName(LOGGED_USER_USERNAME);
		studyUser.setStudy(study);
		studyUser.setStudyUserRights(Arrays.asList(rights));
		studyUserList.add(studyUser);			
		study.setStudyUserList(studyUserList);
		return study;		
	}
}
