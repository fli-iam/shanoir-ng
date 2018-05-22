package org.shanoir.ng.preclinical.therapies;

import java.util.List;



/**
 * Custom repository for therapies.
 * 
 * @author sloury
 *
 */
public interface TherapyRepositoryCustom {
	
	List<Therapy> findBy(String fieldName, Object value);
	

}
