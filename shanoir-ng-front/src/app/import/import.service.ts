import { Injectable } from "@angular/core";
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs/Observable';

import { ImportJob, PatientDicom, SerieDicom } from './dicom-data.model';
import * as AppUtils from '../utils/app.utils';

@Injectable()
export class ImportService {

    constructor(private http: HttpClient) { }

    uploadFile(formData: FormData): Observable<ImportJob> {
        return this.http.post<ImportJob>(AppUtils.BACKEND_API_UPLOAD_DICOM_URL, formData)
            .map(response => response);
    }

    startImportJob(importJob: ImportJob): Promise<Object> {
        return this.http.post(AppUtils.BACKEND_API_UPLOAD_DICOM_START_IMPORT_JOB_URL, JSON.stringify(importJob))
            .toPromise()
            .catch((error) => {
                return Promise.reject(error.message || error);
            });
    }
}  