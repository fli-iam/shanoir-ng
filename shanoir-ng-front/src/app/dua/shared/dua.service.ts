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

import { EntityService } from '../../shared/components/entity/entity.abstract.service';
import { DuaDocument } from './dua-document.model';
import { HttpClient } from '@angular/common/http';


@Injectable()
export class DuaService {

    API_URL = '/dua';

    constructor(
        protected http: HttpClient) {
    }

    create(entity: DuaDocument, email: string): Promise<string> {
        let arg: any = {dua: entity, email: email};
        return this.http.post<any>(this.API_URL, this.stringify(arg))
            .toPromise();
    }

    update(entity: DuaDocument): Promise<void> {
        return this.http.put<any>(this.API_URL + '/' + entity.id, this.stringify(entity))
            .toPromise();
    }
    
    get(id: string): Promise<DuaDocument> {
        return this.http.get<any>(this.API_URL + '/' + id)
            .toPromise()
            .then(entity => this.toRealObject(entity));
    }

    private stringify(entity: DuaDocument): string {
        return JSON.stringify(entity);
    }

    protected toRealObject(entity: DuaDocument): DuaDocument {
        let trueObject = Object.assign(new DuaDocument(), entity);
        return trueObject;
    }
}
