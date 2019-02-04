import { Component } from '@angular/core';
import { BreadcrumbsService } from '../../breadcrumbs/breadcrumbs.service';
import { slideDown } from '../../shared/animations/animations';
import { Router } from '@angular/router';
import { ImportService } from '../shared/import.service';
import { DicomQuery, ImportJob } from '../shared/dicom-data.model';
import { ImportDataService } from '../shared/import.data-service';


@Component({
    selector: 'query-pacs',
    templateUrl: 'query-pacs.component.html',
    styleUrls: ['../shared/import.step.css'],
    animations: [slideDown]
})
export class QueryPacsComponent {

    private dicomQuery: DicomQuery = new DicomQuery();
    private isStudyDateValid: boolean = true;

    constructor(
        private breadcrumbsService: BreadcrumbsService,
        private router: Router,
        private importService: ImportService,
        private importDataService: ImportDataService) {
            breadcrumbsService.nameStep('1. Query');
            breadcrumbsService.markMilestone();
    }

    get valid(): boolean {
        return ((this.dicomQuery.patientName != undefined && this.dicomQuery.patientName != null)
            || (this.dicomQuery.patientID != undefined && this.dicomQuery.patientID != null)
            || (this.dicomQuery.patientBirthDate != undefined && this.dicomQuery.patientBirthDate != null)
            || (this.dicomQuery.studyDescription != undefined && this.dicomQuery.studyDescription != null)
            || (this.dicomQuery.studyDate != undefined && this.dicomQuery.studyDate != null && this.isStudyDateValid))
    }

    queryPACS(): void {
        this.importService.queryPACS(this.dicomQuery).then((importJob: ImportJob) => {
            if (importJob && importJob.patients.length > 0) {
                this.importDataService.patientList = importJob;
                this.router.navigate(['imports/series']);}
        })
    }

}