import { Component } from '@angular/core';
import { BreadcrumbsService } from '../../breadcrumbs/breadcrumbs.service';
import { slideDown } from '../../shared/animations/animations';


@Component({
    selector: 'query-pacs',
    templateUrl: 'query-pacs.component.html',
    styleUrls: ['../shared/import.step.css'],
    animations: [slideDown]
})
export class QueryPacsComponent {

    private patientName: string;
    private patientID: string;
    private patientBirthDate: Date;
    private studyDescription: string;
    private studyDate: Date;
    private isStudyDateValid: boolean = true;

    constructor(
        private breadcrumbsService: BreadcrumbsService) {
            breadcrumbsService.nameStep('1. Query');
            breadcrumbsService.markMilestone();
    }

    getValid(): boolean {
        return ((this.patientName != undefined && this.patientName != null)
            || (this.patientID != undefined && this.patientID != null)
            || (this.patientBirthDate != undefined && this.patientBirthDate != null)
            || (this.studyDescription != undefined && this.studyDescription != null)
            || (this.studyDate != undefined && this.studyDate != null && this.isStudyDateValid))
    }

}