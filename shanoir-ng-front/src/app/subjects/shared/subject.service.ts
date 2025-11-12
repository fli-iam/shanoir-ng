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
import { HttpClient } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';

import { Page, Pageable } from 'src/app/shared/components/table/pageable.model';

import { EntityService } from '../../shared/components/entity/entity.abstract.service';
import { IdName } from '../../shared/models/id-name.model';
import * as AppUtils from '../../utils/app.utils';

import { SubjectStudy } from './subject-study.model';
import { Subject } from './subject.model';
import { SubjectDTO, SubjectDTOService } from './subject.dto';
import { SubjectStudyDTO } from './subject-study.dto';


@Injectable()
export class SubjectService extends EntityService<Subject> {

    API_URL = AppUtils.BACKEND_API_SUBJECT_URL;

    constructor(protected http: HttpClient, protected subjectDTOService: SubjectDTOService) {
        super(http);
    }

    getEntityInstance() { return new Subject(); }

    getAllSubjectsNames(): Promise<IdName[]> {
        return firstValueFrom(this.http.get<IdName[]>(AppUtils.BACKEND_API_SUBJECT_NAMES_URL));
    }

    getSubjectsNames(subjectIds: Set<number>): Promise<IdName[]> {
        const formData: FormData = new FormData();
        formData.set('subjectIds', Array.from(subjectIds).join(","));
        return firstValueFrom(this.http.post<IdName[]>(AppUtils.BACKEND_API_SUBJECT_NAMES_URL, formData));
    }

    getClinicalSubjects(): Promise<Subject[]> {
        return firstValueFrom(this.http.get<Subject[]>(AppUtils.BACKEND_API_SUBJECT_URL + '?preclinical=false'));
    }

    getPreclinicalSubjects(): Promise<Subject[]> {
        return firstValueFrom(this.http.get<Subject[]>(AppUtils.BACKEND_API_SUBJECT_URL + '?clinical=false'));
    }

    getPage(pageable: Pageable, name: string):  Promise<Page<Subject>> {
        const params = { 'params': pageable.toParams() };
        params['params']['name'] = name;
        return firstValueFrom(this.http.get<Page<Subject>>(AppUtils.BACKEND_API_SUBJECT_FILTER_URL, params));
    }

    findSubjectByIdentifier(identifier: string): Promise<Subject> {
        return firstValueFrom(this.http.get<SubjectDTO>(AppUtils.BACKEND_API_SUBJECT_FIND_BY_IDENTIFIER + '/' + identifier))
            .then(dto => this.mapEntity(dto));
    }

    updateSubjectStudyValues(subjectStudy: SubjectStudy): Promise<void> {
        return firstValueFrom(this.http.put<void>(
                AppUtils.BACKEND_API_SUBJECT_STUDY_URL + '/' + subjectStudy.id,
                JSON.stringify(new SubjectStudyDTO(subjectStudy))
            ));
    }

    protected mapEntity = (dto: SubjectDTO, result?: Subject): Promise<Subject> => {
        if (result == undefined) result = this.getEntityInstance();
        return this.subjectDTOService.toEntity(dto, result);
    }

    protected mapEntityList = (dtos: SubjectDTO[], result?: Subject[]): Promise<Subject[]> => {
        if (result == undefined) result = [];
        if (dtos) return this.subjectDTOService.toEntityList(dtos, result);
    }

    public stringify(entity: Subject) {
        const dto = new SubjectDTO(entity);
        return JSON.stringify(dto, (key, value) => {
            return this.customReplacer(key, value, dto);
        });
    }
}
