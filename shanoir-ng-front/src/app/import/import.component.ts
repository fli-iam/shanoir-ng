import { Component } from '@angular/core';
import { BreadcrumbsService } from '../breadcrumbs/breadcrumbs.service';

@Component({
    selector: 'import-modality',
    templateUrl: 'import.component.html',
    styleUrls: ['import.component.css']
})
export class ImportComponent {

    constructor(private breadcrumbsService: BreadcrumbsService) {
        breadcrumbsService.markMilestone();
    }

}