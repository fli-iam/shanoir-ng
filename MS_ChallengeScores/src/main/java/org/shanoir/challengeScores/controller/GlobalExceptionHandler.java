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

