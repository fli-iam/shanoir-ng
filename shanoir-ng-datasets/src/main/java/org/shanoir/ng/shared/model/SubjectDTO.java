/**
 * Shanoir NG - Import, manage and share neuroimaging data
 * Copyright (C) 2009-2019 Inria - https://www.inria.fr/
 * Contact us on https://project.inria.fr/shanoir/
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/gpl-3.0.html
 */
package org.shanoir.ng.shared.model;

import java.util.List;

/**
 * @author yyao
 *
 */
public class SubjectDTO {

    private List<SubjectStudyDTO> subjectStudyList;
    
    protected Long id;
    
    protected String name;
    
    public SubjectDTO() { };
    
    /**
     * @param id
     * @param name
     */
    public SubjectDTO (Long id, String name) {
        this.setId(id);
        this.setName(name);
    }

    /**
     * @return the subjectStudyList
     */
    public List<SubjectStudyDTO> getSubjectStudyList() {
        return subjectStudyList;
    }

    /**
     * @param subjectStudyList the subjectStudyList to set
     */
    public void setSubjectStudyList(List<SubjectStudyDTO> subjectStudyList) {
        this.subjectStudyList = subjectStudyList;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
