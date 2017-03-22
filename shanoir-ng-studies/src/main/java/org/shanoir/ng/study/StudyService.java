package org.shanoir.ng.study;

import java.util.List;


public interface StudyService {
	
	/**
     * Get all the studies
     * @return a list of studies
     */
    List<Study> findAll();
    
    /**
     * add new study
     * @param study
     * @return
     */
    Study createStudy(Study study);
    
    /**
     *  Update a study
     * @param study
     * @return
     */
    Study update(Study study);

}
