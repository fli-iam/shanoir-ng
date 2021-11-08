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
import { ProcessedDataset } from './dataset.processed.model';

@Component({
    selector: 'processed-dataset-details',
    templateUrl: 'dataset.processed.component.html'
})

export class ProcessedDatasetComponent  {
    @Input() protected mode: Mode;
    @Input() public dataset: ProcessedDataset;

}