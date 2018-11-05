import { Component, forwardRef } from '@angular/core';
import { NG_VALUE_ACCESSOR } from '@angular/forms';

import { AbstractInput } from '../../../shared/form/input.abstract';


@Component({
    selector: 'select-explored-entity',
    templateUrl: 'explored-entity.component.html',
    providers: [
        { 
          provide: NG_VALUE_ACCESSOR,
          useExisting: forwardRef(() => ExploredEntityComponent),
          multi: true
        }
    ]
})

export class ExploredEntityComponent extends AbstractInput {
    
}