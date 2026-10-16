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
import org.shanoir.ng.shared.security.NeurobagelAccess;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * The Neurobagel export token must be refused by this service, through the real security filter chain.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class NeurobagelTokenSecurityTest {

    private static final String ENDPOINT = "/centers/1";

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private ConnectionFactory connectionFactory;

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
