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

package org.shanoir.ng.studycard;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.shanoir.ng.dicom.web.StudyInstanceUIDHandler;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.study.rights.StudyRightsService;
import org.shanoir.ng.study.rights.StudyUserRightsRepository;
import org.shanoir.ng.studycard.model.StudyCard;
import org.shanoir.ng.studycard.service.StudyCardService;
import org.shanoir.ng.utils.ModelsUtil;
import org.shanoir.ng.utils.usermock.WithMockKeycloakUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.shanoir.ng.utils.assertion.AssertUtils.assertAccessAuthorized;
import static org.shanoir.ng.utils.assertion.AssertUtils.assertAccessDenied;

/**
 * StudyCard service security test.
 *
 * @author jlouis
 *
 */
@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
public class StudyCardServiceSecurityTest {

    private static final long LOGGED_USER_ID = 2L;
    private static final String LOGGED_USER_USERNAME = "logged";
    private static final long ENTITY_ID = 1L;
    
    @Autowired
    private StudyCardService service;
    
    @MockBean
    private StudyRightsService rightsService;
    
    @MockBean
    private StudyUserRightsRepository rightsRepository;

    @MockBean
    private StudyInstanceUIDHandler studyInstanceUIDHandler;

    @BeforeEach
    public void setup() {
        given(rightsService.hasRightOnStudy(Mockito.anyLong(), Mockito.anyString())).willReturn(false);
        given(rightsService.hasRightOnStudies(Mockito.any(), Mockito.anyString())).willReturn(new HashSet<Long>());
    }
    
    @Test
    @WithAnonymousUser
    public void testAsAnonymous() throws ShanoirException {
        given(rightsService.hasRightOnStudy(Mockito.anyLong(), Mockito.anyString())).willReturn(true);
        Set<Long> ids = Mockito.anySet();
        given(rightsService.hasRightOnStudies(ids, Mockito.anyString())).willReturn(ids);
        
        // Fetch one
        assertAccessDenied(service::findById, ENTITY_ID);
        
        // Fetch list
        assertAccessDenied(service::search, Arrays.asList(1L, 2L));
        assertAccessDenied(service::findAll);
        assertAccessDenied(service::findStudyCardsByAcqEq, 1L);
        assertAccessDenied(service::findByStudy, 1L);

        // Write
        assertAccessDenied(service::deleteById, 4L);
        assertAccessDenied(service::save, mockStudyCard());
        assertAccessDenied(service::update, mockStudyCard(1L));
    }

    @Test
    @WithMockKeycloakUser(id = LOGGED_USER_ID, username = LOGGED_USER_USERNAME, authorities = { "ROLE_USER" })
    public void testAsUser() throws ShanoirException {

        // Fetch one
        given(rightsService.hasRightOnStudy(1L, "CAN_SEE_ALL")).willReturn(true);
        given(rightsService.hasRightOnStudy(2L, "CAN_SEE_ALL")).willReturn(false);        
        assertAccessAuthorized(service::findById, 1L);
        assertAccessDenied(service::findById, 3L);
        
        // Fetch list
        HashSet<Long> idSetAuthStudies = new HashSet<Long>(); idSetAuthStudies.add(1L);
        given(rightsService.hasRightOnStudies(Mockito.any(), Mockito.anyString())).willReturn(idSetAuthStudies);
        
        assertAccessAuthorized(service::search, Arrays.asList(1L, 2L, 3L));
        for (StudyCard sc : service.search(Arrays.asList(1L, 2L, 3L))) {
            assertEquals(new Long(1), sc.getStudyId());
        }
        
        assertAccessAuthorized(service::findAll);
        for (StudyCard sc : service.findAll()) {
            assertEquals(new Long(1), sc.getStudyId());
        }
        
        assertAccessAuthorized(service::findStudyCardsByAcqEq, 1L);
        assertEquals(2, service.findStudyCardsByAcqEq(1L).size());
        assertEquals(0, service.findStudyCardsByAcqEq(3L).size());
        assertEquals(0, service.findStudyCardsByAcqEq(4L).size());
        
        assertAccessAuthorized(service::findByStudy, 1L);
        assertEquals(3, service.findByStudy(1L).size());
        assertEquals(0, service.findByStudy(2L).size());
        assertEquals(0, service.findByStudy(3L).size());
        
        // Write
        given(rightsService.hasRightOnStudy(1L, "CAN_SEE_ALL")).willReturn(true);
        HashSet<Long> idSet = new HashSet<Long>(); idSet.add(1L);
        given(rightsService.hasRightOnStudies(Mockito.any(), Mockito.anyString())).willReturn(idSet);
        
        assertAccessDenied(service::deleteById, 4L);
        assertAccessDenied(service::save, mockStudyCard());
        assertAccessDenied(service::update, mockStudyCard(1L));
    }
    
