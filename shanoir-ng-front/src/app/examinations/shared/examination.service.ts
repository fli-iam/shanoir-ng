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
import { Observable } from 'rxjs/Observable';
import { EntityService } from '../../shared/components/entity/entity.abstract.service';
import { Page, Pageable } from '../../shared/components/table/pageable.model';
import * as AppUtils from '../../utils/app.utils';
import { Examination } from './examination.model';
import { SubjectExamination } from './subject-examination.model';


@Injectable()
export class ExaminationService extends EntityService<Examination> {

    API_URL = AppUtils.BACKEND_API_EXAMINATION_URL;

    getEntityInstance() { return new Examination(); }

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