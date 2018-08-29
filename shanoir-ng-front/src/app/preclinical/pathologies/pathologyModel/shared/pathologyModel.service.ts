import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs/Observable';

import { PathologyModel } from './pathologyModel.model';
import { Pathology } from '../../pathology/shared/pathology.model';
import * as PreclinicalUtils from '../../../utils/preclinical.utils';

@Injectable()
export class PathologyModelService {

    constructor(private http: HttpClient) { }

    getPathologyModels(): Promise<PathologyModel[]> {
        return this.http.get<PathologyModel[]>(PreclinicalUtils.PRECLINICAL_API_PATHOLOGY_MODELS_ALL_URL)
            .toPromise();
    }

    getPathologyModelsByPathology(pathology:Pathology): Promise<PathologyModel[]> {
        const url = `${PreclinicalUtils.PRECLINICAL_API_PATHOLOGIES_URL}${pathology.id}/${PreclinicalUtils.PRECLINICAL_MODEL_DATA}${PreclinicalUtils.PRECLINICAL_ALL_URL}`;
        return this.http.get<PathologyModel[]>(url)
            .toPromise()
            .then(response => response)
            .catch((error) => {
                console.error('Error while getting pathology models ', error);
                return Promise.reject(error.message || error);
            });
    }

        
    getPathologyModel(id: string): Promise<PathologyModel>{
            return this.http.get<PathologyModel>(PreclinicalUtils.PRECLINICAL_API_PATHOLOGY_MODELS_URL+"/"+id)
                    .toPromise()
                    .then(response => response)
                    .catch((error) => {
                        console.error('Error while getting PathologyModel', error);
                        return Promise.reject(error.message || error);
            });
        }


    update(model: PathologyModel): Observable<PathologyModel> {
        const url = `${PreclinicalUtils.PRECLINICAL_API_PATHOLOGY_MODELS_URL}/`+model.id;
        return this.http
            .put<PathologyModel>(url, JSON.stringify(model))
            .map(response => response);
    }

    create(model: PathologyModel): Observable<PathologyModel> {
        return this.http
            .post<PathologyModel>(PreclinicalUtils.PRECLINICAL_API_PATHOLOGY_MODELS_URL, JSON.stringify(model))
            .map(res => res);
    }

	delete(id: number): Promise<void> {
		const url = `${PreclinicalUtils.PRECLINICAL_API_PATHOLOGY_MODELS_URL}/`+id;
        return this.http.delete<void>(url)
            	.toPromise()
            	.catch((error) => {
                	console.error('Error delete PathologyModel', error);
                	return Promise.reject(error);
            	});
    }
    	
    getUploadUrl(model: PathologyModel): string {
        return `${PreclinicalUtils.PRECLINICAL_API_PATHOLOGY_MODELS_URL}/upload/specs/`+model.id;
    }

    getDownloadUrl(model: PathologyModel): string {
        return `${PreclinicalUtils.PRECLINICAL_API_PATHOLOGY_MODELS_URL}/download/specs/`+model.id;
    }
    
    
     postFile(fileToUpload: File,  model: PathologyModel): Observable<any> {
        const endpoint = this.getUploadUrl(model);
        const formData: FormData = new FormData();
        formData.append('files', fileToUpload, fileToUpload.name);
        return this.http
            .post(endpoint, formData)
            .map(response => response);
    }
    
    
    
    
}