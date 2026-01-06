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

package org.shanoir.ng.extensionrequest;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.shanoir.ng.email.EmailService;
import org.shanoir.ng.extensionrequest.controller.ExtensionRequestApiController;
import org.shanoir.ng.extensionrequest.model.ExtensionRequestInfo;
import org.shanoir.ng.shared.jackson.JacksonUtils;
import org.shanoir.ng.user.model.User;
import org.shanoir.ng.user.repository.UserRepository;
import org.shanoir.ng.user.security.UserFieldEditionSecurityManager;
import org.shanoir.ng.user.service.UserService;
import org.shanoir.ng.user.service.UserUniqueConstraintManager;
import org.shanoir.ng.user.utils.KeycloakClient;
import org.shanoir.ng.utils.ModelsUtil;
import org.shanoir.ng.utils.usermock.WithMockKeycloakUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

/**
 * Test class for ExtensionRequestApiController
 * @author fli
 *
 */
@WebMvcTest(controllers = {ExtensionRequestApiController.class, UserUniqueConstraintManager.class, UserRepository.class})
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
public class ExtensionRequestApiControllerTest {

    private static final String REQUEST_PATH = "/extensionrequest";

    private static final String KEYCLOAK_ID = "KEYCLOAK_ID";

    private static final String EMAIL = "test@gmail.com";

    private static final String PASSWORD = "password";

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private KeycloakClient keycloakClient;

    @MockitoBean
    private EmailService emailService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private UserFieldEditionSecurityManager fieldEditionSecurityManager;

    @MockitoBean
    private UserUniqueConstraintManager uniqueConstraintManager;


    @Test
    @WithMockKeycloakUser(authorities = { "ROLE_ADMIN" }, id = 0)
    public void extensionRequestTest() throws Exception {
        // GIVEN a disabled user with no current extension request
        User mockUser = ModelsUtil.createUser(1L);
        mockUser.setExpirationDate(LocalDate.now().minusDays(1));
        mockUser.setExtensionRequestDemand(Boolean.FALSE);
        mockUser.setKeycloakId(KEYCLOAK_ID);
        Mockito.when(userService.findByEmailForExtension(EMAIL)).thenReturn(Optional.of(mockUser));
        Mockito.when(keycloakClient.resetPassword(KEYCLOAK_ID)).thenReturn(PASSWORD);

        // WHEN we request for an extension
        ExtensionRequestInfo extensionRequest = new ExtensionRequestInfo();
        extensionRequest.setEmail(EMAIL);
        mvc.perform(MockMvcRequestBuilders.post(REQUEST_PATH)
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON)
                .content(JacksonUtils.serialize(extensionRequest)))
                .andExpect(status().isOk());

        // THEN an extension is requested and password is changed
        Mockito.verify(keycloakClient).resetPassword(mockUser.getKeycloakId());
        Mockito.verify(emailService).notifyUserResetPassword(Mockito.eq(mockUser), Mockito.anyString());
        Mockito.verify(userService).requestExtension(Mockito.eq(mockUser.getId()), Mockito.any(ExtensionRequestInfo.class));
    }

    @Test
    @WithMockUser(authorities = { "ROLE_ADMIN" })
    public void extensionRequestNoUserTest() throws Exception {
        // GIVEN a non existing user
        Mockito.when(userService.findByEmailForExtension(EMAIL)).thenReturn(Optional.ofNullable(null));

        // WHEN we request for an extension
        ExtensionRequestInfo extensionRequest = new ExtensionRequestInfo();
        extensionRequest.setEmail(EMAIL);
        mvc.perform(MockMvcRequestBuilders.post(REQUEST_PATH)
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON)
                .content(JacksonUtils.serialize(extensionRequest)))
                .andExpect(status().isBadRequest());

        // THEN a 404 is returned
        Mockito.verifyNoInteractions(keycloakClient);
        Mockito.verifyNoInteractions(emailService);
    }

    @Test
    @WithMockUser(authorities = { "ROLE_ADMIN" })
    public void extensionRequestUserEnabledTest() throws Exception {
        // GIVEN an ENABLED user with no current extension request
        User mockUser = ModelsUtil.createUser(1L);
        mockUser.setExpirationDate(LocalDate.now().plusDays(1));
        Mockito.when(userService.findByEmailForExtension(EMAIL)).thenReturn(Optional.of(mockUser));

        // WHEN we request for an extension
        ExtensionRequestInfo extensionRequest = new ExtensionRequestInfo();
        extensionRequest.setEmail(EMAIL);
        mvc.perform(MockMvcRequestBuilders.post(REQUEST_PATH)
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON)
                .content(JacksonUtils.serialize(extensionRequest)))
                .andExpect(status().isNotAcceptable());

        // THEN a NOT acceptable error is sent
        Mockito.verifyNoInteractions(keycloakClient);
        Mockito.verifyNoInteractions(emailService);    }

    @Test
    @WithMockUser(authorities = { "ROLE_ADMIN" })
    public void extensionRequestUserAreadyHaveARequestTest() throws Exception {
        // GIVEN a disabled user with a current extension request
        User mockUser = ModelsUtil.createUser(1L);
        mockUser.setExpirationDate(LocalDate.now().minusDays(1));
        mockUser.setExtensionRequestDemand(Boolean.TRUE);
        Mockito.when(userService.findByEmailForExtension(EMAIL)).thenReturn(Optional.of(mockUser));

        // WHEN we request for an extension
        ExtensionRequestInfo extensionRequest = new ExtensionRequestInfo();
        extensionRequest.setEmail(EMAIL);
        mvc.perform(MockMvcRequestBuilders.post(REQUEST_PATH)
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON)
                .content(JacksonUtils.serialize(extensionRequest)))
                .andExpect(status().isNotAcceptable());

        // THEN a NOT acceptable error is sent
        Mockito.verifyNoInteractions(keycloakClient);
        Mockito.verifyNoInteractions(emailService);
    }
}
