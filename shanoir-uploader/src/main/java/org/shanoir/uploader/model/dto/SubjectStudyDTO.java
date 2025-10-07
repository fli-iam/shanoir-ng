package org.shanoir.uploader.model.dto;

public class SubjectStudyDTO {

    private Long id;

    private Long studyId;

    private boolean physicallyInvolved;

    private String subjectStudyIdentifier;

    private String subjectType;

    public SubjectStudyDTO(Long id, Long studyId, boolean physicallyInvolved, String subjectStudyIdentifier,
            String subjectType) {
        super();
        this.id = id;
        this.studyId = studyId;
        this.physicallyInvolved = physicallyInvolved;
        this.subjectStudyIdentifier = subjectStudyIdentifier;
        this.subjectType = subjectType;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public boolean isPhysicallyInvolved() {
        return physicallyInvolved;
    }

    public void setPhysicallyInvolved(boolean physicallyInvolved) {
        this.physicallyInvolved = physicallyInvolved;
    }

    public String getSubjectStudyIdentifier() {
        return subjectStudyIdentifier;
    }

    public void setSubjectStudyIdentifier(String subjectStudyIdentifier) {
        this.subjectStudyIdentifier = subjectStudyIdentifier;
    }

    public String getSubjectType() {
        return subjectType;
    }

    public void setSubjectType(String subjectType) {
        this.subjectType = subjectType;
    }

    public Long getStudyId() {
        return studyId;
    }

    public void setStudyId(Long studyId) {
        this.studyId = studyId;
    }

}
