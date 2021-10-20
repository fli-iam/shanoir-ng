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

package org.shanoir.ng.extensionrequest.controller;

import org.shanoir.ng.email.EmailService;
import org.shanoir.ng.extensionrequest.model.ExtensionRequestInfo;
import org.shanoir.ng.shared.controller.AbstractUserRequestApiController;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.user.model.User;
import org.shanoir.ng.user.utils.KeycloakClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
public class ExtensionRequestApiController extends AbstractUserRequestApiController implements ExtensionRequestApi {

	private static final Logger LOG = LoggerFactory.getLogger(ExtensionRequestApiController.class);

	@Autowired
	private KeycloakClient keycloakClient;

	@Autowired
	private EmailService emailService;

	@Override
	public ResponseEntity<Void> requestExtension(@RequestBody final ExtensionRequestInfo requestInfo) {
		try {
			User userToExtend = getUserService().findByEmailForExtension(requestInfo.getEmail()).orElseThrow(() -> new EntityNotFoundException(requestInfo.getEmail()));
			// Check that the user is well disabled or that at least the first notification email was sent, see #923
			if (userToExtend.isEnabled() && (userToExtend.isFirstExpirationNotificationSent() == null || !userToExtend.isFirstExpirationNotificationSent().booleanValue())) {
				throw new ShanoirException("This user is not disabled, please enter an email of a disabled account. If you forgot your password, please return to login page and follow the adapted link.");
			}
			if (userToExtend.isExtensionRequestDemand().booleanValue()) {
				throw new ShanoirException("An extension has already been requested for this user, please be patient of contact an administrator to accept the extension request.");
			}
			LOG.info("Resetting password after extension request for user {}", userToExtend.getUsername());

			getUserService().requestExtension(userToExtend.getId(), requestInfo);
			String password = keycloakClient.resetPassword(userToExtend.getKeycloakId());
			emailService.notifyUserResetPassword(userToExtend, password);

			return new ResponseEntity<>(HttpStatus.OK);
		} catch (EntityNotFoundException e) {
			LOG.error("User with email {} not found", requestInfo.getEmail(), e);
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		} catch (ShanoirException e) {
			LOG.error(e.getMessage(), e);
			return new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);
		}
	}

}
