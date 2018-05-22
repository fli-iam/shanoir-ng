package org.shanoir.ng.preclinical.pathologies.subject_pathologies;

import java.util.List;

import org.shanoir.ng.preclinical.pathologies.Pathology;
import org.shanoir.ng.preclinical.pathologies.pathology_models.PathologyModel;
import org.shanoir.ng.preclinical.references.Reference;



/**
 * Custom repository for subject pathologies
 * 
 * @author sloury
 *
 */
public interface SubjectPathologyRepositoryCustom {
	
	List<SubjectPathology> findBy(String fieldName, Object value);
	
	//List<SubjectPathology> findBySubject(Subject subject);
	
	List<SubjectPathology> findAllByPathology(Pathology pathology);
	
	List<SubjectPathology> findAllByPathologyModel(PathologyModel model);
	
	List<SubjectPathology> findAllByLocation(Reference location);

}
