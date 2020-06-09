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

import { Entity } from "../../shared/components/entity/entity.abstract";
import { IdName } from "../../shared/models/id-name.model";
import { SubjectWithSubjectStudy } from "../../subjects/shared/subject.with.subject-study.model";
import { InstrumentBasedAssessment } from "../instrument-assessment/instrument.model"

export class Examination extends Entity {
    id: number;
    examinationDate: Date;
    examinationExecutive: IdName;
    subjectStudy: SubjectWithSubjectStudy;
    subject: IdName;
    subjectId: number;
    study: IdName;
    studyId: number;
    center: IdName;
    centerId: number;
    comment: string;
    note: string;
    subjectWeight: number;
    instrumentBasedAssessmentList: InstrumentBasedAssessment[];
    extraDataFilePathList: string[] = [];
    preclinical: boolean;
    hasStudyCenterData: boolean = false;  
}