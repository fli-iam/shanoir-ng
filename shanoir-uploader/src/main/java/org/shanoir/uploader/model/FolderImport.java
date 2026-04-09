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

package org.shanoir.uploader.model;

import org.shanoir.uploader.model.rest.Subject;

import java.util.List;

public class FolderImport {

    private List<ExaminationImport> examinationImports;

    private org.shanoir.uploader.model.rest.Study study;
    private org.shanoir.uploader.model.rest.StudyCard studyCard;

    private List<Subject> listOfSubjectsForStudy;

    public org.shanoir.uploader.model.rest.Study getStudy() {
        return study;
    }

    public void setStudy(org.shanoir.uploader.model.rest.Study study) {
        this.study = study;
    }

    public org.shanoir.uploader.model.rest.StudyCard getStudyCard() {
        return studyCard;
    }

    public void setStudyCard(org.shanoir.uploader.model.rest.StudyCard studyCard) {
        this.studyCard = studyCard;
    }

    public List<ExaminationImport> getExaminationImports() {
        return examinationImports;
    }

    public void setExaminationImports(List<ExaminationImport> examinationImports) {
        this.examinationImports = examinationImports;
    }

    public List<Subject> getListOfSubjectsForStudy() {
        return listOfSubjectsForStudy;
    }

    public void setListOfSubjectsForStudy(List<Subject> listOfSubjectsForStudy) {
        this.listOfSubjectsForStudy = listOfSubjectsForStudy;
    }
}
