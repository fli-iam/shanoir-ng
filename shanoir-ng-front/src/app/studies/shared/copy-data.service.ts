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

    public copy(copyData: CopyData): Promise<void> {
        this.consoleService.log('info', 'The copy of ' + copyData.datasets.length 
            + ' datasets has started.');
        const dto: DataCopyDTO = this.buildCopyDataDTO(copyData);
        return this.copyData(dto);
    }
    
    private copyData(data: DataCopyDTO): Promise<any> {
        return this.http.post<string>(AppUtils.BACKEND_API_STUDY_URL + '/copyDatasets', data, { responseType: 'text' as 'json' })
            .toPromise();
    }

    private buildCopyDataDTO(copyData: CopyData): DataCopyDTO {
        const centerIdSet: Set<number> = new Set(copyData.datasets.map(d => d.centerId));
        const subjectIdSet: Set<number> = new Set(copyData.datasets.map(d => d.subjectId));
        return {
            datasetIds: copyData.datasets.map(d => d.datasetId),
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
    taskId?: string;
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