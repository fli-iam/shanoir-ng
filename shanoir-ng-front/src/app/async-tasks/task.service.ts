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
import { HttpClient, HttpParams, HttpResponse} from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/Observable';

import { EntityService } from '../shared/components/entity/entity.abstract.service';
import * as AppUtils from '../utils/app.utils';
import { Task } from './task.model';

@Injectable()
export class TaskService extends EntityService<Task> {

    API_URL = AppUtils.BACKEND_API_TASKS_URL;

    constructor(protected http: HttpClient) {
        super(http)
    }

    getEntityInstance() { return new Task(); }

    getTasks(): Promise<Task[]> {
       return this.http.get<Task[]>(this.API_URL).toPromise();
    }

    getTaskOfTypes(types: String[]): Promise<Task[]> {
       const params = new HttpParams().set('types', types.join(','));
       return this.http.get<Task[]>(this.API_URL + "/types", {params}).toPromise();
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