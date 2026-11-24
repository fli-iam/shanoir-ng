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

package org.shanoir.ng.preclinical.contrast_agents;

import static org.shanoir.ng.utils.assertion.AssertUtils.assertAccessAuthorized;
import static org.shanoir.ng.utils.assertion.AssertUtils.assertAccessDenied;

import org.junit.jupiter.api.Test;
import org.shanoir.ng.preclinical.contrast_agent.ContrastAgent;
import org.shanoir.ng.preclinical.contrast_agent.ContrastAgentApiController;
import org.shanoir.ng.preclinical.contrast_agent.ContrastAgentEditableByManager;
import org.shanoir.ng.preclinical.contrast_agent.ContrastAgentService;
import org.shanoir.ng.preclinical.contrast_agent.ContrastAgentUniqueValidator;
import org.shanoir.ng.preclinical.references.RefsService;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.utils.assertion.AccessCheckedFunction0Arg;
import org.shanoir.ng.utils.usermock.WithMockKeycloakUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.validation.BeanPropertyBindingResult;

/**
 * The contrast agents are not used by the pages and their protocols are not linked to a study: admins only.
 */
@SpringBootTest
@ActiveProfiles("test")
public class ContrastAgentApiSecurityTest {

    @Autowired
    private ContrastAgentApiController controller;

    @MockitoBean
    private ContrastAgentService contrastAgentService;

    @MockitoBean
    private RefsService referencesService;

    @MockitoBean
    private ContrastAgentUniqueValidator uniqueValidator;

    @MockitoBean
    private ContrastAgentEditableByManager editableOnlyValidator;

    @Test
    @WithAnonymousUser
    public void testAsAnonymous() throws ShanoirException {
        checkAll(false);
    }

    @Test
    @WithMockKeycloakUser(id = 2L, authorities = { "ROLE_USER" })
    public void testAsUser() throws ShanoirException {
        checkAll(false);
    }

    @Test
    @WithMockKeycloakUser(id = 2L, authorities = { "ROLE_EXPERT" })
    public void testAsExpert() throws ShanoirException {
        checkAll(false);
    }

    @Test
    @WithMockKeycloakUser(id = 2L, authorities = { "ROLE_ADMIN" })
    public void testAsAdmin() throws ShanoirException {
        checkAll(true);
    }

    private void checkAll(boolean authorized) throws ShanoirException {
        check(authorized, () -> controller.createContrastAgent(1L, new ContrastAgent(), bindingResult()));
        check(authorized, () -> controller.deleteContrastAgent(1L, 1L));
        check(authorized, () -> controller.getContrastAgentByProtocolId(1L));
        check(authorized, () -> controller.getContrastAgentById(1L, 1L));
        check(authorized, () -> controller.getContrastAgentByName(1L, "agent"));
        check(authorized, () -> controller.getContrastAgents(1L));
        check(authorized, () -> controller.updateContrastAgent(1L, 1L, new ContrastAgent(), bindingResult()));
    }

    private static void check(boolean authorized, AccessCheckedFunction0Arg call) throws ShanoirException {
        if (authorized) {
            assertAccessAuthorized(call);
        } else {
            assertAccessDenied(call);
        }
    }

    private static BeanPropertyBindingResult bindingResult() {
        return new BeanPropertyBindingResult(new ContrastAgent(), "contrastagent");
    }

}
