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

package org.shanoir.ng.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Deny by default: every endpoint declares who can call it, with @PreAuthorize or @PostAuthorize on its method, its
 * interface method or its class. An endpoint without one is open to any logged-in user, or to anyone when the
 * security configuration permits its path: a new endpoint whose check was forgotten makes this test fail.
 */
class EndpointSecurityTest {

    /** The endpoints open on purpose, with why. Do not add one without a reason. */
    private static final Map<String, String> UNGUARDED = Map.ofEntries(
            Map.entry("SwaggerController#index", "redirect to the public Swagger UI"),
            Map.entry("AccountRequestApiController#saveNewAccountRequest", "public (/accountrequest): sign-up form"),
            Map.entry("ExtensionRequestApiController#requestExtension", "public (/extensionrequest): a disabled account asks for an extension"),
            Map.entry("LastLoginDateApiController#lastLoginDate",
                    "public (/last_login_date): called by the Keycloak authenticator after each login"),
            Map.entry("UserApiController#countActiveUsers", "public (/users/count): a count shown on the welcome page"),
            Map.entry("EventsApiController#countPassedEvents", "public (/events/count): a count shown on the welcome page"),
            Map.entry("RoleApiController#findRoles", "any logged-in user: the list of the roles"),
            Map.entry("AccessRequestApiController#findAllByUserId", "any logged-in user: their own access requests"),
            Map.entry("AccessRequestApiController#saveNewAccessRequest", "any logged-in user asks access to a study, for themselves"),
            Map.entry("AccessRequestApiController#findAllByAdminId", "checked in the method: admins and experts, on the studies they administrate"),
            Map.entry("AsyncTaskApiController#updateTasks", "any logged-in user: the events of their own tasks"));

    @Test
    void everyEndpointDeclaresItsAccessRule() throws ClassNotFoundException {
        Set<String> endpoints = endpoints();
        List<String> unguarded = new ArrayList<>();
        for (String endpoint : endpoints) {
            if (!endpoint.startsWith("+") && !UNGUARDED.containsKey(endpoint)) {
                unguarded.add(endpoint);
            }
        }
        assertFalse(endpoints.isEmpty(), "No endpoint found: the scan is broken.");
        assertTrue(unguarded.isEmpty(), "Endpoints without @PreAuthorize or @PostAuthorize (add the access rule, "
                + "or add them to UNGUARDED with the reason they are open):\n" + String.join("\n", unguarded));
    }

    @Test
    void theAllowedEndpointsExistAndAreStillUnguarded() throws ClassNotFoundException {
        Set<String> endpoints = endpoints();
        for (String endpoint : UNGUARDED.keySet()) {
            assertTrue(endpoints.contains(endpoint), endpoint + " is in UNGUARDED but is not an unguarded endpoint "
                    + "any more: remove it from the list.");
        }
    }

    /** @return Controller#method of every endpoint, prefixed with + when it has an access rule */
    private static Set<String> endpoints() throws ClassNotFoundException {
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(Controller.class));
        Set<String> endpoints = new TreeSet<>();
        for (BeanDefinition definition : scanner.findCandidateComponents("org.shanoir")) {
            Class<?> controller = ClassUtils.forName(definition.getBeanClassName(), EndpointSecurityTest.class.getClassLoader());
            boolean classGuarded = guarded(controller);
            for (Method method : ReflectionUtils.getUniqueDeclaredMethods(controller, ReflectionUtils.USER_DECLARED_METHODS)) {
                if (AnnotatedElementUtils.hasAnnotation(method, RequestMapping.class)) {
                    boolean isGuarded = classGuarded || guarded(method);
                    endpoints.add((isGuarded ? "+" : "") + controller.getSimpleName() + "#" + method.getName());
                }
            }
        }
        return endpoints;
    }

    /** As Spring Security, the annotations of the interfaces count. */
    private static boolean guarded(AnnotatedElement element) {
        return AnnotatedElementUtils.hasAnnotation(element, PreAuthorize.class)
                || AnnotatedElementUtils.hasAnnotation(element, PostAuthorize.class);
    }

}
