import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/Observable';

import { Page, Pageable } from '../../shared/components/table/pageable.model';
import * as AppUtils from '../../utils/app.utils';
import { Examination } from './examination.model';
import { SubjectExamination } from './subject-examination.model';

@Injectable()
export class ExaminationService {
    constructor(private http: HttpClient) { }

    countExaminations(): Promise<number> {
        return this.http.get<number>(AppUtils.BACKEND_API_EXAMINATION_COUNT_URL)
            .toPromise();
    }

    create(examination: Examination): Promise<Examination> {
        return this.http.post<Examination>(AppUtils.BACKEND_API_EXAMINATION_URL, JSON.stringify(examination))
            .map((entity) => Object.assign(new Examination(), entity))
            .toPromise();
    }

    delete(id: number): Promise<void> {
        return this.http.delete<void>(AppUtils.BACKEND_API_EXAMINATION_URL + '/' + id)
            .toPromise();
    }

    findExaminationsBySubjectAndStudy(subjectId: number, studyId: number): Promise<SubjectExamination[]> {
        return this.http.get<SubjectExamination[]>(AppUtils.BACKEND_API_EXAMINATION_URL + '/subject/' + subjectId + '/study/' + studyId)
            .toPromise();
    }

    getExamination(id: number): Promise<Examination> {
        return this.http.get<Examination>(AppUtils.BACKEND_API_EXAMINATION_URL + '/' + id)
            .map((entity) => Object.assign(new Examination(), entity))
            .toPromise();
    }

    getPage(pageable: Pageable): Promise<Page<Examination>> {
        return this.http.get<Page<Examination>>(
            AppUtils.BACKEND_API_EXAMINATION_URL, 
            { 'params': pageable.toParams() }
        ).toPromise();
    }

    postFile(fileToUpload: File): Observable<any> {
        const endpoint = 'your-destination-url';
        const formData: FormData = new FormData();
        formData.append('fileKey', fileToUpload, fileToUpload.name);
        return this.http
            .post(endpoint, formData)
            .map(response => response);
    }
    
    update(id: number, examination: Examination): Promise<void> {
        return this.http.put<void>(AppUtils.BACKEND_API_EXAMINATION_URL + '/' + id, JSON.stringify(examination))
            .toPromise();
    }

}