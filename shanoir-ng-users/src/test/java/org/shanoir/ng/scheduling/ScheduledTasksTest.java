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

package org.shanoir.ng.scheduling;

import static org.mockito.BDDMockito.given;

import java.time.LocalDate;
import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.shanoir.ng.email.EmailService;
import org.shanoir.ng.user.model.User;
import org.shanoir.ng.user.service.UserService;
import org.shanoir.ng.user.utils.KeycloakClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.aot.DisabledInAotMode;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * Test class for ShcheduledTasks class
 * @author fli
 *
 */
@SpringBootTest
@ActiveProfiles("test")
@DisabledInAotMode
public class ScheduledTasksTest {

    @MockitoBean
    private EmailService emailService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private KeycloakClient keycloakClient;

    @Autowired
    private ScheduledTasks scheduledTasks;

    @Test
    public void testCheckExpirationDateExact() {
        // GIVEN three user with near expiration dates
        User user3 = new User();
        user3.setExpirationDate(LocalDate.now());

        given(userService.getUsersToReceiveFirstExpirationNotification()).willReturn(Collections.emptyList());
        given(userService.getUsersToReceiveSecondExpirationNotification()).willReturn(Collections.emptyList());
        given(userService.getExpiredUsers()).willReturn(Collections.singletonList(user3));

        // WHEN we check expiration dates
        scheduledTasks.checkExpirationDate();

        // THEN user3 is updated in keycloak database
        Mockito.verify(keycloakClient).updateUser(user3);
    }

    @Test
    public void testCheckExpirationDateFirst() {
        // GIVEN three user with near expiration dates
        User user1 = new User();
        user1.setExpirationDate(LocalDate.now().minusWeeks(1));

        given(userService.getUsersToReceiveFirstExpirationNotification()).willReturn(Collections.singletonList(user1));
        given(userService.getUsersToReceiveSecondExpirationNotification()).willReturn(Collections.emptyList());
        given(userService.getExpiredUsers()).willReturn(Collections.emptyList());

        // WHEN we check expiration dates
        scheduledTasks.checkExpirationDate();

        // THEN first expiration warning is sent to user1
        Mockito.verify(userService).updateExpirationNotification(user1, true);
        Mockito.verify(emailService).notifyAccountWillExpire(user1);
    }

    @Test
    public void testCheckExpirationDateSecond() {
        // GIVEN three user with near expiration dates
        User user2 = new User();
        user2.setExpirationDate(LocalDate.now().minusDays(2));

        given(userService.getUsersToReceiveFirstExpirationNotification()).willReturn(Collections.emptyList());
        given(userService.getUsersToReceiveSecondExpirationNotification()).willReturn(Collections.singletonList(user2));
        given(userService.getExpiredUsers()).willReturn(Collections.emptyList());

        // WHEN we check expiration dates
        scheduledTasks.checkExpirationDate();

        // THEN second expiration warning is sent to user2
        Mockito.verify(userService).updateExpirationNotification(user2, false);
        Mockito.verify(emailService).notifyAccountWillExpire(user2);

    }
}
