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

import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Pageable } from '../shared/components/table/pageable.model';
import { KeycloakService } from '../shared/keycloak/keycloak.service';
import * as AppUtils from '../utils/app.utils';
import { SolrRequest, SolrResultPage } from './solr.document.model';


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
        if (!solrReq.studyName && !solrReq.subjectName && !solrReq.examinationComment && !solrReq.datasetName
            && !solrReq.datasetStartDate && !solrReq.datasetEndDate && !solrReq.datasetType && !solrReq.datasetNature) {
                return this.http.get<SolrResultPage>(AppUtils.BACKEND_API_SOLR_URL, { 'params': pageable.toParams() })    
                .map((solrResultPage: SolrResultPage) => {
                    return solrResultPage;
                })
            .toPromise();
        } else {
            return this.http.post<SolrResultPage>(AppUtils.BACKEND_API_SOLR_URL, JSON.stringify(solrReq), { 'params': pageable.toParams() })    
                .map((solrResultPage: SolrResultPage) => {
                    return solrResultPage;
                })
            .toPromise();
        }
    }

}