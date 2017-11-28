import { Injectable } from "@angular/core";
import { Http, Response } from "@angular/http";

import { PatientsDicom, SerieDicom } from './dicom-data.model';
import * as AppUtils from '../utils/app.utils';

@Injectable()
export class ImportService {

    constructor(private http: Http) {
        this.http = http;
    }

    uploadFile(formData: FormData): Promise<PatientsDicom> {
        return this.http.post(AppUtils.BACKEND_API_UPLOAD_DICOM_URL, formData)
            .toPromise()
            .then(response => response.json() as PatientsDicom)
            .catch((error) => {
                console.error('Error while sending zip files', error);
                return Promise.reject(error.message || error);
        });
    }

    selectSeries(selectedSeries: SerieDicom[]): void {
        this.http.post(AppUtils.BACKEND_API_UPLOAD_DICOM_SELECT_SERIES_URL, JSON.stringify(selectedSeries))
            .toPromise()
            .catch((error) => {
                console.error('Error while sending select series in json fromat', error);
                return Promise.reject(error.message || error);
        });
    }
}  