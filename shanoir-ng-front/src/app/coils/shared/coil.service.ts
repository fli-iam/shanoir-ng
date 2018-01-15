import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs/Observable';

import * as AppUtils from '../../utils/app.utils';
import { Coil } from './coil.model';
import { HandleErrorService } from '../../shared/utils/handle-error.service';
//import { SubjectExamination } from '../shared/subject-examination.model';

@Injectable()
export class CoilService {
    constructor(private http: HttpClient, private handleErrorService: HandleErrorService) { }

    getCoils(): Promise<Coil[]> {
        return this.http.get<Coil[]>(AppUtils.BACKEND_API_COIL_URL)
            .toPromise()
            .then(response => response)
            .catch((error) => {
                console.error('Error while getting coils', error);
                return Promise.reject(error.message || error);
            });
    }

    delete(id: number): Promise<void> {
        return this.http.delete<void>(AppUtils.BACKEND_API_COIL_URL + '/' + id)
            .toPromise()
            .catch((error) => {
                console.error('Error delete coil', error);
                return Promise.reject(error.message || error);
            });
    }

    getCoil(id: number): Promise<Coil> {
        return this.http.get<Coil>(AppUtils.BACKEND_API_COIL_URL + '/' + id)
            .toPromise()
            .then(res => res)
            .catch((error) => {
                console.error('Error while getting coil', error);
                return Promise.reject(error.message || error);
            });
    }

    create(coil: Coil): Observable<Coil> {
        return this.http.post<Coil>(AppUtils.BACKEND_API_COIL_URL, JSON.stringify(coil))
            .map(response => response)
            .catch(this.handleErrorService.handleError);
    }

    update(id: number, coil: Coil): Observable<Coil> {
        return this.http.put<Coil>(AppUtils.BACKEND_API_COIL_URL + '/' + id, JSON.stringify(coil))
            .map(response => response)
            .catch(this.handleErrorService.handleError);
    }

   /* findExaminationsBySubjectId(subjectId: number): Promise<SubjectExamination[]> {
        return this.http.get<SubjectExamination[]>(AppUtils.BACKEND_API_EXAMINATION_ALL_BY_SUBJECT_URL + '/' + subjectId)
            .toPromise()
            .then(response => response)
            .catch((error) => {
                console.error('Error while getting examinations by subject id', error);
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
    }*/
}