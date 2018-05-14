import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs/Observable';

import { ExtraData } from './extradata.model';
import * as PreclinicalUtils from '../../../utils/preclinical.utils';

@Injectable()
export class ExaminationExtraDataService {
        
             
        constructor(private http: HttpClient) { }    
        
        getExtraDatas(examId:number): Promise<ExtraData[]>{
            const url = `${PreclinicalUtils.PRECLINICAL_API_EXAMINATION_URL}/${examId}/${PreclinicalUtils.PRECLINICAL_EXTRA_DATA}${PreclinicalUtils.PRECLINICAL_ALL_URL}`;
            return this.http.get<ExtraData[]>(url)
                    .toPromise()
                    .then(response => response)
                    .catch((error) => {
                        console.error('Error while getting examination extra data', error);
                        return Promise.reject(error.message || error);
            });
        }
  
        getExtraData(id:string): Promise<ExtraData>{
            return this.http.get<ExtraData>(PreclinicalUtils.PRECLINICAL_API_EXAMINATION_URL+"/"+id)
                    .toPromise()
                    .then(response => response)
                    .catch((error) => {
                        console.error('Error while getting examination extra data', error);
                        return Promise.reject(error.message || error);
            });
        }
  
    
        create(datatype:string,extradata: any): Observable<any> {
          const url = `${PreclinicalUtils.PRECLINICAL_API_EXAMINATION_URL}/${extradata.examination_id}/${datatype}`;
          return this.http
            .post<ExtraData>(url, JSON.stringify(extradata))
            .map(res => res);
        }
        
        
     	postFile(fileToUpload: File,  extraData: ExtraData): Observable<any> {
        	const endpoint = this.getUploadUrl(extraData);
        	const formData: FormData = new FormData();
        	formData.append('files', fileToUpload, fileToUpload.name);
        	return this.http
            	.post(endpoint, formData)
            	.map(response => response);
    	}
    	
    	getUploadUrl(extraData: ExtraData): string {
        	return `${PreclinicalUtils.PRECLINICAL_API_EXAMINATION_URL}/${PreclinicalUtils.PRECLINICAL_EXTRA_DATA}/upload/`+extraData.id;
    	}

   		getDownloadUrl(extraData: ExtraData): string {
        	return `${PreclinicalUtils.PRECLINICAL_API_EXAMINATION_URL}/${PreclinicalUtils.PRECLINICAL_EXTRA_DATA}/download/`+extraData.id;
    	}
        
            
    /*
        upload(file:any): Observable<ExaminationExtraData> {
            let formData:FormData = new FormData();
            formData.append('uploadfile', file, file.name);
            //formData.append('EOF','EOF','EOF');
            let headers = new Headers();
            //headers.append('Content-Type', 'multipart/form-data');
            headers.append('enctype', 'multipart/form-data');
            headers.append('Accept', 'application/json');
            let options = new RequestOptions({ headers: headers });
            //this.http.post(`${this.apiEndPoint}`, formData, options)
            return this.http.post(PreclinicalUtils.PRECLINICAL_API_EXTRA_DATA_UPLOAD_PATH, formData, options)
            .map(this.extractData)
            .catch(error => Observable.throw(error));
            
        }
    */
    
        
        delete(extradata: ExtraData): Promise<void> {
        	const url = `${PreclinicalUtils.PRECLINICAL_API_EXAMINATION_URL}/${extradata.examination_id}/${PreclinicalUtils.PRECLINICAL_EXTRA_DATA}/${extradata.id}`;
          	return this.http.delete<void>(url)
            	.toPromise()
            	.catch((error) => {
                	console.error('Error delete ExtraData', error);
                	return Promise.reject(error);
            	});
    	}
    
        download(extradata:ExtraData): Observable<any>{
          const url = `${PreclinicalUtils.PRECLINICAL_API_EXAMINATION_URL}/${extradata.examination_id}/${PreclinicalUtils.PRECLINICAL_EXTRA_DATA}/${extradata.id}/download`;
          return this.http.get<ExtraData>(url);
            //.map(res => res);
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
        
        
        update(datatype :string, id: number,extradata : ExtraData): Observable<ExtraData> {
          const url = `${PreclinicalUtils.PRECLINICAL_API_EXAMINATION_URL}/${extradata.examination_id}/`+datatype+`/`+id;
          return this.http
            .put<ExtraData>(url, JSON.stringify(extradata))
            .map(response => response);
        }
    
}