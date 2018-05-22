package org.shanoir.ng.preclinical.pathologies;

import java.util.List;



/**
 * Custom repository for pathologies.
 * 
 * @author sloury
 *
 */
public interface PathologyRepositoryCustom {
	
	List<Pathology> findBy(String fieldName, Object value);
	

}
