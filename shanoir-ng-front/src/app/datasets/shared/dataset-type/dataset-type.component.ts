import { Component, forwardRef } from '@angular/core';
import { NG_VALUE_ACCESSOR } from '@angular/forms';
import { AbstractInput} from '../../../shared/form/input.abstract'


@Component({
    selector: 'select-dataset-type',
    templateUrl: 'dataset-type.component.html',
    providers: [
        { 
          provide: NG_VALUE_ACCESSOR,
          useExisting: forwardRef(() => DatasetTypeComponent),
          multi: true
        }
    ]
})

export class DatasetTypeComponent extends AbstractInput {

}