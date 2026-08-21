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

import { ComponentRef, Injectable } from '@angular/core';

import { ServiceLocator } from 'src/app/utils/locator.service';
import { DatasetService } from 'src/app/datasets/shared/dataset.service';

import { DatasetCopyDialogComponent, InputDataset } from './dataset-copy-dialog.component';

@Injectable()
export class DatasetCopyDialogService {

    constructor(private datasetService: DatasetService) { }

    public open(inputDatasets: InputDataset[]) {
        const modalRef: ComponentRef<DatasetCopyDialogComponent> = ServiceLocator.rootViewContainerRef.createComponent(DatasetCopyDialogComponent);
        modalRef.instance.setUp(inputDatasets, modalRef);
    }

    public openWithIds(datasetIds: Set<number>) {
        this.datasetService.getByIds(datasetIds).then(datasets => {
            const inputDatasets: InputDataset[] = datasets.map(ds => ({
                datasetId: ds.id,
                centerId: ds.centerId,
                subjectId: ds.subject?.id,
                studyId: ds.study?.id
            }));
            this.open(inputDatasets);
        });
    }
}
