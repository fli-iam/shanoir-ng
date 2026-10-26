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
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.shanoir.ng.importer.ImportJobStatusService;
import org.shanoir.ng.importer.ImporterApiController;
import org.shanoir.ng.importer.ImporterManagerService;
import org.shanoir.ng.importer.dicom.DicomDirGeneratorService;
import org.shanoir.ng.importer.dicom.DicomDirToModelService;
import org.shanoir.ng.importer.dicom.ImagesCreatorAndDicomFileAnalyzerService;
import org.shanoir.ng.importer.dicom.query.QueryPACSService;
import org.shanoir.ng.shared.event.ShanoirEventService;
import org.shanoir.ng.shared.security.NeurobagelAccess;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

/**
 * The Neurobagel export token must be refused by this service, through the real security filter chain.
 * Web slice with the real SecurityConfiguration: the import test setup has no database for a full context.
 */
@WebMvcTest(controllers = ImporterApiController.class)
@Import(SecurityConfiguration.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class NeurobagelTokenSecurityTest {

    private static final String ENDPOINT = "/importer/status/1";

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @MockitoBean
    private RestTemplate restTemplate;

    @MockitoBean
    private DicomDirToModelService dicomDirToModel;

    @MockitoBean
    private ImagesCreatorAndDicomFileAnalyzerService imagesCreatorAndDicomFileAnalyzer;

    @MockitoBean
    private ImporterManagerService importerManagerService;

    @MockitoBean
    private QueryPACSService queryPACSService;

    @MockitoBean
    private RabbitTemplate rabbitTemplate;

    @MockitoBean
    private DicomDirGeneratorService dicomDirGeneratorService;

    @MockitoBean
    private ShanoirEventService shanoirEventService;

    @MockitoBean
    private ImportJobStatusService importJobStatusService;

    @Test
    void neurobagelTokenIsRefused() throws Exception {
        mvc.perform(get(ENDPOINT).with(jwt().authorities(new SimpleGrantedAuthority(NeurobagelAccess.ROLE))))
                .andExpect(status().isForbidden());
    }

    /**
     * ROLE_ADMIN passes every @PreAuthorize: only the filter chain rule (NeurobagelAccess) refuses this one.
     */
    @Test
    void neurobagelTokenIsRefusedEvenWithOtherRoles() throws Exception {
        mvc.perform(get(ENDPOINT).with(jwt().authorities(
                new SimpleGrantedAuthority(NeurobagelAccess.ROLE), new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void anonymousIsRefused() throws Exception {
        mvc.perform(get(ENDPOINT)).andExpect(status().isUnauthorized());
    }

    @Test
    void shanoirUserGoesThroughTheFilterChain() throws Exception {
        int status = mvc.perform(get(ENDPOINT).with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andReturn().getResponse().getStatus();
        assertNotEquals(401, status);
        assertNotEquals(403, status);
    }

}
