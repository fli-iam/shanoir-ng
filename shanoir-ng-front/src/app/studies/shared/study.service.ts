import { Injectable } from '@angular/core';
import { EntityService } from '../../shared/components/entity/entity.abstract.service';
import { IdName } from '../../shared/models/id-name.model';
import { SubjectWithSubjectStudy } from '../../subjects/shared/subject.with.subject-study.model';
import * as AppUtils from '../../utils/app.utils';
import { Study } from './study.model';

@Injectable()
export class StudyService extends EntityService<Study> {

    API_URL = AppUtils.BACKEND_API_STUDY_URL;

    getEntityInstance() { return new Study(); }
    
    findStudiesByUserId(): Promise<Study[]> {
        return this.http.get<Study[]>(AppUtils.BACKEND_API_STUDY_URL)
        .map(entities => entities.map((entity) => Object.assign(new Study(), entity)))
        .toPromise();
    }
    
    findStudiesForImport(): Promise<Study[]> {
        return this.http.get<Study[]>(AppUtils.BACKEND_API_STUDY_FOR_IMPORT_URL)
            .toPromise();
    }

    getStudiesNames(): Promise<IdName[]> {
        return this.http.get<IdName[]>(AppUtils.BACKEND_API_STUDY_ALL_NAMES_URL)
            .toPromise();
    }
    
    findSubjectsByStudyId(studyId: number): Promise<SubjectWithSubjectStudy[]> {
        return this.http.get<SubjectWithSubjectStudy[]>(AppUtils.BACKEND_API_SUBJECT_URL + '/' + studyId + '/allSubjects')
            .toPromise();
    }
}