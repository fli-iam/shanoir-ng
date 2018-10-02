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
                page.content = page.content.map(ds => Object.assign(ds, this.getEntityInstance(ds)));
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