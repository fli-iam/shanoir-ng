import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs/Observable';

import { Study } from './study.model';
import { SubjectWithSubjectStudy } from '../../subjects/shared/subject.with.subject-study.model';
import * as AppUtils from '../../utils/app.utils';
import { IdNameObject } from '../../shared/models/id-name-object.model';
import { EquipmentDicom } from "../../import/dicom-data.model";
import { MsgBoxService } from '../../shared/msg-box/msg-box.service';
import { GuiError } from '../../shared/models/error.model';

@Injectable()
export class StudyService {
    constructor(private http: HttpClient, private msgBoxService: MsgBoxService) { }

    findStudiesByUserId(): Promise<Study[]> {
        return this.http.get<Study[]>(AppUtils.BACKEND_API_STUDY_URL)
            .toPromise();
    }

    findStudiesWithStudyCardsByUserAndEquipment(equipment: EquipmentDicom): Promise<Study[]> {
        return this.http.post<Study[]>(AppUtils.BACKEND_API_STUDY_WITH_CARDS_BY_USER_EQUIPMENT_URL, JSON.stringify(equipment))
            .toPromise();
    }

    create(study: Study): Observable<Study> {
        return this.http.post<Study>(AppUtils.BACKEND_API_STUDY_URL, JSON.stringify(study))
            .map(res => res);
    }

    delete(id: number): Promise<void> {
        return this.http.delete<void>(AppUtils.BACKEND_API_STUDY_URL + '/' + id)
            .toPromise();
    }

    getStudies(): Promise<Study[]> {
        return this.http.get<Study[]>(AppUtils.BACKEND_API_STUDY_URL)
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
    
    getStudy(id: number, withData: boolean): Promise<Study> {
        return this.http.get<Study>(AppUtils.BACKEND_API_STUDY_URL + '/' + id + '?withdata=' + withData)
            .toPromise();
    }

    update(id: number, study: Study): Observable<Study> {
        return this.http.put<Study>(AppUtils.BACKEND_API_STUDY_URL + '/' + id, JSON.stringify(study))
            .map(response => response);
    }
}