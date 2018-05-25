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