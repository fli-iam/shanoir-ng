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
import { FacetPageable, FacetResultPage, SolrDocument, SolrRequest, SolrResultPage } from './solr.document.model';


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
        return this.http.post<SolrResultPage>(AppUtils.BACKEND_API_SOLR_URL, this.stringifySolrRequest(solrReq), { 'params': pageable.toParams() })    
        .toPromise();
    }

    public getFacets(): Promise<SolrResultPage> {
        let fakePageable: Pageable = new Pageable(1, 1, new Sort([new Order('DESC', 'id')]));
        let solrRequest: SolrRequest = new SolrRequest();
        solrRequest.facetPaging = new Map();
        solrRequest.facetPaging.set('datasetName', new FacetPageable(1, 10));
        return this.http.post<SolrResultPage>(AppUtils.BACKEND_API_SOLR_URL, this.stringifySolrRequest(solrRequest), { 'params': fakePageable.toParams() })    
        .toPromise();
    }

    public getFacet(facetName: string, pageable: FacetPageable, mainRequest: SolrRequest): Promise<FacetResultPage> {
        let fakePageable: Pageable = new Pageable(1, 0, new Sort([new Order('DESC', 'id')]));
        mainRequest.facetPaging = new Map();
        mainRequest.facetPaging.set(facetName, pageable);
        return this.http.post<SolrResultPage>(AppUtils.BACKEND_API_SOLR_URL, this.stringifySolrRequest(mainRequest), { 'params': fakePageable.toParams() })  
            .toPromise().then(solrResPage => solrResPage && solrResPage.facetResultPages ? solrResPage.facetResultPages[0] : null);
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

    private stringifySolrRequest(solrRequest: SolrRequest): string {
        return JSON.stringify(solrRequest, (key, value) => {
            // write a Map as a key value object
            if(value instanceof Map) {
                let res: any = {};
                value.forEach((v, k) => res[k] = v);
                return res;
            } else if (key.endsWith('Date') && value == 'invalid') {
                return null;
            } else {
                return value;
            }
        })
    }

}