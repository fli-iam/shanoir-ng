package org.shanoir.ng.preclinical.pathologies.pathology_models;

import java.util.List;

import org.shanoir.ng.preclinical.pathologies.Pathology;



/**
 * Custom repository for pathology models.
 * 
 * @author sloury
 *
 */
public interface PathologyModelRepositoryCustom {
	
	List<PathologyModel> findBy(String fieldName, Object value);
	
	List<PathologyModel> findByPathology(Pathology pathology);

}
