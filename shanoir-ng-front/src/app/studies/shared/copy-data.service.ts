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
import { inject, Injectable } from '@angular/core';

import * as AppUtils from '../../utils/app.utils';
import { CopyData } from '../study/copy-csv.component';


@Injectable()
export class CopyDataService {
    
    private http: HttpClient = inject(HttpClient);

    copyData(data: CopyData): Promise<any> {
        return this.http.post<string>(AppUtils.BACKEND_API_STUDY_URL + '/copyDatasets', data, { responseType: 'text' as 'json' })
            .toPromise();
    }
}