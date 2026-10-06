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

package org.shanoir.ng.configuration.security;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.shanoir.ng.neurobagel.dto.NeurobagelDatasetDescriptionDTO;
import org.shanoir.ng.neurobagel.dto.NeurobagelStudyDTO;
import org.shanoir.ng.neurobagel.service.NeurobagelService;
import org.shanoir.ng.shared.security.NeurobagelAccess;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

/**
 * Access rules of the Neurobagel export token, through the real security filter chain:
 * /neurobagel/** is for that token only, and that token is refused everywhere else.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class NeurobagelTokenSecurityTest {

    private static final String OTHER_ENDPOINT = "/datasetacquisition/byStudyCard/1";

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private NeurobagelService neurobagelService;

    @MockitoBean
    private ConnectionFactory connectionFactory;

    private static MockHttpServletRequestBuilder asNeurobagel(MockHttpServletRequestBuilder request) {
        return request.with(jwt().authorities(new SimpleGrantedAuthority(NeurobagelAccess.ROLE)));
    }

    private static MockHttpServletRequestBuilder as(String role, MockHttpServletRequestBuilder request) {
        return request.with(jwt().authorities(new SimpleGrantedAuthority(role)));
    }

    // --- /neurobagel/** ---

    @Test
    void neurobagelTokenListsExportedStudies() throws Exception {
        given(neurobagelService.findExportedStudies()).willReturn(List.of(new NeurobagelStudyDTO(1L, "first")));
        mvc.perform(asNeurobagel(get("/neurobagel/studies")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("first"));
    }

    @Test
    void neurobagelTokenGetsAnExportedStudy() throws Exception {
        given(neurobagelService.findExportedStudy(1L)).willReturn(Optional.of(new NeurobagelStudyDTO(1L, "first")));
        mvc.perform(asNeurobagel(get("/neurobagel/study/1")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("first"));
    }

    @Test
    void studyNotExportedIsNotFound() throws Exception {
        given(neurobagelService.findExportedStudy(2L)).willReturn(Optional.empty());
        mvc.perform(asNeurobagel(get("/neurobagel/study/2"))).andExpect(status().isNotFound());
    }

    @Test
    void neurobagelTokenGetsTheParticipantsOfAnExportedStudy() throws Exception {
        given(neurobagelService.findParticipantsTsv(1L)).willReturn(Optional.of("participant_id\nsub-1\n"));
        mvc.perform(asNeurobagel(get("/neurobagel/study/1/participants.tsv")))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/tab-separated-values"))
                .andExpect(content().string("participant_id\nsub-1\n"));
    }

    @Test
    void participantsOfAStudyNotExportedAreNotFound() throws Exception {
        given(neurobagelService.findParticipantsTsv(2L)).willReturn(Optional.empty());
        mvc.perform(asNeurobagel(get("/neurobagel/study/2/participants.tsv"))).andExpect(status().isNotFound());
    }

    @Test
    void neurobagelTokenGetsTheParticipantsDictionary() throws Exception {
        given(neurobagelService.getParticipantsDictionary()).willReturn("{\"participant_id\": {}}");
        mvc.perform(asNeurobagel(get("/neurobagel/participants.json")))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.participant_id").exists());
    }

    @Test
    void neurobagelTokenGetsTheDatasetDescriptionWithNeurobagelKeys() throws Exception {
        NeurobagelDatasetDescriptionDTO description = new NeurobagelDatasetDescriptionDTO();
        description.setName("My study");
        description.setKeywords(List.of("mri"));
        description.setAccessType("restricted");
        given(neurobagelService.findDatasetDescription(1L)).willReturn(Optional.of(description));
        mvc.perform(asNeurobagel(get("/neurobagel/study/1/dataset_description.json")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.Name").value("My study"))
                .andExpect(jsonPath("$.Keywords[0]").value("mri"))
                .andExpect(jsonPath("$.AccessType").value("restricted"))
                .andExpect(jsonPath("$.name").doesNotExist())
                .andExpect(jsonPath("$.AccessLink").doesNotExist());
    }

    @Test
    void datasetDescriptionOfAStudyNotExportedIsNotFound() throws Exception {
        given(neurobagelService.findDatasetDescription(2L)).willReturn(Optional.empty());
        mvc.perform(asNeurobagel(get("/neurobagel/study/2/dataset_description.json"))).andExpect(status().isNotFound());
    }

    @Test
    void shanoirUsersCannotCallNeurobagelEndpoints() throws Exception {
        for (String role : List.of("ROLE_USER", "ROLE_EXPERT", "ROLE_ADMIN")) {
            mvc.perform(as(role, get("/neurobagel/studies"))).andExpect(status().isForbidden());
            mvc.perform(as(role, get("/neurobagel/study/1"))).andExpect(status().isForbidden());
            mvc.perform(as(role, get("/neurobagel/study/1/participants.tsv"))).andExpect(status().isForbidden());
            mvc.perform(as(role, get("/neurobagel/participants.json"))).andExpect(status().isForbidden());
            mvc.perform(as(role, get("/neurobagel/study/1/dataset_description.json"))).andExpect(status().isForbidden());
        }
    }

    @Test
    void anonymousCannotCallNeurobagelEndpoints() throws Exception {
        mvc.perform(get("/neurobagel/studies")).andExpect(status().isUnauthorized());
    }

    // --- every other endpoint ---

    @Test
    void neurobagelTokenIsRefusedElsewhere() throws Exception {
        mvc.perform(asNeurobagel(get(OTHER_ENDPOINT))).andExpect(status().isForbidden());
    }

    /**
     * ROLE_ADMIN passes every @PreAuthorize: only the filter chain rule (NeurobagelAccess) refuses this one.
     */
    @Test
    void neurobagelTokenIsRefusedElsewhereEvenWithOtherRoles() throws Exception {
        mvc.perform(get(OTHER_ENDPOINT).with(jwt().authorities(
                new SimpleGrantedAuthority(NeurobagelAccess.ROLE), new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void shanoirUserGoesThroughTheFilterChain() throws Exception {
        int status = mvc.perform(as("ROLE_ADMIN", get(OTHER_ENDPOINT))).andReturn().getResponse().getStatus();
        assertNotEquals(401, status);
        assertNotEquals(403, status);
    }

}
