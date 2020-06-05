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
import { EntityService } from '../../../shared/components/entity/entity.abstract.service';
import { ContrastAgent } from './contrastAgent.model';
import * as PreclinicalUtils from '../../utils/preclinical.utils';
import { HttpClient } from '@angular/common/http';

@Injectable()
export class ContrastAgentService extends EntityService<ContrastAgent>{
             
    API_URL = PreclinicalUtils.PRECLINICAL_API_PROTOCOL_URL;

    constructor(protected http: HttpClient) {
        super(http)
    }

    getEntityInstance() { return new ContrastAgent(); }

    getContrastAgents(protocolId:number): Promise<ContrastAgent[]>{
        const url = PreclinicalUtils.PRECLINICAL_API_PROTOCOL_URL+"/"+protocolId+"/"+PreclinicalUtils.PRECLINICAL_CONTRASTAGENT_DATA+PreclinicalUtils.PRECLINICAL_ALL_URL;
        return this.http.get<ContrastAgent[]>(url)
            .map(entities => entities.map((entity) => this.toRealObject(entity)))    
            .toPromise();
    }
  
    getContrastAgent(protocolId:number): Promise<ContrastAgent>{
        const url = PreclinicalUtils.PRECLINICAL_API_PROTOCOL_URL+"/"+protocolId+"/"+PreclinicalUtils.PRECLINICAL_CONTRASTAGENT_DATA;
        return this.http.get<ContrastAgent>(url)
            .map((entity) => this.toRealObject(entity)) 
            .toPromise();
    }
  
    
    updateConstrastAgent(protocolId:number,agent: ContrastAgent): Promise<ContrastAgent> {
        const url = PreclinicalUtils.PRECLINICAL_API_PROTOCOL_URL+"/"+protocolId+"/"+PreclinicalUtils.PRECLINICAL_CONTRASTAGENT_DATA+"/"+agent.id;
        return this.http
            .put<ContrastAgent>(url, JSON.stringify(agent))
            .map((entity) => this.toRealObject(entity))
            .toPromise();
    }
    
    createConstrastAgent(protocolId:number,agent: ContrastAgent): Promise<ContrastAgent> {
        const url = PreclinicalUtils.PRECLINICAL_API_PROTOCOL_URL+"/"+protocolId+"/"+PreclinicalUtils.PRECLINICAL_CONTRASTAGENT_DATA;
        return this.http
            .post<ContrastAgent>(url, JSON.stringify(agent))
            .map((entity) => this.toRealObject(entity))
            .toPromise();
    }
        
    deletConstrastAgent(protocolId:number,id: number): Promise<void> {
        const url = PreclinicalUtils.PRECLINICAL_API_PROTOCOL_URL+"/"+protocolId+"/"+PreclinicalUtils.PRECLINICAL_CONTRASTAGENT_DATA+"/"+id;
        return this.http.delete<void>(url)
            .toPromise();
    }
 
    
}