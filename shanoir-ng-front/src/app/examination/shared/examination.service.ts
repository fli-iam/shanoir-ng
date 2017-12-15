import { Injectable } from '@angular/core';
import { Response, Http } from '@angular/http';
import { Observable } from 'rxjs/Observable';

import { Examination } from './examination.model';
import * as AppUtils from '../../utils/app.utils';
import { HandleErrorService } from '../../shared/utils/handle-error.service';

@Injectable()
export class ExaminationService {
    constructor(private http: Http, private handleErrorService: HandleErrorService) { }

    findExaminationsBySubjectId(subjectId: number): Promise<Examination[]> {
        return this.http.get(AppUtils.BACKEND_API_EXAMINATION_ALL_BY_SUBJECT_URL + '/' + subjectId)
            .toPromise()
            .then(response => response.json() as Examination[])
            .catch((error) => {
                console.error('Error while getting examinations by subject id', error);
                return Promise.reject(error.message || error);
        });
    }
}