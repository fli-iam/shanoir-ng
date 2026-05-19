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

import {Injectable, OnDestroy} from "@angular/core";
import { HttpClient } from "@angular/common/http";

import {EntityService} from "../../shared/components/entity/entity.abstract.service";
import * as AppUtils from "../../utils/app.utils";
import {Page, Pageable} from "../../shared/components/table/pageable.model";

import {ShanoirEvent} from "./shanoir-event.model";


@Injectable()
export class ShanoirEventService extends EntityService<ShanoirEvent> implements OnDestroy {

    API_URL = AppUtils.BACKEND_API_USER_EVENTS;

    constructor(protected http: HttpClient) {
        super(http);
    }

    getPage(pageable : Pageable, studyId: number, searchStr : string, searchField : string): Promise<Page<ShanoirEvent>> {
        const params = { 'params': pageable.toParams() };
        params['params']['searchStr'] = searchStr;
        params['params']['searchField'] = searchField;
        return this.http.get<Page<ShanoirEvent>>(this.API_URL + '/' + studyId, params)
            .toPromise()
            .then(this.mapPage);
    }

    getEntityInstance(): ShanoirEvent {
        return new ShanoirEvent();
    }
}
