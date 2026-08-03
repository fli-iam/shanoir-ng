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

package org.shanoir.ng.email;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.shanoir.ng.ShanoirUsersManagement;
import org.shanoir.ng.email.model.RecipientGroup;
import org.shanoir.ng.shared.exception.SecurityException;
import org.shanoir.ng.user.model.User;
import org.shanoir.ng.utils.ModelsUtil;
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

    private static final String SEND_PATH = "/massemail";

    private static final String COUNT_PATH = "/massemail/count";

    @Autowired
    private MockMvc mvc;

    @MockBean
    private MassEmailService massEmailService;

    /** Referenced by the @PreAuthorize of the send endpoint to check CAN_ADMINISTRATE. */
    @MockBean(name = "shanoirUsersManagement")
    private ShanoirUsersManagement shanoirUsersManagement;

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
    public void sendMassEmailTest() throws Exception {
        final List<User> recipients = List.of(ModelsUtil.createUser(1L), ModelsUtil.createUser(2L));
        given(massEmailService.resolveRecipients(RecipientGroup.ACTIVE)).willReturn(recipients);

        mvc.perform(MockMvcRequestBuilders.post(SEND_PATH).accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"recipientGroup\":\"ACTIVE\",\"subject\":\"Maintenance\",\"content\":\"Down tomorrow.\"}"))
                .andExpect(status().isAccepted())
                .andExpect(content().string("2"));

        Mockito.verify(massEmailService).sendMassEmail(recipients, "Maintenance", "Down tomorrow.");
    }

    @Test
    @WithMockUser(authorities = { "ROLE_ADMIN" })
    public void sendMassEmailToStudyTest() throws Exception {
        final List<User> recipients = List.of(ModelsUtil.createUser(1L), ModelsUtil.createUser(2L));
        given(massEmailService.resolveStudyRecipients(3L)).willReturn(recipients);

        mvc.perform(MockMvcRequestBuilders.post(SEND_PATH).accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"recipientGroup\":\"STUDY\",\"studyId\":3,"
                        + "\"subject\":\"Maintenance\",\"content\":\"Down tomorrow.\"}"))
                .andExpect(status().isAccepted())
                .andExpect(content().string("2"));

        Mockito.verify(massEmailService).sendMassEmail(recipients, "Maintenance", "Down tomorrow.");
    }

    @Test
    @WithMockUser(authorities = { "ROLE_ADMIN" })
    public void sendMassEmailStudyWithoutStudyIdTest() throws Exception {
        mvc.perform(MockMvcRequestBuilders.post(SEND_PATH).accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"recipientGroup\":\"STUDY\",\"subject\":\"Maintenance\",\"content\":\"Down tomorrow.\"}"))
                .andExpect(status().isUnprocessableEntity());

        Mockito.verify(massEmailService, Mockito.never()).sendMassEmail(Mockito.anyList(), Mockito.anyString(),
                Mockito.anyString());
    }

    @Test
    @WithMockUser(authorities = { "ROLE_ADMIN" })
    public void sendMassEmailNonStudyGroupWithStudyIdTest() throws Exception {
        mvc.perform(MockMvcRequestBuilders.post(SEND_PATH).accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"recipientGroup\":\"ALL\",\"studyId\":3,"
                        + "\"subject\":\"Maintenance\",\"content\":\"Down tomorrow.\"}"))
                .andExpect(status().isUnprocessableEntity());

        Mockito.verify(massEmailService, Mockito.never()).sendMassEmail(Mockito.anyList(), Mockito.anyString(),
                Mockito.anyString());
    }

    @Test
    @WithMockUser(authorities = { "ROLE_EXPERT" })
    public void sendMassEmailToAdministratedStudyTest() throws Exception {
        final List<User> recipients = List.of(ModelsUtil.createUser(1L), ModelsUtil.createUser(2L));
        given(shanoirUsersManagement.hasRightOnStudy(3L, "CAN_ADMINISTRATE")).willReturn(true);
        given(massEmailService.resolveStudyRecipients(3L)).willReturn(recipients);

        mvc.perform(MockMvcRequestBuilders.post(SEND_PATH).accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"recipientGroup\":\"STUDY\",\"studyId\":3,"
                        + "\"subject\":\"Maintenance\",\"content\":\"Down tomorrow.\"}"))
                .andExpect(status().isAccepted())
                .andExpect(content().string("2"));

        Mockito.verify(massEmailService).sendMassEmail(recipients, "Maintenance", "Down tomorrow.");
    }

    @Test
    @WithMockUser(authorities = { "ROLE_USER" })
    public void sendMassEmailToStudyForbiddenWithoutAdministrateRightTest() throws Exception {
        given(shanoirUsersManagement.hasRightOnStudy(3L, "CAN_ADMINISTRATE")).willReturn(false);

        mvc.perform(MockMvcRequestBuilders.post(SEND_PATH).accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"recipientGroup\":\"STUDY\",\"studyId\":3,"
                        + "\"subject\":\"Maintenance\",\"content\":\"Down tomorrow.\"}"))
                .andExpect(status().isForbidden());

        Mockito.verify(massEmailService, Mockito.never()).sendMassEmail(Mockito.anyList(), Mockito.anyString(),
                Mockito.anyString());
    }

    /**
     * A study administrator is confined to the STUDY group: the platform-wide
     * groups stay out of reach even though they administrate a study.
     */
    @Test
    @WithMockUser(authorities = { "ROLE_EXPERT" })
    public void sendMassEmailToAllForbiddenToStudyAdminTest() throws Exception {
        given(shanoirUsersManagement.hasRightOnStudy(Mockito.anyLong(), Mockito.anyString())).willReturn(true);

        mvc.perform(MockMvcRequestBuilders.post(SEND_PATH).accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"recipientGroup\":\"ALL\",\"subject\":\"Maintenance\",\"content\":\"Down tomorrow.\"}"))
                .andExpect(status().isForbidden());

        Mockito.verify(massEmailService, Mockito.never()).sendMassEmail(Mockito.anyList(), Mockito.anyString(),
                Mockito.anyString());
    }

    /**
     * The study members are resolved server side, so a study administrator
     * cannot reach a user outside of the study by naming them in the payload.
     */
    @Test
    @WithMockUser(authorities = { "ROLE_EXPERT" })
    public void sendMassEmailIgnoresCallerSuppliedRecipientsTest() throws Exception {
        final List<User> members = List.of(ModelsUtil.createUser(1L));
        given(shanoirUsersManagement.hasRightOnStudy(3L, "CAN_ADMINISTRATE")).willReturn(true);
        given(massEmailService.resolveStudyRecipients(3L)).willReturn(members);

        mvc.perform(MockMvcRequestBuilders.post(SEND_PATH).accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"recipientGroup\":\"STUDY\",\"studyId\":3,\"recipientUserIds\":[7,8],"
                        + "\"subject\":\"Maintenance\",\"content\":\"Down tomorrow.\"}"))
                .andExpect(status().isAccepted())
                .andExpect(content().string("1"));

        Mockito.verify(massEmailService).sendMassEmail(members, "Maintenance", "Down tomorrow.");
    }

    @Test
    @WithMockUser(authorities = { "ROLE_ADMIN" })
    public void countRecipientsStudyGroupTest() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get(COUNT_PATH).param("group", "STUDY")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnprocessableEntity());

        Mockito.verifyNoInteractions(massEmailService);
    }

    @Test
    @WithMockUser(authorities = { "ROLE_ADMIN" })
    public void sendMassEmailBlankSubjectTest() throws Exception {
        mvc.perform(MockMvcRequestBuilders.post(SEND_PATH).accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"recipientGroup\":\"ALL\",\"subject\":\" \",\"content\":\"Down tomorrow.\"}"))
                .andExpect(status().isUnprocessableEntity());

        Mockito.verify(massEmailService, Mockito.never()).sendMassEmail(Mockito.anyList(), Mockito.anyString(),
                Mockito.anyString());
    }

    @Test
    @WithMockUser(authorities = { "ROLE_ADMIN" })
    public void sendMassEmailMissingGroupTest() throws Exception {
        mvc.perform(MockMvcRequestBuilders.post(SEND_PATH).accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"subject\":\"Maintenance\",\"content\":\"Down tomorrow.\"}"))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @WithMockUser(authorities = { "ROLE_USER" })
    public void sendMassEmailForbiddenToNonAdminTest() throws Exception {
        mvc.perform(MockMvcRequestBuilders.post(SEND_PATH).accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"recipientGroup\":\"ALL\",\"subject\":\"Maintenance\",\"content\":\"Down tomorrow.\"}"))
                .andExpect(status().isForbidden());

        Mockito.verify(massEmailService, Mockito.never()).sendMassEmail(Mockito.anyList(), Mockito.anyString(),
                Mockito.anyString());
    }

    @Test
    @WithMockUser(authorities = { "ROLE_ADMIN" })
    public void sendMassEmailKeycloakErrorTest() throws Exception {
        given(massEmailService.resolveRecipients(RecipientGroup.INACTIVE))
                .willThrow(new SecurityException("keycloak unreachable"));

        mvc.perform(MockMvcRequestBuilders.post(SEND_PATH).accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"recipientGroup\":\"INACTIVE\",\"subject\":\"Maintenance\",\"content\":\"Down tomorrow.\"}"))
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
