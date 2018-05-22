package org.shanoir.ng.preclinical.extra_data;

import java.util.List;

/**
 * Custom repository for extra data.
 * 
 * @author sloury
 *
 */
public interface ExtraDataRepositoryCustom<T> {
	
	 public List<T> findAllByExaminationId(Long id);
	  
	 public List<T> findBy(String fieldName, Object value);
		

}