    @Test
    @WithMockKeycloakUser(id = LOGGED_USER_ID, username = LOGGED_USER_USERNAME, authorities = { "ROLE_EXPERT" })
    public void testAsExpert() throws ShanoirException {
        // Fetch one
        given(rightsService.hasRightOnStudy(1L, "CAN_SEE_ALL")).willReturn(true);
        given(rightsService.hasRightOnStudy(2L, "CAN_SEE_ALL")).willReturn(false);        
        assertAccessAuthorized(service::findById, 1L);
        assertAccessDenied(service::findById, 3L);
        
        // Fetch list
        HashSet<Long> idSetAuthStudies = new HashSet<Long>(); idSetAuthStudies.add(1L);
        given(rightsService.hasRightOnStudies(Mockito.any(), Mockito.anyString())).willReturn(idSetAuthStudies);
        
        assertAccessAuthorized(service::search, Arrays.asList(1L, 2L, 3L));
        for (StudyCard sc : service.search(Arrays.asList(1L, 2L, 3L))) {
            assertEquals(new Long(1), sc.getStudyId());
        }
        
        assertAccessAuthorized(service::findAll);
        for (StudyCard sc : service.findAll()) {
            assertEquals(new Long(1), sc.getStudyId());
        }
        
        assertAccessAuthorized(service::findStudyCardsByAcqEq, 1L);
        assertEquals(2, service.findStudyCardsByAcqEq(1L).size());
        assertEquals(0, service.findStudyCardsByAcqEq(3L).size());
        assertEquals(0, service.findStudyCardsByAcqEq(4L).size());
        
        assertAccessAuthorized(service::findByStudy, 1L);
        assertEquals(3, service.findByStudy(1L).size());
        assertEquals(0, service.findByStudy(2L).size());
        assertEquals(0, service.findByStudy(3L).size());
        
        // Write
        given(rightsService.hasRightOnStudy(1L, "CAN_SEE_ALL")).willReturn(true);
        HashSet<Long> idSet = new HashSet<Long>(); idSet.add(1L);
        given(rightsService.hasRightOnStudies(Mockito.any(), Mockito.anyString())).willReturn(idSet);
        given(rightsService.hasRightOnStudy(1L, "CAN_ADMINISTRATE")).willReturn(true);
        given(rightsService.hasRightOnStudy(2L, "CAN_ADMINISTRATE")).willReturn(false);
        given(rightsService.hasRightOnStudy(3L, "CAN_ADMINISTRATE")).willReturn(false);
        
        StudyCard testSc = mockStudyCard();
        
        assertAccessAuthorized(service::save, testSc);
        
        assertAccessAuthorized(service::update, testSc);
        testSc.setStudyId(3L);
        assertAccessDenied(service::update, testSc);
        
        StudyCard testSc2 = mockStudyCard(3L);
        testSc2.setStudyId(1L);
        assertAccessDenied(service::update, testSc);
        
        assertAccessAuthorized(service::deleteById, testSc.getId());
        assertAccessDenied(service::deleteById, 3L);        
    }
    
    @Test
    @WithMockKeycloakUser(id = LOGGED_USER_ID, username = LOGGED_USER_USERNAME, authorities = { "ROLE_ADMIN" })
    public void testAsAdmin() throws ShanoirException {
        // Fetch one
        assertAccessAuthorized(service::findById, ENTITY_ID);
        
        // Fetch list
        assertAccessAuthorized(service::search, Arrays.asList(1L, 2L));
        assertAccessAuthorized(service::findAll);
        assertAccessAuthorized(service::findStudyCardsByAcqEq, 1L);
        assertAccessAuthorized(service::findByStudy, 1L);
        
        // Write
        StudyCard sc = mockStudyCard();
        assertAccessAuthorized(service::save, sc);
        assertAccessAuthorized(service::update, sc);
        assertAccessAuthorized(service::deleteById, sc.getId());
    }
        
    private StudyCard mockStudyCard(Long id) {
        StudyCard sc = ModelsUtil.createStudyCard();
        sc.setId(id);
        return sc;
    }
    
    private StudyCard mockStudyCard() {
        return mockStudyCard(null);
    }

}
