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
import { HttpClient, HttpResponse } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';

import { EntityService } from '../shared/components/entity/entity.abstract.service';
import * as AppUtils from '../utils/app.utils';

import { Task } from './task.model';

@Injectable()
export class TaskService extends EntityService<Task> {

    API_URL = AppUtils.BACKEND_API_TASKS_URL;


    constructor(protected http: HttpClient) {
        super(http);
    }

    getEntityInstance() { return new Task(); }

    getTasks(): Promise<Task[]> {
        return firstValueFrom(this.http.get<Task[]>(this.API_URL))
            .then(this.mapEntityList);
    }

    public toRealObject(entity: any): Task {
        const trueObject = Object.assign(new Task(), entity);
        trueObject.completeId = entity.idAsString;
        Object.keys(entity).forEach(key => {
            if (key != 'idAsString') {
                const value = entity[key];
                if (['creationDate', 'lastUpdate'].includes(key) && value) {
                    trueObject[key] = new Date(value);
                }
            }
        });
        return trueObject;
    }

    public downloadStats(item: Task) {
        const endpoint = AppUtils.BACKEND_API_DATASET_MS_URL + item.route;
        firstValueFrom(this.http.get(endpoint, { observe: 'response', responseType: 'blob' }))
            .then((response: HttpResponse<Blob>) => {
                if (response.status == 200 || response.status == 204) {
                    AppUtils.browserDownloadFileFromResponse(response);
                } else {
                    this.consoleService.log('error', 'Statistics file not found or deleted (after 6 hours).');
                }
            });
    }
}
