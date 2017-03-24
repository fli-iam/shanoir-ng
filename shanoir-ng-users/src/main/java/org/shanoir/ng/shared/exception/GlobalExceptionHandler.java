package org.shanoir.ng.shared.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
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
	public ResponseEntity<ErrorModel> handleRestServiceException(final RestServiceException e) {
		LOG.warn("Error in the rest service. ", e);
		return new ResponseEntity<ErrorModel>(e.getErrorModel(), HttpStatus.valueOf(e.getErrorModel().getCode()));
	}

	@ExceptionHandler(value = AccessDeniedException.class)
	public ResponseEntity<ErrorModel> handleAccessDeniedException(final AccessDeniedException e) {
		final ErrorModel error = new ErrorModel(HttpStatus.FORBIDDEN.value(), e.getMessage());
		return new ResponseEntity<ErrorModel>(error, HttpStatus.FORBIDDEN);
	}

	@ExceptionHandler(value = Exception.class)
	public ResponseEntity<ErrorModel> handleException(final Exception e) {
		final ErrorModel error = new ErrorModel(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage());
		LOG.error("Unexpected error in the rest service. ", e);
		return new ResponseEntity<ErrorModel>(error, HttpStatus.INTERNAL_SERVER_ERROR);
	}

}
