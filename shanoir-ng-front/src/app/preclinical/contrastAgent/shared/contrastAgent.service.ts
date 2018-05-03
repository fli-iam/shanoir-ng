import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs/Observable';

import { ContrastAgent } from './contrastAgent.model';
import * as PreclinicalUtils from '../../utils/preclinical.utils';
import { HandleErrorService } from '../../../shared/utils/handle-error.service';

@Injectable()
export class ContrastAgentService {
             
        constructor(private http: HttpClient,private handleErrorService: HandleErrorService) { }    
        
        getContrastAgents(protocolId:number): Promise<ContrastAgent[]>{
            const url = PreclinicalUtils.PRECLINICAL_API_PROTOCOL_URL+"/"+protocolId+"/"+PreclinicalUtils.PRECLINICAL_CONTRASTAGENT_DATA+PreclinicalUtils.PRECLINICAL_ALL_URL;
            return this.http.get<ContrastAgent[]>(url)
                    .toPromise()
                    .then(response => response)
                    .catch((error) => {
                        console.error('Error while getting contrast agents', error);
                        return Promise.reject(error.message || error);
            });
        }
  
        getContrastAgent(protocolId:number): Promise<ContrastAgent>{
            const url = PreclinicalUtils.PRECLINICAL_API_PROTOCOL_URL+"/"+protocolId+"/"+PreclinicalUtils.PRECLINICAL_CONTRASTAGENT_DATA;
            return this.http.get<ContrastAgent>(url)
                    .toPromise()
                    .then(response => response)
                    .catch((error) => {
                        console.error('Error while getting contrast agent', error);
                        return Promise.reject(error.message || error);
            });
        }
  
    
        update(protocolId:number,agent: ContrastAgent): Observable<ContrastAgent> {
          const url = PreclinicalUtils.PRECLINICAL_API_PROTOCOL_URL+"/"+protocolId+"/"+PreclinicalUtils.PRECLINICAL_CONTRASTAGENT_DATA+"/"+agent.id;
          return this.http
            .put<ContrastAgent>(url, JSON.stringify(agent))
            .map(response => response)
            .catch(this.handleErrorService.handleError);
        }
    
        create(protocolId:number,agent: ContrastAgent): Observable<ContrastAgent> {
            const url = PreclinicalUtils.PRECLINICAL_API_PROTOCOL_URL+"/"+protocolId+"/"+PreclinicalUtils.PRECLINICAL_CONTRASTAGENT_DATA;
            return this.http
            .post<ContrastAgent>(url, JSON.stringify(agent))
            .map(res => res)
            .catch(this.handleErrorService.handleError);
        }
        
        delete(protocolId:number,id: number): Promise<void> {
        	const url = PreclinicalUtils.PRECLINICAL_API_PROTOCOL_URL+"/"+protocolId+"/"+PreclinicalUtils.PRECLINICAL_CONTRASTAGENT_DATA+"/"+id;
          	return this.http.delete<void>(url)
            	.toPromise()
            	.catch((error) => {
                	console.error('Error delete ContrastAgent', error);
                	return Promise.reject(error);
            	});
    	}
 
    
        /*This method is to avoid unexpected error if returned object is null*/
        private extractData(res: Response) {
            let body;        
            // check if empty, before call json
            if (res.text()) {
                body = res.json();
            }
            return body || {};
        }
    
}