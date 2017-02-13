package org.shanoir.ng.model.error;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;


public class FieldErrorMap extends HashMap<String, List<FieldError>> {

	private static final long serialVersionUID = 1L;


    /**
	 * Constructor
	 */
	public FieldErrorMap() {
		super();
	}


	/**
	 * Constructor
	 */
	public FieldErrorMap(FieldErrorMap... maps) {
		this.merge(maps);
	}


	/**
     * Constructor
     *
     * @param {@link BindingResult}
     * @return {@link FieldErrorMap}
     */
    public FieldErrorMap(BindingResult result) {
        if (result.hasErrors()) {
        	for (ObjectError objectError : result.getAllErrors()) {
        		org.springframework.validation.FieldError fieldError = (org.springframework.validation.FieldError) objectError;
        		if (!this.containsKey(fieldError.getField())) {
        			this.put(fieldError.getField(), new ArrayList<org.shanoir.ng.model.error.FieldError>());
        		}
        		this.get(fieldError.getField()).add(new org.shanoir.ng.model.error.FieldError(
        				fieldError.getCode(), fieldError.getDefaultMessage(), fieldError.getRejectedValue()));
        	}
        }
    }
    
	/**
     * Tell Spring to remove the hibernante validation error on username blank
     *
     * @param {@link BindingResult}
     * @return {@link FieldErrorMap}
     */
    public static FieldErrorMap fieldErrorMapIgnoreUsernameBlank(BindingResult result) {
    	FieldErrorMap fieldErrorMap = new FieldErrorMap();
        if (result.hasErrors()) {
        	for (ObjectError objectError : result.getAllErrors()) {
        		org.springframework.validation.FieldError fieldError = (org.springframework.validation.FieldError) objectError;
        		if (!fieldError.getField().equals("username") || fieldError.getRejectedValue() != null) {
	        		if (!fieldErrorMap.containsKey(fieldError.getField())) {
	        			fieldErrorMap.put(fieldError.getField(), new ArrayList<org.shanoir.ng.model.error.FieldError>());
	        		}
	        		fieldErrorMap.get(fieldError.getField()).add(new org.shanoir.ng.model.error.FieldError(
	        				fieldError.getCode(), fieldError.getDefaultMessage(), fieldError.getRejectedValue()));
        		}
        	}
        }
        return fieldErrorMap;
    }
    
	/**
     * Tell Spring to remove the hibernante validation error on username or role blank for user account request
     *
     * @param {@link BindingResult}
     * @return {@link FieldErrorMap}
     */
    public static FieldErrorMap fieldErrorMapIgnoreUsernameAndRoleBlank(BindingResult result) {
    	FieldErrorMap fieldErrorMap = new FieldErrorMap();
        if (result.hasErrors()) {
        	for (ObjectError objectError : result.getAllErrors()) {
        		org.springframework.validation.FieldError fieldError = (org.springframework.validation.FieldError) objectError;
        		if ((!fieldError.getField().equals("role") && !fieldError.getField().equals("username")) || fieldError.getRejectedValue() != null) {
	        		if (!fieldErrorMap.containsKey(fieldError.getField())) {
	        			fieldErrorMap.put(fieldError.getField(), new ArrayList<org.shanoir.ng.model.error.FieldError>());
	        		}
	        		fieldErrorMap.get(fieldError.getField()).add(new org.shanoir.ng.model.error.FieldError(
	        				fieldError.getCode(), fieldError.getDefaultMessage(), fieldError.getRejectedValue()));
        		}
        	}
        }
        return fieldErrorMap;
    }



    /**
     * Merge errors properly
     *
     * @param maps
     */
    public void merge(FieldErrorMap... maps) {
    	for (FieldErrorMap map : maps) {
			for (String fieldName : map.keySet()) {
				List<FieldError> error = map.get(fieldName);
				if (!this.containsKey(fieldName)) {
					this.put(fieldName, error);
				} else {
					this.get(fieldName).addAll(error);
				}
			}
		}
    }
}
