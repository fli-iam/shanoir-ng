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

import { ConsoleService } from 'src/app/shared/console/console.service';

import * as AppUtils from '../../utils/app.utils';


@Injectable()
export class CopyDataService {
    
    private http: HttpClient = inject(HttpClient);
    private consoleService = inject(ConsoleService);
    private readonly BATCH_SIZE: number = 5000;

    public copy(copyData: CopyData): Promise<void> {
        const nbPages: number = Math.ceil(copyData.datasets.length / this.BATCH_SIZE);
        const copyDataBatches: DataCopyDTO[] = [];
        for (let page = 1; page <= nbPages; page++) {
            copyDataBatches.push(this.buildCopyDataDTO(copyData, page, this.BATCH_SIZE));
        }
        let promise: Promise<void> = Promise.resolve();
        if (copyDataBatches.length > 1) {
            this.consoleService.log('info', 'The copy of ' + copyData.datasets.length 
                + ' datasets has started in batch mode, it may take a while. '
                + copyDataBatches.length + ' batch(es) will be processed sequentially.');
        } else {
            this.consoleService.log('info', 'The copy of ' + copyData.datasets.length 
                + ' datasets has started.');
        }
        copyDataBatches.forEach(cd => {
            promise = promise.then(() => {
                return this.copyData(cd);
            });
        });
        return promise;
    }
    
    private copyData(data: DataCopyDTO): Promise<any> {
        return this.http.post<string>(AppUtils.BACKEND_API_STUDY_URL + '/copyDatasets', data, { responseType: 'text' as 'json' })
            .toPromise();
    }

    private buildCopyDataDTO(copyData: CopyData, page: number, pageSize: number): DataCopyDTO {
        const start = (page - 1) * pageSize;
        const end = start + pageSize;
        const datasetSlice = copyData.datasets.slice(start, end);
        const centerIdSet: Set<number> = new Set(datasetSlice.map(d => d.centerId));
        const subjectIdSet: Set<number> = new Set(datasetSlice.map(d => d.subjectId));
        return {
            datasetIds: datasetSlice.map(d => d.datasetId),
            targetStudyId: copyData.targetStudyId,
            centerIds: Array.from(centerIdSet),
            subjects: copyData.subjects
                .filter(s => subjectIdSet.has(s.id))
                .map(s => ({
                    id: s.id,
                    newName: s.newName
                }))
        };
    }
}

export interface DataCopyDTO {
    datasetIds: number[];
    subjects: {
        id: number;
        newName: string;
    }[];
    centerIds: number[];
    targetStudyId?: number;
}

export interface CopyData {
    datasets: {
        datasetId: number;
        centerId: number;
        subjectId: number;
    }[];
    subjects: {
        id: number;
        newName: string;
    }[];
    targetStudyId?: number;
}