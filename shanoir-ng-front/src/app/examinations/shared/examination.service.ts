import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/Observable';

import { Page, Pageable } from '../../shared/components/table/pageable.model';
import * as AppUtils from '../../utils/app.utils';
import { Examination } from './examination.model';
import { SubjectExamination } from './subject-examination.model';
import { EntityService } from '../../shared/components/entity/entity.abstract.service';

@Injectable()
export class ExaminationService extends EntityService<Examination> {

    API_URL = AppUtils.BACKEND_API_EXAMINATION_URL;

    getEntityInstance() { return new Examination(); }

    countExaminations(): Promise<number> {
        return this.http.get<number>(AppUtils.BACKEND_API_EXAMINATION_COUNT_URL)
            .toPromise();
    }

    findExaminationsBySubjectAndStudy(subjectId: number, studyId: number): Promise<SubjectExamination[]> {
        return this.http.get<SubjectExamination[]>(AppUtils.BACKEND_API_EXAMINATION_URL + '/subject/' + subjectId + '/study/' + studyId)
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
}