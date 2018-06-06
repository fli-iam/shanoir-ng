import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs/Observable';
import { ImportJob } from '../../import/dicom-data.model';

import * as PreclinicalUtils from '../utils/preclinical.utils';
import * as AppUtils from '../../utils/app.utils';

@Injectable()
export class ImportBrukerService {

    constructor(private http: HttpClient) { }

     postFile(fileToUpload: File): Observable<String> {
        const endpoint = PreclinicalUtils.PRECLINICAL_API_BRUKER_UPLOAD;
        const formData: FormData = new FormData();
        formData.append('files', fileToUpload, fileToUpload.name);
        const options = {responseType: 'text' as 'text'};
        return this.http
            .post(endpoint, formData, options)
            .map(response => response);
    }
    
    importDicomFile(filePath: String): Observable<ImportJob> {
        return this.http.post<ImportJob>(AppUtils.BACKEND_API_IMPORT_DICOM_URL, filePath)
            .map(response => response);
    }
    
}