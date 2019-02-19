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

package org.shanoir.challengeScores.controller;

import org.shanoir.challengeScores.data.model.exception.RestServiceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.model.ErrorModel;

/**
 * @author jlouis
 */
@ControllerAdvice
@RestController
public class GlobalExceptionHandler {

    @ExceptionHandler(value = RestServiceException.class)
    public ResponseEntity<ErrorModel> handleRestServiceException(RestServiceException e){
    	e.printStackTrace();
    	return new ResponseEntity<ErrorModel>(e.toErrorModel(), HttpStatus.valueOf(e.getCode()));
    }

    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<ErrorModel> handleException(Exception e){
    	e.printStackTrace();
    	ErrorModel error = new ErrorModel().code(500).message(e.toString());
    	return new ResponseEntity<ErrorModel>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

