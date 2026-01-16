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

package org.shanoir.ng.accountrequest;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.shanoir.ng.ShanoirUsersManagement;
import org.shanoir.ng.accessrequest.controller.AccessRequestService;
import org.shanoir.ng.accountrequest.controller.AccountRequestApiController;
import org.shanoir.ng.accountrequest.model.AccountRequestInfo;
import org.shanoir.ng.email.EmailService;
import org.shanoir.ng.shared.error.FieldErrorMap;
import org.shanoir.ng.shared.event.ShanoirEventService;
import org.shanoir.ng.shared.exception.SecurityException;
import org.shanoir.ng.study.rights.StudyUserRightsRepository;
import org.shanoir.ng.user.model.User;
import org.shanoir.ng.user.repository.UserRepository;
import org.shanoir.ng.user.security.UserFieldEditionSecurityManager;
import org.shanoir.ng.user.service.UserService;
import org.shanoir.ng.user.service.UserUniqueConstraintManager;
import org.shanoir.ng.utils.ModelsUtil;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.oauth2.server.resource.autoconfigure.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import tools.jackson.databind.json.JsonMapper;

/**
 * Unit tests for AccountRequestApiController.
 *
 * @author msimon
 *
 */
@WebMvcTest(
        controllers = AccountRequestApiController.class,
        excludeAutoConfiguration = {
            OAuth2ResourceServerAutoConfiguration.class
        }
)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
public class AccountRequestApiControllerTest {

    private static final String REQUEST_PATH = "/accountrequest";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JsonMapper mapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private ShanoirUsersManagement shanoirUsersManagement;

    @MockitoBean
    private JavaMailSender mailSender;

    @MockitoBean
    private RabbitTemplate rabbitTemplate;

    @MockitoBean
    private ShanoirEventService shanoirEventService;

    @MockitoBean
    private EmailService emailService;

    @MockitoBean
    private AccessRequestService accessRequestService;

    @MockitoBean
    private StudyUserRightsRepository studyUserRightsRepository;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private UserFieldEditionSecurityManager fieldEditionSecurityManager;

    @MockitoBean
    private UserUniqueConstraintManager uniqueConstraintManager;

    @BeforeAll
    public static void beforeAll() {
        System.setProperty("vip.enabled", "false");
    }

    @BeforeEach
    public void setup() throws SecurityException {
        given(fieldEditionSecurityManager.validate(Mockito.any(User.class))).willReturn(new FieldErrorMap());
        given(uniqueConstraintManager.validate(Mockito.any(User.class))).willReturn(new FieldErrorMap());
    }

    @Test
    public void saveNewAccountRequestTest() throws Exception {
        final User user = ModelsUtil.createUser(null);
        user.setEmail("test@te.st");
        user.setUsername("test");
        final AccountRequestInfo info = new AccountRequestInfo();
        info.setContact("contact");
        info.setFunction("function");
        info.setInstitution("institution");
        info.setStudyId(1L);
        user.setAccountRequestInfo(info);

        given(userService.createAccountRequest(Mockito.mock(User.class))).willReturn(new User());

        mvc.perform(MockMvcRequestBuilders.post(REQUEST_PATH).accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(user)))
                .andExpect(status().isNoContent());
    }

    @Test
    public void saveNewAccountRequestChallengeTest() throws Exception {
        final User user = ModelsUtil.createUser(null);
        user.setEmail("test@te.st");
        user.setUsername("test");
        user.setId(2L);
        final AccountRequestInfo info = new AccountRequestInfo();
        info.setContact("contact");
        info.setFunction("function");
        info.setInstitution("institution");
        info.setStudyId(1L);
        user.setAccountRequestInfo(info);

        given(userService.createAccountRequest(Mockito.any(User.class))).willReturn(user);

        mvc.perform(MockMvcRequestBuilders.post(REQUEST_PATH).accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(user)))
                .andExpect(status().isNoContent());

    }

}
