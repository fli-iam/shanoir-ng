package org.shanoir.ng.preclinical.anesthetics.anesthetic;

import java.util.List;

/**
 * Custom repository for anesthetic.
 * 
 * @author sloury
 *
 */
public interface AnestheticRepositoryCustom {
	
	List<Anesthetic> findBy(String fieldName, Object value);

}
