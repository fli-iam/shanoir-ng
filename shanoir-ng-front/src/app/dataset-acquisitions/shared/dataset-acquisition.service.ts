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
import { Injectable } from '@angular/core';

import { BreadcrumbsService } from '../../breadcrumbs/breadcrumbs.service';
import { EntityService } from '../../shared/components/entity/entity.abstract.service';
import { Page, Pageable } from '../../shared/components/table/pageable.model';
import * as AppUtils from '../../utils/app.utils';
import { ServiceLocator } from '../../utils/locator.service';
import {
    DatasetAcquisitionDTO,
    DatasetAcquisitionDTOService,
    ExaminationDatasetAcquisitionDTO,
} from './dataset-acquisition.dto';
import { DatasetAcquisition } from './dataset-acquisition.model';
import { DatasetAcquisitionUtils } from './dataset-acquisition.utils';


@Injectable()
export class DatasetAcquisitionService extends EntityService<DatasetAcquisition> {

    protected dsAcqDtoService: DatasetAcquisitionDTOService = ServiceLocator.injector.get(DatasetAcquisitionDTOService);

    protected bcService: BreadcrumbsService = ServiceLocator.injector.get(BreadcrumbsService);

    API_URL = AppUtils.BACKEND_API_DATASET_ACQUISITION_URL;
    
    constructor(protected http: HttpClient) {
        super(http)
    }

    getEntityInstance(entity): DatasetAcquisition {
        return DatasetAcquisitionUtils.getNewDAInstance(entity.type);
    }

    protected mapEntity = (entity: any): Promise<DatasetAcquisition> => {
        let result: DatasetAcquisition = this.getEntityInstance(entity);
        this.dsAcqDtoService.toDatasetAcquisition(entity, result);
        return Promise.resolve(result);
    }

    protected mapEntityList = (entities: any[]): Promise<DatasetAcquisition[]> => {
        let result: DatasetAcquisition[] = [];
        if (entities) this.dsAcqDtoService.toDatasetAcquisitions(entities, result);
        return Promise.resolve(result);
    }

    getPage(pageable: Pageable): Promise<Page<DatasetAcquisition>> {
        return this.http.get<Page<DatasetAcquisitionDTO>>(AppUtils.BACKEND_API_DATASET_ACQUISITION_URL, { 'params': pageable.toParams() })
            .toPromise()
            .then((page: Page<DatasetAcquisitionDTO>) => {
                if (!page) return null;
                let immediateResult: DatasetAcquisition[] = [];
                this.dsAcqDtoService.toDatasetAcquisitions(page.content, immediateResult);
                return Page.transType<DatasetAcquisition>(page, immediateResult);
            });
    }

    getAllForExamination(examinationId: number): Promise<ExaminationDatasetAcquisitionDTO[]> {
        return this.http.get<ExaminationDatasetAcquisitionDTO[]>(AppUtils.BACKEND_API_DATASET_ACQUISITION_URL + '/examination/' + examinationId)
            .toPromise();
    }

    public stringify(entity: DatasetAcquisition) {
        let dto = new DatasetAcquisitionDTO(entity);
        return JSON.stringify(dto, (key, value) => {
            return this.customReplacer(key, value, dto);
        });
    }
}