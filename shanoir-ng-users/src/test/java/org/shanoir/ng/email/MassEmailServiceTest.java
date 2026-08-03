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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.shanoir.ng.email.model.RecipientGroup;
import org.shanoir.ng.shared.exception.SecurityException;
import org.shanoir.ng.study.rights.StudyUser;
import org.shanoir.ng.study.rights.StudyUserRightsRepository;
import org.shanoir.ng.user.model.User;
import org.shanoir.ng.user.repository.UserRepository;
import org.shanoir.ng.user.utils.KeycloakClient;
import org.shanoir.ng.utils.ModelsUtil;

/**
 * Unit tests for {@link MassEmailServiceImpl} recipient resolution.
 *
 * @author afragkiadakis
 */
@ExtendWith(MockitoExtension.class)
public class MassEmailServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private KeycloakClient keycloakClient;

    @Mock
    private EmailService emailService;

    @Mock
    private StudyUserRightsRepository studyUserRightsRepository;

    @InjectMocks
    private MassEmailServiceImpl massEmailService;

    private User enabledUser;

    private User disabledUser;

    private User unknownToKeycloakUser;

    private User noKeycloakIdUser;

    private User noEmailUser;

    private User pendingAccountRequestUser;

    @BeforeEach
    public void setup() {
        enabledUser = user(1L, "enabled@shanoir.fr", "kc-enabled", null);
        disabledUser = user(2L, "disabled@shanoir.fr", "kc-disabled", null);
        unknownToKeycloakUser = user(3L, "unknown@shanoir.fr", "kc-unknown", null);
        noKeycloakIdUser = user(4L, "nokeycloak@shanoir.fr", null, null);
        noEmailUser = user(5L, null, "kc-noemail", null);
        pendingAccountRequestUser = user(6L, "pending@shanoir.fr", "kc-pending", Boolean.TRUE);

        // lenient: the sendMassEmail delegation test does not resolve recipients
        Mockito.lenient().when(userRepository.findAll()).thenReturn(List.of(enabledUser, disabledUser,
                unknownToKeycloakUser, noKeycloakIdUser, noEmailUser, pendingAccountRequestUser));
    }

    @Test
    public void resolveRecipientsAllTest() throws SecurityException {
        final List<User> recipients = massEmailService.resolveRecipients(RecipientGroup.ALL);

        assertEquals(List.of(1L, 2L, 3L, 4L), ids(recipients));
        // resolving ALL must not need any Keycloak request
        Mockito.verifyNoInteractions(keycloakClient);
    }

    @Test
    public void resolveRecipientsActiveTest() throws SecurityException {
        given(keycloakClient.getUsersEnabledStatus()).willReturn(enabledStatus());

        final List<User> recipients = massEmailService.resolveRecipients(RecipientGroup.ACTIVE);

        assertEquals(List.of(1L), ids(recipients));
    }

    @Test
    public void resolveRecipientsInactiveTest() throws SecurityException {
        given(keycloakClient.getUsersEnabledStatus()).willReturn(enabledStatus());

        final List<User> recipients = massEmailService.resolveRecipients(RecipientGroup.INACTIVE);

        // disabled in Keycloak, unknown to Keycloak and without keycloak id are all inactive
        assertEquals(List.of(2L, 3L, 4L), ids(recipients));
    }

    @Test
    public void countRecipientsTest() throws SecurityException {
        given(keycloakClient.getUsersEnabledStatus()).willReturn(enabledStatus());

        assertEquals(4, massEmailService.countRecipients(RecipientGroup.ALL));
        assertEquals(1, massEmailService.countRecipients(RecipientGroup.ACTIVE));
        assertEquals(3, massEmailService.countRecipients(RecipientGroup.INACTIVE));
    }

    @Test
    public void sendMassEmailDelegatesToEmailServiceTest() {
        final List<User> recipients = List.of(enabledUser);

        massEmailService.sendMassEmail(recipients, "[Shanoir] Maintenance", "Service unavailable.");

        Mockito.verify(emailService).sendMassEmail(recipients, "[Shanoir] Maintenance", "Service unavailable.");
    }

    @Test
    public void resolveStudyRecipientsTest() {
        given(studyUserRightsRepository.findByStudyId(7L))
                .willReturn(List.of(studyUser(1L), studyUser(5L), studyUser(6L)));
        given(userRepository.findAllById(List.of(1L, 5L, 6L)))
                .willReturn(List.of(enabledUser, noEmailUser, pendingAccountRequestUser));

        final List<User> recipients = massEmailService.resolveStudyRecipients(7L);

        // no email and pending account request are excluded, like for named groups
        assertEquals(List.of(1L), ids(recipients));
        Mockito.verifyNoInteractions(keycloakClient);
    }

    @Test
    public void resolveStudyRecipientsWithoutMemberTest() {
        given(studyUserRightsRepository.findByStudyId(7L)).willReturn(List.of());

        assertEquals(List.of(), massEmailService.resolveStudyRecipients(7L));

        Mockito.verify(userRepository, Mockito.never()).findAllById(Mockito.anyList());
    }

    @Test
    public void resolveRecipientsKeycloakErrorTest() throws SecurityException {
        given(keycloakClient.getUsersEnabledStatus()).willThrow(new SecurityException("keycloak unreachable"));

        assertThrows(SecurityException.class, () -> massEmailService.resolveRecipients(RecipientGroup.ACTIVE));
    }

    private Map<String, Boolean> enabledStatus() {
        return Map.of("kc-enabled", Boolean.TRUE, "kc-disabled", Boolean.FALSE, "kc-noemail", Boolean.TRUE,
                "kc-pending", Boolean.TRUE);
    }

    private List<Long> ids(final List<User> users) {
        return users.stream().map(User::getId).sorted().collect(Collectors.toList());
    }

    private StudyUser studyUser(final Long userId) {
        final StudyUser studyUser = new StudyUser();
        studyUser.setUserId(userId);
        return studyUser;
    }

    private User user(final Long id, final String email, final String keycloakId, final Boolean accountRequestDemand) {
        final User user = ModelsUtil.createUser(id);
        user.setEmail(email);
        user.setKeycloakId(keycloakId);
        user.setAccountRequestDemand(accountRequestDemand);
        return user;
    }

}
