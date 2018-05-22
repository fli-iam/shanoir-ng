package org.shanoir.ng.preclinical.references;

import java.util.List;
import java.util.Optional;

import org.shanoir.ng.preclinical.references.Reference;

/**
 * Custom repository for refs.
 * 
 * @author sloury
 *
 */
public interface RefsRepositoryCustom {

	
	List<Reference> findByCategory(String category);
	
	List<Reference> findByCategoryAndType(String category, String type);
	
	Optional<Reference> findByCategoryTypeAndValue(String category, String reftype, String value);
	
	Optional<Reference> findByTypeAndValue(String reftype, String value);
	
	List<String> findCategories();
	
	List<String> findTypesByCategory(String category);

}
