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
import { HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs/Observable';
import { EntityService } from '../../shared/components/entity/entity.abstract.service';
import { Page, Pageable } from '../../shared/components/table/pageable.model';
import * as AppUtils from '../../utils/app.utils';
import { Examination } from './examination.model';
import { SubjectExamination } from './subject-examination.model';
import { HttpClient } from '@angular/common/http';
import { ExaminationDTO, ExaminationDTOService } from './examination.dto';
import { ServiceLocator } from '../../utils/locator.service';


@Injectable()
export class ExaminationService extends EntityService<Examination> {

    API_URL = AppUtils.BACKEND_API_EXAMINATION_URL;

    constructor(protected http: HttpClient) {
        super(http)
    }
    protected examinationDtoService: ExaminationDTOService = ServiceLocator.injector.get(ExaminationDTOService);

    getEntityInstance() { return new Examination(); }

    findExaminationsBySubjectAndStudy(subjectId: number, studyId: number): Promise<SubjectExamination[]> {
        return this.http.get<SubjectExamination[]>(AppUtils.BACKEND_API_EXAMINATION_URL + '/subject/' + subjectId + '/study/' + studyId)
            .toPromise();
    }

    getPage(pageable: Pageable, preclinical: boolean = false): Promise<Page<Examination>> {
        return this.http.get<Page<Examination>>(
            (!preclinical) ? AppUtils.BACKEND_API_EXAMINATION_URL : (AppUtils.BACKEND_API_EXAMINATION_PRECLINICAL_URL+'/1'), 
            { 'params': pageable.toParams() }
        )
        .toPromise()
        .then(this.mapPage);
    }

    protected mapEntity = (entity: ExaminationDTO, result?: Examination): Promise<Examination> => {
        return this.examinationDtoService.toEntity(entity);
    }

    protected mapEntityList = (entities: any[], result?: Examination[]): Promise<Examination[]> => {
        if (!entities) entities = [];
        return this.examinationDtoService.toEntityList(entities);
    }
        
    postFile(fileToUpload: File, examId: number): Promise<any> {
        const endpoint = this.API_URL + '/extra-data-upload/' + examId;
        const formData: FormData = new FormData();
        formData.append('file', fileToUpload, fileToUpload.name);
        return this.http.post<any>(endpoint, formData).toPromise();
    }

    downloadFile(fileName: string, examId: number): void {
        const endpoint = this.API_URL + '/extra-data-download/' + examId + "/" + fileName + "/";
        this.http.get(endpoint, { observe: 'response', responseType: 'blob' }).subscribe(response => {
            if (response.status == 200) {
                this.downloadIntoBrowser(response);
            }
        });
    }

    private getFilename(response: HttpResponse<any>): string {
        const prefix = 'attachment;filename=';
        let contentDispHeader: string = response.headers.get('Content-Disposition');
        return contentDispHeader.slice(contentDispHeader.indexOf(prefix) + prefix.length, contentDispHeader.length);
    }

    private downloadIntoBrowser(response: HttpResponse<Blob>){
        AppUtils.browserDownloadFile(response.body, this.getFilename(response));
    }
}