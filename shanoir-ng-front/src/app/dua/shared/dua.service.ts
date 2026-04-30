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

import { StudyService } from 'src/app/studies/shared/study.service';

import * as AppUtils from '../../utils/app.utils';

import { DuaDocument } from './dua-document.model';


@Injectable()
export class DuaService {

    API_URL = AppUtils.BACKEND_API_STUDIES_MS_URL + '/dua';

    imagePreview: string | null = null;

    constructor(
            protected http: HttpClient,
            protected studyService: StudyService) {
    }

    create(entity: DuaDocument, email: string): Promise<string> {
        const arg: any = {duaDraft: entity, email: email};
        return firstValueFrom(this.http.post(this.API_URL, this.stringify(arg), {responseType: 'text'}));
    }

    update(entity: DuaDocument): Promise<void> {
        return firstValueFrom(this.http.put<any>(this.API_URL + '/' + entity.id, this.stringify(entity)));
    }
    
    get(id: string): Promise<DuaDocument> {
        return firstValueFrom(this.http.get<any>(this.API_URL + '/' + id))
            .then(entity => this.toRealObject(entity));
    }

    private stringify(entity: DuaDocument): string {
        return JSON.stringify(entity);
    }

    protected toRealObject(entity: DuaDocument): DuaDocument {
        const trueObject = Object.assign(new DuaDocument(), entity);
        return trueObject;
    }
}
