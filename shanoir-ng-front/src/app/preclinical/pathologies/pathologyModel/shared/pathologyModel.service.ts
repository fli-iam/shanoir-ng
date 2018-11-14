import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs/Observable';
import { EntityService } from '../../../../shared/components/entity/entity.abstract.service';

import { PathologyModel } from './pathologyModel.model';
import { Pathology } from '../../pathology/shared/pathology.model';
import * as PreclinicalUtils from '../../../utils/preclinical.utils';

@Injectable()
export class PathologyModelService  extends EntityService<PathologyModel>{

    API_URL = PreclinicalUtils.PRECLINICAL_API_PATHOLOGY_MODELS_URL;

    getEntityInstance() { return new PathologyModel(); }

    getPathologyModelsByPathology(pathology:Pathology): Promise<PathologyModel[]> {
        const url = `${PreclinicalUtils.PRECLINICAL_API_PATHOLOGIES_URL}${pathology.id}/${PreclinicalUtils.PRECLINICAL_MODEL_DATA}${PreclinicalUtils.PRECLINICAL_ALL_URL}`;
        return this.http.get<PathologyModel[]>(url)
            .map(entities => entities.map((entity) => this.toRealObject(entity)))    
            .toPromise();
    }

    
    	
    getUploadUrl(model_id: number): string {
        return `${PreclinicalUtils.PRECLINICAL_API_PATHOLOGY_MODELS_URL}/upload/specs/`+model_id;
    }

    getDownloadUrl(model: PathologyModel): string {
        return `${PreclinicalUtils.PRECLINICAL_API_PATHOLOGY_MODELS_URL}/download/specs/`+model.id;
    }
    
    
     postFile(fileToUpload: File,  model_id: number): Observable<any> {
        const endpoint = this.getUploadUrl(model_id);
        const formData: FormData = new FormData();
        formData.append('files', fileToUpload, fileToUpload.name);
        return this.http
            .post(endpoint, formData)
            .map(response => response);
    }
    
    
    
    
}