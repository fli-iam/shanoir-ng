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
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs/Observable';
import { ImportJob } from '../../import/shared/dicom-data.model';

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
            .post(endpoint, formData, options);
    }
    
    importDicomFile(filePath: String): Observable<ImportJob> {
        return this.http.post<ImportJob>(AppUtils.BACKEND_API_IMPORT_DICOM_URL, filePath);
    }
    
}