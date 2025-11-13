package org.shanoir.ng.shared.message;

import org.shanoir.ng.shared.dicom.InstitutionDicom;

public class CreateCenterForStudyMessage {

    private Long studyId;

    private InstitutionDicom institutionDicom;

    public CreateCenterForStudyMessage() {}

    public CreateCenterForStudyMessage(Long studyId, InstitutionDicom institutionDicom) {
        this.studyId = studyId;
        this.institutionDicom = institutionDicom;
    }

    public Long getStudyId() {
        return studyId;
    }

    public void setStudyId(Long studyId) {
        this.studyId = studyId;
    }

    public InstitutionDicom getInstitutionDicom() {
        return institutionDicom;
    }

    public void setInstitutionDicom(InstitutionDicom institutionDicom) {
        this.institutionDicom = institutionDicom;
    }

}
