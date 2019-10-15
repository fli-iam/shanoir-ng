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

import { Injectable } from '@angular/core';

import { EntityService } from '../../shared/components/entity/entity.abstract.service';
import { IdName } from '../../shared/models/id-name.model';
import * as AppUtils from '../../utils/app.utils';
import { SubjectStudy } from './subject-study.model';
import { Subject } from './subject.model';
import { HttpClient } from '@angular/common/http';

@Injectable()
export class SubjectService extends EntityService<Subject> {

    API_URL = AppUtils.BACKEND_API_SUBJECT_URL;

    constructor(protected http: HttpClient) {
        super(http)
    }

    getEntityInstance() { return new Subject(); }

    getSubjectsNames(): Promise<IdName[]> {
        return this.http.get<IdName[]>(AppUtils.BACKEND_API_SUBJECT_NAMES_URL)
        .toPromise();
    }

    getCentersNames(): Promise<Subject[]> {
        return this.http.get<Subject[]>(AppUtils.BACKEND_API_CENTER_NAMES_URL)
            .toPromise();
    }

    getCentersNamesForExamination(): Promise<IdName[]> {
        return this.http.get<IdName[]>(AppUtils.BACKEND_API_CENTER_NAMES_URL)
            .toPromise();
    }

    findSubjectByIdentifier(identifier: string): Promise<Subject> {
        return this.http.get<Subject>(AppUtils.BACKEND_API_SUBJECT_FIND_BY_IDENTIFIER + '/' + identifier)
            .toPromise();
    }

    updateSubjectStudyValues(subjectStudy: SubjectStudy): Promise<SubjectStudy> {
        return this.http.put<SubjectStudy>(AppUtils.BACKEND_API_SUBJECT_STUDY_URL + '/' + subjectStudy.id, JSON.stringify(subjectStudy))
            .toPromise();
    }
}