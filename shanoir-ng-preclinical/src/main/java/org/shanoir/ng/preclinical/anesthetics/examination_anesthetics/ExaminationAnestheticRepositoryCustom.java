package org.shanoir.ng.preclinical.anesthetics.examination_anesthetics;

import java.util.List;



/**
 * Custom repository for examination anesthetics
 * 
 * @author sloury
 *
 */
public interface ExaminationAnestheticRepositoryCustom {
	
	List<ExaminationAnesthetic> findBy(String fieldName, Object value);
		

}
