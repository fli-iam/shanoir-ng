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

import { HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/Observable';

import { EntityService } from '../../shared/components/entity/entity.abstract.service';
import { Page, Pageable } from '../../shared/components/table/pageable.model';
import * as AppUtils from '../../utils/app.utils';
import { Dataset } from './dataset.model';

@Injectable()
export class DatasetService extends EntityService<Dataset> {

    API_URL = AppUtils.BACKEND_API_DATASET_URL;

    getEntityInstance(entity: Dataset) { 
        return AppUtils.getEntityInstance(entity);
    }

    getPage(pageable: Pageable): Promise<any> {
        return this.http.get<any>(AppUtils.BACKEND_API_DATASET_URL, { 'params': pageable.toParams() })
            .map((page: Page<Dataset>) => {
                if (page && page.content) {
                    page.content = page.content.map(ds => Object.assign(ds, this.getEntityInstance(ds)));
                }
                return page;
            })
            .toPromise();
    }

    download(dataset: Dataset, format: string): void {
        if (!dataset.id) throw Error('Cannot download a dataset without an id');
        this.downloadToBlob(dataset.id, format).subscribe(
            response => {
                this.downloadIntoBrowser(response);
            }
        );
    }

    downloadToBlob(id: number, format: string): Observable<HttpResponse<Blob>> {
        if (!id) throw Error('Cannot download a dataset without an id');
        return this.http.get(
            AppUtils.BACKEND_API_DATASET_URL + '/download/' + id + '?format=' + format, 
            { observe: 'response', responseType: 'blob' }
        ).map(response => response);
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