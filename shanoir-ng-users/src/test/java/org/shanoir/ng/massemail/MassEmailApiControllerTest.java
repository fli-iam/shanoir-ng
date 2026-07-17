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

package org.shanoir.ng.massemail;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.shanoir.ng.massemail.controller.MassEmailApiController;
import org.shanoir.ng.massemail.model.RecipientGroup;
import org.shanoir.ng.massemail.service.MassEmailService;
import org.shanoir.ng.shared.exception.SecurityException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

/**
 * Unit tests for the mass email controller.
 *
 * @author afragkiadakis
 */
@WebMvcTest(controllers = MassEmailApiController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Import(MassEmailApiControllerTest.MethodSecurityConfiguration.class)
public class MassEmailApiControllerTest {

    /**
     * The production SecurityConfiguration is not loaded in this WebMvcTest
     * slice: re-enable method security so @PreAuthorize is enforced. CGLIB
     * proxying replicates Spring Boot's default (spring.aop.proxy-target-class
     * is true in production), as a JDK interface proxy would unmap the
     * controller.
     */
    @TestConfiguration
    @EnableMethodSecurity(proxyTargetClass = true)
    static class MethodSecurityConfiguration {
    }

    private static final String COUNT_PATH = "/massemail/count";

    @Autowired
    private MockMvc mvc;

    @MockBean
    private MassEmailService massEmailService;

    @Test
    @WithMockUser(authorities = { "ROLE_ADMIN" })
    public void countRecipientsTest() throws Exception {
        given(massEmailService.countRecipients(RecipientGroup.ACTIVE)).willReturn(42);

        mvc.perform(MockMvcRequestBuilders.get(COUNT_PATH).param("group", "ACTIVE")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("42"));
    }

    @Test
    @WithMockUser(authorities = { "ROLE_USER" })
    public void countRecipientsForbiddenToNonAdminTest() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get(COUNT_PATH).param("group", "ALL")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = { "ROLE_ADMIN" })
    public void countRecipientsBadGroupTest() throws Exception {
        // the platform-wide GlobalExceptionHandler maps type mismatches to 500
        mvc.perform(MockMvcRequestBuilders.get(COUNT_PATH).param("group", "NOT_A_GROUP")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser(authorities = { "ROLE_ADMIN" })
    public void countRecipientsKeycloakErrorTest() throws Exception {
        given(massEmailService.countRecipients(RecipientGroup.INACTIVE))
                .willThrow(new SecurityException("keycloak unreachable"));

        mvc.perform(MockMvcRequestBuilders.get(COUNT_PATH).param("group", "INACTIVE")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

}
