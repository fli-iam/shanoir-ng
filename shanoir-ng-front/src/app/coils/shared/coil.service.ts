import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs/Observable';

import * as AppUtils from '../../utils/app.utils';
import { Coil } from './coil.model';
import { FilterablePageable, Page } from '../../shared/components/table/pageable.model';
import { BrowserPaging } from '../../shared/components/table/browser-paging.model';

@Injectable()
export class CoilService {
    constructor(private http: HttpClient) { }

    getCoils(): Promise<Coil[]> {
        return this.http.get<Coil[]>(AppUtils.BACKEND_API_COIL_URL)
        .map(coils => coils.map((coil) => Object.assign(new Coil(), coil)))
        .toPromise();
    }

    delete(id: number): Promise<void> {
        return this.http.delete<void>(AppUtils.BACKEND_API_COIL_URL + '/' + id).toPromise();
    }

    getCoil(id: number): Promise<Coil> {
        return this.http.get<Coil>(AppUtils.BACKEND_API_COIL_URL + '/' + id)
        .map(coil => Object.assign(new Coil(), coil))
        .toPromise();
    }

    create(coil: Coil): Promise<Coil> {
        return this.http.post<Coil>(AppUtils.BACKEND_API_COIL_URL, JSON.stringify(coil)).toPromise();
    }

    update(id: number, coil: Coil): Promise<void> {
        return this.http.put<void>(AppUtils.BACKEND_API_COIL_URL + '/' + id, JSON.stringify(coil)).toPromise();
    }

    private coilsPromise: Promise<void> = this.loadCoils();
    private browserPaging: BrowserPaging<Coil>;
    private coils: Coil[];

    getPage(pageable: FilterablePageable): Promise<Page<Coil>> {
        return new Promise((resolve) => {
            this.coilsPromise.then(() => {
                this.browserPaging.setItems(this.coils);
                resolve(this.browserPaging.getPage(pageable));
            });
        });
    }

    private loadCoils(): Promise<void> {
        return this.getCoils().then(coils => {
            if (coils) {
                this.coils = coils;
                this.browserPaging = new BrowserPaging(this.coils, this.columnDefs);
            }
        })
    }
}