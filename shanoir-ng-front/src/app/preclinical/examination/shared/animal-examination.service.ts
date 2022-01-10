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
import { HttpResponse, HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs/Observable';

import { Examination } from '../../../examinations/shared/examination.model';
import * as AppUtils from '../../../utils/app.utils';
import { EntityService } from '../../../shared/components/entity/entity.abstract.service';
import { Page, Pageable } from '../../../shared/components/table/pageable.model';
import { ExaminationDTO, ExaminationDTOService } from '../../../examinations/shared/examination.dto';

@Injectable()
export class AnimalExaminationService extends EntityService<Examination>{
    API_URL = AppUtils.BACKEND_API_EXAMINATION_URL;

    constructor(protected http: HttpClient) {
        super(http)
    }
    
    getEntityInstance() { return new Examination(); }

    getPage(pageable: Pageable): Promise<Page<Examination>> {
        return this.http.get<Page<Examination>>(
            AppUtils.BACKEND_API_EXAMINATION_PRECLINICAL_URL+'/1', 
            { 'params': pageable.toParams() }
        ).toPromise();
    }

    getBrukerArchive(examinationId): Promise<HttpResponse<Blob>> {
        return this.http.get(
            AppUtils.BACKEND_API_EXAMINATION_PRECLINICAL_URL+'/examinationId/' + examinationId  + '/export',
            { observe: 'response', responseType: 'blob' }
        ).toPromise();
    }
    
    postFile(fileToUpload: File, examId: number): Observable<any> {
        const endpoint = this.API_URL + '/extra-data-upload/' + examId;
        const formData: FormData = new FormData();
        formData.append('file', fileToUpload, fileToUpload.name);
        return this.http.post<any>(endpoint, formData);
    }

    public stringify(entity: Examination) {
        let dto = new ExaminationDTO(entity);
        return JSON.stringify(dto, (key, value) => {
            return this.customReplacer(key, value, dto);
        });
    }
}