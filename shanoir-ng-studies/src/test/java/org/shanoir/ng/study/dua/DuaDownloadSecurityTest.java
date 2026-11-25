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

package org.shanoir.ng.study.dua;

import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.shanoir.ng.shared.security.rights.StudyUserRight;
import org.shanoir.ng.storage.StorageService;
import org.shanoir.ng.study.model.Study;
import org.shanoir.ng.study.model.StudyUser;
import org.shanoir.ng.study.repository.StudyRepository;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

/**
 * The DUA of a study can be downloaded by the admins, the members who can see the study, and the users asked to
 * accept it; and the endpoint serves only the DUA, not the other files of the study (protocol files).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DuaDownloadSecurityTest {

    private static final String DUA = "DUA-agreement.pdf";

    private static final String PROTOCOL = "protocol.pdf";

    private static final String URL = "/studies/dua-download/1/";

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private ConnectionFactory connectionFactory;

    @MockitoBean
    private StudyRepository studyRepository;

    @MockitoBean
    private DataUserAgreementRepository dataUserAgreementRepository;

    @MockitoBean
    private StorageService storageService;

    @BeforeEach
    void setUp() throws Exception {
        Study study = new Study();
        study.setId(1L);
        study.setDataUserAgreementPaths(List.of(DUA));
        study.setProtocolFilePaths(List.of(PROTOCOL));
        List<StudyUser> members = new ArrayList<>();
        members.add(member(20L, true, StudyUserRight.CAN_SEE_ALL));
        // a member with a pending DUA is not confirmed yet
        members.add(member(30L, false, StudyUserRight.CAN_SEE_ALL));
        members.add(member(40L, true, StudyUserRight.CAN_IMPORT));
        study.setStudyUserList(members);
        given(studyRepository.findById(1L)).willReturn(Optional.of(study));
        given(studyRepository.findDataUserAgreementPathsByStudyId(1L)).willReturn(List.of(DUA));
        given(dataUserAgreementRepository.findByUserIdAndStudy_IdAndTimestampOfAcceptedIsNull(30L, 1L))
                .willReturn(new DataUserAgreement());
        given(storageService.loadStudyData(1L, DUA)).willReturn(new ByteArrayResource("dua".getBytes(StandardCharsets.UTF_8)));
        given(storageService.loadStudyData(1L, PROTOCOL)).willReturn(new ByteArrayResource("protocol".getBytes(StandardCharsets.UTF_8)));
    }

    private static StudyUser member(Long userId, boolean confirmed, StudyUserRight right) {
        StudyUser member = new StudyUser();
        member.setUserId(userId);
        member.setConfirmed(confirmed);
        member.setStudyUserRights(new ArrayList<>(List.of(right)));
        return member;
    }

    private static MockHttpServletRequestBuilder as(Long userId, String role, MockHttpServletRequestBuilder request) {
        return request.with(jwt().jwt(token -> token.claim("userId", userId)).authorities(new SimpleGrantedAuthority(role)));
    }

    private static MockHttpServletRequestBuilder dua(String fileName) {
        return get(URL.replace("/1/", "/1/" + fileName + "/"));
    }

    @Test
    void anAdminDownloadsTheDua() throws Exception {
        mvc.perform(as(10L, "ROLE_ADMIN", dua(DUA))).andExpect(status().isOk()).andExpect(content().string("dua"));
    }

    @Test
    void aMemberWhoCanSeeTheStudyDownloadsTheDua() throws Exception {
        mvc.perform(as(20L, "ROLE_USER", dua(DUA))).andExpect(status().isOk()).andExpect(content().string("dua"));
    }

    @Test
    void aUserAskedToAcceptTheDuaDownloadsIt() throws Exception {
        mvc.perform(as(30L, "ROLE_USER", dua(DUA))).andExpect(status().isOk()).andExpect(content().string("dua"));
    }

    @Test
    void aMemberWhoCantSeeTheStudyCantDownloadTheDua() throws Exception {
        mvc.perform(as(40L, "ROLE_EXPERT", dua(DUA))).andExpect(status().isForbidden());
    }

    @Test
    void aUserWithNoLinkToTheStudyCantDownloadTheDua() throws Exception {
        mvc.perform(as(50L, "ROLE_USER", dua(DUA))).andExpect(status().isForbidden());
    }

    @Test
    void anotherFileOfTheStudyIsNotServedAsTheDua() throws Exception {
        // the protocol files need CAN_DOWNLOAD, on their own endpoint
        mvc.perform(as(20L, "ROLE_USER", dua(PROTOCOL))).andExpect(status().isNotFound());
        mvc.perform(as(10L, "ROLE_ADMIN", dua(PROTOCOL))).andExpect(status().isNotFound());
    }

}
