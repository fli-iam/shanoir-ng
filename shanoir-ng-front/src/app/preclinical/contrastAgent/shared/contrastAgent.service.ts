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
import { HttpClient } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';

import { EntityService } from '../../../shared/components/entity/entity.abstract.service';
import * as PreclinicalUtils from '../../utils/preclinical.utils';

import { ContrastAgent } from './contrastAgent.model';

@Injectable()
export class ContrastAgentService extends EntityService<ContrastAgent>{
             
    API_URL = PreclinicalUtils.PRECLINICAL_API_PROTOCOL_URL;

    constructor(protected http: HttpClient) {
        super(http)
    }

    getEntityInstance() { return new ContrastAgent(); }

    getContrastAgents(protocolId:number): Promise<ContrastAgent[]>{
        const url = PreclinicalUtils.PRECLINICAL_API_PROTOCOL_URL+"/"+protocolId+"/"+PreclinicalUtils.PRECLINICAL_CONTRASTAGENT_DATA+PreclinicalUtils.PRECLINICAL_ALL_URL;
        return firstValueFrom(this.http.get<ContrastAgent[]>(url))
            .then(entities => entities?.map((entity) => this.toRealObject(entity)) || []);
    }
  
    getContrastAgent(protocolId:number): Promise<ContrastAgent>{
        const url = PreclinicalUtils.PRECLINICAL_API_PROTOCOL_URL+"/"+protocolId+"/"+PreclinicalUtils.PRECLINICAL_CONTRASTAGENT_DATA;
        return firstValueFrom(this.http.get<ContrastAgent>(url))
            .then((entity) => this.toRealObject(entity));
    }
  
    
    updateConstrastAgent(protocolId:number,agent: ContrastAgent): Promise<ContrastAgent> {
        const url = PreclinicalUtils.PRECLINICAL_API_PROTOCOL_URL+"/"+protocolId+"/"+PreclinicalUtils.PRECLINICAL_CONTRASTAGENT_DATA+"/"+agent.id;
        return firstValueFrom(this.http
            .put<ContrastAgent>(url, JSON.stringify(agent)))
            .then((entity) => this.toRealObject(entity));
    }
    
    createConstrastAgent(protocolId:number,agent: ContrastAgent): Promise<ContrastAgent> {
        const url = PreclinicalUtils.PRECLINICAL_API_PROTOCOL_URL+"/"+protocolId+"/"+PreclinicalUtils.PRECLINICAL_CONTRASTAGENT_DATA;
        return firstValueFrom(this.http
            .post<ContrastAgent>(url, JSON.stringify(agent)))
            .then((entity) => this.toRealObject(entity));
    }
        
    deletConstrastAgent(protocolId:number,id: number): Promise<void> {
        const url = PreclinicalUtils.PRECLINICAL_API_PROTOCOL_URL+"/"+protocolId+"/"+PreclinicalUtils.PRECLINICAL_CONTRASTAGENT_DATA+"/"+id;
        return firstValueFrom(this.http.delete<void>(url));
    }
 
    
}