import { Component, Input } from '@angular/core';

import { Mode } from '../../../shared/components/entity/entity.component.abstract';
import { Dataset } from '../../shared/dataset.model';


@Component({
    selector: 'mr-dataset-details',
    templateUrl: 'dataset.mr.component.html'
})

export class MrDatasetComponent {

    @Input() private mode: Mode;
    @Input() private dataset: Dataset;
    
    constructor() {}

}