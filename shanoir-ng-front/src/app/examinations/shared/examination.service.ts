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
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Injectable, OnDestroy } from '@angular/core';
import { Observable } from 'rxjs';

import { TaskState, TaskStatus } from 'src/app/async-tasks/task.model';
import { EntityService } from '../../shared/components/entity/entity.abstract.service';
import { Page, Pageable } from '../../shared/components/table/pageable.model';
import * as AppUtils from '../../utils/app.utils';
import { ServiceLocator } from '../../utils/locator.service';
import { ExaminationDTO, ExaminationDTOService } from './examination.dto';
import { Examination } from './examination.model';
import { SubjectExamination } from './subject-examination.model';


@Injectable()
export class ExaminationService extends EntityService<Examination> {

    API_URL = AppUtils.BACKEND_API_EXAMINATION_URL;

    constructor(protected http: HttpClient) {
        super(http)
    }
    protected examinationDtoService: ExaminationDTOService = ServiceLocator.injector.get(ExaminationDTOService);

    getEntityInstance() { return new Examination(); }

    findExaminationsBySubjectAndStudy(subjectId: number, studyId: number): Promise<SubjectExamination[]> {
        let url = AppUtils.BACKEND_API_EXAMINATION_URL
            + '/subject/' + subjectId
            + '/study/' + studyId;
        return this.http.get<SubjectExamination[]>(url)
            .toPromise();
    }

    findExaminationIdsByStudy(studyId: number): Promise<number[]> {
        let url = AppUtils.BACKEND_API_EXAMINATION_URL
            + '/study/' + studyId;
        return this.http.get<number[]>(url)
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

    downloadFile(fileName: string, examId: number, state?: TaskState): Observable<TaskState>  {
        const endpoint: string = this.API_URL + '/extra-data-download/' + examId + "/" + fileName + "/";
        return AppUtils.downloadWithStatusGET(endpoint, null, state);
    }

    private downloadIntoBrowser(response: HttpResponse<Blob>){
        AppUtils.browserDownloadFileFromResponse(response);
    }

    public stringify(entity: Examination) {
        let dto = new ExaminationDTO(entity);
        return JSON.stringify(dto, (key, value) => {
            return this.customReplacer(key, value, dto);
        });
    }
}
