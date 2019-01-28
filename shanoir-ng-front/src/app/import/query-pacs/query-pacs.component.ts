import { Component } from '@angular/core';
import { Router } from '@angular/router';

import { BreadcrumbsService } from '../../breadcrumbs/breadcrumbs.service';
import { slideDown } from '../../shared/animations/animations';
import { DicomArchiveService } from '../shared/dicom-archive.service';
import { ImportJob } from '../shared/dicom-data.model';
import { ImportDataService } from '../shared/import.data-service';
import { ImportService } from '../shared/import.service';

@Component({
    selector: 'query-pacs',
    templateUrl: 'query-pacs.component.html',
    styleUrls: ['../shared/import.step.css'],
    animations: [slideDown]
})
export class QueryPacsComponent {
    constructor(
        private importService: ImportService, 
        private dicomArchiveService: DicomArchiveService,
        private router: Router,
        private breadcrumbsService: BreadcrumbsService,
        private importDataService: ImportDataService) {
    
    breadcrumbsService.nameStep('1. Query');
    breadcrumbsService.markMilestone();
}

}