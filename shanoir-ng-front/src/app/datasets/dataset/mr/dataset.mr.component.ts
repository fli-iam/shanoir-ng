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

import { Component, Input, OnInit } from '@angular/core';
import { Mode } from '../../../shared/components/entity/entity.component.abstract';
import { MrDataset, MrDatasetMetadata, MrDatasetNature } from './dataset.mr.model';
import { Option } from '../../../shared/select/select.component';

@Component({
    selector: 'mr-dataset-details',
    templateUrl: 'dataset.mr.component.html'
})

export class MrDatasetComponent implements OnInit{

    @Input() mode: Mode;
    @Input() dataset: MrDataset;
    // allMrDatasetNatures: any[];
    private natureOptions: Option<MrDatasetNature>[];
    
    constructor() {
        this.natureOptions = MrDatasetNature.options;
    }
    
    ngOnInit() {
        if (!this.dataset.updatedMrMetadata) this.dataset.updatedMrMetadata = new MrDatasetMetadata();
    }

    get natureLabel(): string {
        return this.dataset.updatedMrMetadata ? MrDatasetNature.getLabel(this.dataset.updatedMrMetadata.mrDatasetNature) : null;
    }
}