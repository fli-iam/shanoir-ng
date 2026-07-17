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

package org.shanoir.ng.massemail.controller;

import org.shanoir.ng.massemail.model.RecipientGroup;
import org.shanoir.ng.massemail.service.MassEmailService;
import org.shanoir.ng.shared.exception.ErrorModel;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.shared.exception.SecurityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

/**
 * Implementation of the mass email api.
 *
 * @author afragkiadakis
 */
@Controller
public class MassEmailApiController implements MassEmailApi {

    private static final Logger LOG = LoggerFactory.getLogger(MassEmailApiController.class);

    @Autowired
    private MassEmailService massEmailService;

    @Override
    public ResponseEntity<Integer> countRecipients(final RecipientGroup group) throws RestServiceException {
        try {
            return new ResponseEntity<>(massEmailService.countRecipients(group), HttpStatus.OK);
        } catch (SecurityException e) {
            LOG.error("Could not count the {} mass email recipients", group, e);
            throw new RestServiceException(
                    new ErrorModel(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Could not read the users enabled status from Keycloak."));
        }
    }

}
