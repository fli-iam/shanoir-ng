import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs/Observable';
import { HandleErrorService } from '../../shared/utils/handle-error.service';

import * as PreclinicalUtils from '../utils/preclinical.utils';

@Injectable()
export class ImportBrukerService {

    constructor(private http: HttpClient, private handleErrorService: HandleErrorService) { }

     postFile(fileToUpload: File): Observable<boolean> {
        const endpoint = PreclinicalUtils.PRECLINICAL_API_BRUKER_UPLOAD;
        const formData: FormData = new FormData();
        formData.append('files', fileToUpload, fileToUpload.name);
        return this.http
            .post(endpoint, formData)
            .map(response => response)
            .catch(this.handleErrorService.handleError);
    }
    
}