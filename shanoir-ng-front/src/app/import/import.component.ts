import { Component, OnInit } from '@angular/core';
import { BreadcrumbsService } from '../breadcrumbs/breadcrumbs.service';

@Component({
    selector: 'import-modality',
    templateUrl: 'import.component.html',
    styleUrls: ['import.component.css']
})
export class ImportComponent implements OnInit {

    constructor(private breadcrumbsService: BreadcrumbsService){
    }
    
    ngOnInit() {
        this.breadcrumbsService.currentStep.importStart = true;
    }

}