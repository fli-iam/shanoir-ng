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

import { HttpClient } from '@angular/common/http';
import { Injectable } from "@angular/core";
import * as AppUtils from '../utils/app.utils';
import { ImportJob } from './dicom-data.model';


@Injectable()
export class ImportService {

    constructor(private http: HttpClient) { }

    uploadFile(formData: FormData): Promise<ImportJob> {
        return this.http.post<ImportJob>(AppUtils.BACKEND_API_UPLOAD_DICOM_URL, formData)
            .toPromise();
    }

    startImportJob(importJob: ImportJob): Promise<Object> {
        return this.http.post(AppUtils.BACKEND_API_UPLOAD_DICOM_START_IMPORT_JOB_URL, JSON.stringify(importJob))
            .toPromise()
            .catch((error) => {
                return Promise.reject(error.message || error);
            });
    }

    /**
     * This function has been added as we need to send the keycloak token in the header,
     * what is not done and easy to do, when we give the entire URL list of the images
     * to Papaya, as we can not and want not to modify Papaya. So we download for Papaya.
     * @param url 
     */
    downloadImage(url: string): Promise<ArrayBuffer> {
        if (!url) throw Error('Cannot download a image without an url');
        return this.http.get(url,
            { observe: 'response', responseType: 'arraybuffer' }
            ).map(response => response.body).toPromise();
    }
}  