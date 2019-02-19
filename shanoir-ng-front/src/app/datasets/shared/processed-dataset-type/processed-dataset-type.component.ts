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

import { Component, forwardRef } from '@angular/core';
import { NG_VALUE_ACCESSOR } from '@angular/forms';
import { AbstractInput} from '../../../shared/form/input.abstract'


@Component({
    selector: 'select-processed-dataset-type',
    templateUrl: 'processed-dataset-type.component.html',
    providers: [
        { 
          provide: NG_VALUE_ACCESSOR,
          useExisting: forwardRef(() => ProcessedDatasetTypeComponent),
          multi: true
        }
    ]
})

export class ProcessedDatasetTypeComponent extends AbstractInput {

}