package org.shanoir.ng.controller;

import org.shanoir.ng.model.error.ErrorModel;
import org.shanoir.ng.model.exception.RestServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author jlouis
 */
@ControllerAdvice
@RestController
public class GlobalExceptionHandler {

	private static final Logger LOG = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(value = RestServiceException.class)
    public ResponseEntity<ErrorModel> handleRestServiceException(RestServiceException e){
    	LOG.error("Error in the rest service. ", e);
    	return new ResponseEntity<ErrorModel>(e.getErrorModel(), HttpStatus.valueOf(e.getErrorModel().getCode()));
    }

    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<ErrorModel> handleException(Exception e){
    	e.printStackTrace();
    	ErrorModel error = new ErrorModel(500, e.toString());
    	LOG.error("Unexpected error in the rest service. ", e);
    	return new ResponseEntity<ErrorModel>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

