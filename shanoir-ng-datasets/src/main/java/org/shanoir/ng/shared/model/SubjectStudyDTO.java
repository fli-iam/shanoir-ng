package org.shanoir.ng.shared.model;

import org.shanoir.ng.shared.subjectstudy.SubjectType;
import org.shanoir.ng.tag.model.Tag;

import java.util.List;

public class SubjectStudyDTO {

    private Long id;
    
    private List<Tag> tags;

    private SubjectType subjectType;

    /**
     * @return the tags
     */
    public List<Tag> getTags() {
        return tags;
    }

    /**
     * @param tags the tags to set
     */
    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }

    /**
     * @return the id
     */
    public Long getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(Long id) {
        this.id = id;
    }

    public SubjectType getSubjectType() {
        return subjectType;
    }

    public void setSubjectType(SubjectType subjectType) {
        this.subjectType = subjectType;
    }
}
