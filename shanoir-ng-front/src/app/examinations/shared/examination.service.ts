import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs/Observable';

import * as AppUtils from '../../utils/app.utils';
import { Examination } from './examination.model';
import { HandleErrorService } from '../../shared/utils/handle-error.service';
import { SubjectExamination } from '../shared/subject-examination.model';
import { Pageable } from '../../shared/components/table/pageable.model';

@Injectable()
export class ExaminationService {
    constructor(private http: HttpClient, private handleErrorService: HandleErrorService) { }

    countExaminations(): Promise<number> {
        return this.http.get<number>(AppUtils.BACKEND_API_EXAMINATION_COUNT_URL)
            .toPromise()
            .then(response => response)
            .catch((error) => {
                console.error('Error while counting examinations', error);
                return Promise.reject(error.message || error);
            });
    }

    create(examination: Examination): Observable<Examination> {
        return this.http.post<Examination>(AppUtils.BACKEND_API_EXAMINATION_URL, JSON.stringify(examination))
            .map(response => response)
            .catch(this.handleErrorService.handleError);
    }

    delete(id: number): Promise<void> {
        return this.http.delete<void>(AppUtils.BACKEND_API_EXAMINATION_URL + '/' + id)
            .toPromise()
            .catch((error) => {
                console.error('Error delete examination', error);
                return Promise.reject(error.message || error);
            });
    }

    findExaminationsBySubjectAndStudy(subjectId: number, studyId: number): Promise<SubjectExamination[]> {
        return this.http.get<SubjectExamination[]>(AppUtils.BACKEND_API_EXAMINATION_ALL_BY_SUBJECT_URL + '/' + subjectId + '/' + studyId)
            .toPromise()
            .then(response => response)
            .catch((error) => {
                console.error('Error while getting examinations by subject id', error);
                return Promise.reject(error.message || error);
            });
    }

    getExamination(id: number): Promise<Examination> {
        return this.http.get<Examination>(AppUtils.BACKEND_API_EXAMINATION_URL + '/' + id)
            .toPromise()
            .then(res => res)
            .catch((error) => {
                console.error('Error while getting examination', error);
                return Promise.reject(error.message || error);
            });
    }

    getExaminations(pageable: Pageable): Promise<Examination[]> {
        return this.http.get<Examination[]>(AppUtils.BACKEND_API_EXAMINATION_URL + AppUtils.getPageableQuery(pageable))
            .toPromise()
            .then(response => response)
            .catch((error) => {
                console.error('Error while getting examinations', error);
                return Promise.reject(error.message || error);
            });
    }

    postFile(fileToUpload: File): Observable<boolean> {
        const endpoint = 'your-destination-url';
        const formData: FormData = new FormData();
        formData.append('fileKey', fileToUpload, fileToUpload.name);
        return this.http
            .post(endpoint, formData)
            .map(response => response)
            .catch(this.handleErrorService.handleError);
    }
    
    update(id: number, examination: Examination): Observable<Examination> {
        return this.http.put<Examination>(AppUtils.BACKEND_API_EXAMINATION_URL + '/' + id, JSON.stringify(examination))
            .map(response => response)
            .catch(this.handleErrorService.handleError);
    }

}