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
import { SolrResultPage, ShanoirSolrFacet } from './solr.document.model';


@Injectable()
export class SolrService {
    
    constructor(private http: HttpClient, private keycloakService: KeycloakService) {

    }

    public indexAll(): Promise<void> {
        if (this.keycloakService.isUserAdmin()) {
            return this.http.post<void>(AppUtils.BACKEND_API_SOLR_INDEX_URL, {}).toPromise();
        }
    }

    public facetSearch(facetSearch: ShanoirSolrFacet, pageable: Pageable): Promise<SolrResultPage> {
        if (!facetSearch.studyName && !facetSearch.subjectName && !facetSearch.examinationComment && !facetSearch.datasetName
            && !facetSearch.datasetStartDate && !facetSearch.datasetEndDate && !facetSearch.datasetType && !facetSearch.datasetNature) {
                return this.http.get<SolrResultPage>(AppUtils.BACKEND_API_SOLR_URL, { 'params': pageable.toParams() })    
                .map((solrResultPage: SolrResultPage) => {
                    return solrResultPage;
                })
            .toPromise();
        } else {
            return this.http.post<SolrResultPage>(AppUtils.BACKEND_API_SOLR_URL, JSON.stringify(facetSearch), { 'params': pageable.toParams() })    
                .map((solrResultPage: SolrResultPage) => {
                    return solrResultPage;
                })
            .toPromise();
        }   
    }

    // private toRealObject(entity: T) {
    //     let trueObject = Object.assign(this.getEntityInstance(entity), entity);
    //     Object.keys(entity).forEach(key => {
    //         let value = entity[key];
    //         // For Date Object, put the json object to a real Date object
    //         if (String(key).indexOf("Date") > -1 && value) {
    //             trueObject[key] = new Date(value);
    //         } 
    //     });
    //     return trueObject;
    // }

}