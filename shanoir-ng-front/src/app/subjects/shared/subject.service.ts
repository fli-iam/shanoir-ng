import { Injectable } from '@angular/core';

import { EntityService } from '../../shared/components/entity/entity.abstract.service';
import { IdName } from '../../shared/models/id-name.model';
import * as AppUtils from '../../utils/app.utils';
import { SubjectStudy } from './subject-study.model';
import { Subject } from './subject.model';

@Injectable()
export class SubjectService extends EntityService<Subject> {

    API_URL = AppUtils.BACKEND_API_SUBJECT_URL;

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