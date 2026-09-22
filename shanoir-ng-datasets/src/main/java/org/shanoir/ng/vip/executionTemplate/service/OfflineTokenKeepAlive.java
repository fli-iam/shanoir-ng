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

package org.shanoir.ng.vip.executionTemplate.service;

import org.shanoir.ng.shared.exception.SecurityException;
import org.shanoir.ng.shared.security.KeycloakServiceAccountUtils;
import org.shanoir.ng.vip.executionTemplate.model.ExecutionTemplate;
import org.shanoir.ng.vip.executionTemplate.repository.ExecutionTemplateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Keeps the offline tokens stored on execution templates from idle-expiring.
 *
 * Keycloak's "Offline Session Idle" timeout (30 days in our realm) revokes an offline token if it goes unused
 * for that long, and there is no absolute max lifespan. A template that fires regularly stays alive on its own,
 * but one left dormant for over a month would have its token die, breaking its next auto-execution.
 *
 * This weekly job simply refreshes each stored token, which resets the idle timer server-side. Token rotation is
 * off in our realm (revokeRefreshToken=false), so the token string never changes and nothing needs to be persisted
 * here. A token that is already dead (revoked, or expired before this job could touch it) cannot be revived: it is
 * only logged, and the study admin must re-authenticate the template.
 */
@Service
public class OfflineTokenKeepAlive {

    private static final Logger LOG = LoggerFactory.getLogger(OfflineTokenKeepAlive.class);

    @Autowired
    private ExecutionTemplateRepository executionTemplateRepository;

    @Autowired
    private KeycloakServiceAccountUtils keycloakServiceAccountUtils;

    @Scheduled(cron = "0 0 3 * * MON", zone = "Europe/Paris")
    public void keepOfflineTokensAlive() {
        for (ExecutionTemplate template : executionTemplateRepository.findByOfflineTokenNotNull()) {
            try {
                keycloakServiceAccountUtils.refreshUserToken(template.getOfflineToken());
            } catch (SecurityException e) {
                LOG.warn("Could not refresh offline token for template {} (may be expired or revoked); "
                        + "study admin must re-authenticate it.", template.getId(), e);
            }
        }
    }
}
