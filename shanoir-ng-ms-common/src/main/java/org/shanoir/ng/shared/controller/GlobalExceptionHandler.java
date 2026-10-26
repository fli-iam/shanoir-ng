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

package org.shanoir.ng.shared.controller;

import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.ErrorModel;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * Global exception handler.
 *
 * @author jlouis
 */
@ControllerAdvice
@RestController
public class GlobalExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(value = RestServiceException.class)
    public ResponseEntity<ErrorModel> handleRestServiceException(final RestServiceException e) {
        LOG.warn("Error in the rest service. ", e);
        return new ResponseEntity<>(e.getErrorModel(), HttpStatus.valueOf(e.getErrorModel().getCode()));
    }

    @ExceptionHandler(value = AccessDeniedException.class)
    public ResponseEntity<ErrorModel> handleAccessDeniedException(final AccessDeniedException e) {
        final ErrorModel error = new ErrorModel(HttpStatus.FORBIDDEN.value(), e.getMessage());
        LOG.warn("Acces denied in the rest service. ", e);
        return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(value = EntityNotFoundException.class)
    public ResponseEntity<ErrorModel> handleEntityNotFoundException(final EntityNotFoundException e) {
        final ErrorModel error = new ErrorModel(HttpStatus.NOT_FOUND.value(), e.getMessage());
        LOG.warn("Entity not found in the rest service. ", e);
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(value = MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorModel> handleMethodArgumentTypeMismatchException(final MethodArgumentTypeMismatchException e) {
        final ErrorModel error = new ErrorModel(HttpStatus.BAD_REQUEST.value(), e.getMessage());
        LOG.warn("Bad request in the rest service: {}", e.getMessage());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<ErrorModel> handleException(final Exception e) {

        if (e.getCause() instanceof RestServiceException) {
            return this.handleRestServiceException((RestServiceException) e.getCause());
        }

        // Spring MVC client errors (unknown URL, unsupported method or media type, missing parameter...) carry their own status.
        if (e instanceof ErrorResponse errorResponse && errorResponse.getStatusCode().is4xxClientError()) {
            final HttpStatusCode status = errorResponse.getStatusCode();
            final ErrorModel error = new ErrorModel(status.value(), errorResponse.getBody().getDetail());
            LOG.warn("Client error in the rest service: {}", e.getMessage());
            return new ResponseEntity<>(error, status);
        }

        final ErrorModel error = new ErrorModel(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage());
        LOG.error("Unexpected error in the rest service. ", e);
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
