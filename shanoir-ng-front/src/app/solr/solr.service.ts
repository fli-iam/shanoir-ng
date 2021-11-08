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

import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Order, Page, Pageable, Sort } from '../shared/components/table/pageable.model';
import { KeycloakService } from '../shared/keycloak/keycloak.service';
import * as AppUtils from '../utils/app.utils';
import { SolrDocument, SolrRequest, SolrResultPage } from './solr.document.model';


@Injectable()
export class SolrService {
    
    constructor(private http: HttpClient, private keycloakService: KeycloakService) {

    }

    public indexAll(): Promise<void> {
        if (this.keycloakService.isUserAdmin()) {
            return this.http.post<void>(AppUtils.BACKEND_API_SOLR_INDEX_URL, {}).toPromise();
        }
    }

    public search(solrReq: SolrRequest, pageable: Pageable): Promise<SolrResultPage> {
        let serializedSolrRequest: string = JSON.stringify(solrReq);
        if (serializedSolrRequest == '{}') {
            return this.http.get<SolrResultPage>(AppUtils.BACKEND_API_SOLR_URL, { 'params': pageable.toParams() })    
            .toPromise();
        } else {
            return this.http.post<SolrResultPage>(AppUtils.BACKEND_API_SOLR_URL, serializedSolrRequest, { 'params': pageable.toParams() })    
            .toPromise();
        }
    }

    public getFacets(): Promise<SolrResultPage> {
        let pageable: Pageable = new Pageable(1, 1, new Sort([new Order('DESC', 'id')]));
        return this.http.get<SolrResultPage>(AppUtils.BACKEND_API_SOLR_URL, { 'params': pageable.toParams() })    
            .toPromise();
    }

    public getByDatasetIds(datasetIds: number[], pageable: Pageable): Promise<Page<SolrDocument>> {
        return this.http.post<Page<SolrDocument>>(AppUtils.BACKEND_API_SOLR_URL + '/byIds', 
                JSON.stringify(datasetIds), 
                { 'params': pageable.toParams() })    
            .toPromise().then(page => {
                if (page) page.content.forEach(solrDoc => solrDoc.id = solrDoc.datasetId);
                return page;
            });
    }

}