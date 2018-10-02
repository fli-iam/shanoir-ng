import { Injectable } from '@angular/core';

import { EquipmentDicom } from '../../import/dicom-data.model';
import { EntityService } from '../../shared/components/entity/entity.abstract.service';
import { IdNameObject } from '../../shared/models/id-name-object.model';
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
    
    findStudiesWithStudyCardsByUserAndEquipment(equipment: EquipmentDicom): Promise<Study[]> {
        return this.http.post<Study[]>(AppUtils.BACKEND_API_STUDY_WITH_CARDS_BY_USER_EQUIPMENT_URL, JSON.stringify(equipment))
            .map(entities => entities.map((entity) => Object.assign(new Study(), entity)))
            .toPromise();
    }

    getStudiesNames(): Promise<IdNameObject[]> {
        return this.http.get<IdNameObject[]>(AppUtils.BACKEND_API_STUDY_ALL_NAMES_URL)
            .toPromise();
    }
    
    findSubjectsByStudyId(studyId: number): Promise<SubjectWithSubjectStudy[]> {
        return this.http.get<SubjectWithSubjectStudy[]>(AppUtils.BACKEND_API_SUBJECT_URL + '/' + studyId + '/allSubjects')
            .toPromise();
    }
}