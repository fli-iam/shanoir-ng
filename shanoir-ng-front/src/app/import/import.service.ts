import { Injectable } from "@angular/core";
import { Http, Response } from "@angular/http";
import { Observable } from 'rxjs/Observable';

import { PatientsDicom, SerieDicom } from './dicom-data.model';
import * as AppUtils from '../utils/app.utils';
import { HandleErrorService } from '../shared/utils/handle-error.service';

@Injectable()
export class ImportService {

    constructor(private http: Http, private handleErrorService: HandleErrorService) {
        this.http = http;
    }

    uploadFile(formData: FormData): Observable<PatientsDicom> {
        return this.http.post(AppUtils.BACKEND_API_UPLOAD_DICOM_URL, formData)
            .map(response => response.json() as PatientsDicom)
            .catch(this.handleErrorService.handleError);
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