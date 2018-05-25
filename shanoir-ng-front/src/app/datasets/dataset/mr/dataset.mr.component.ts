import { Component, Input } from '@angular/core';
import { Dataset } from '../../shared/dataset.model';
import { ActivatedRoute, Params } from '@angular/router';


@Component({
    selector: 'mr-dataset-details',
    templateUrl: 'dataset.mr.component.html'
})

export class MrDatasetComponent {

    @Input() private mode: 'create' | 'edit' | 'view';
    @Input() private dataset: Dataset;
    
    constructor() {}

}