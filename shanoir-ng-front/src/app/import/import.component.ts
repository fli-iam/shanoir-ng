/**
 * Shanoir NG - Import, manage and share neuroimaging data
 * Copyright (C) 2009-2019 Inria - https://www.inria.fr/
 * Contact us on https://project.inria.fr/shanoir/
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/gpl-3.0.html
 */
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { BreadcrumbsService } from '../breadcrumbs/breadcrumbs.service';
import { StudyRightsService } from '../studies/shared/study-rights.service';


@Component({
    selector: 'imports',
    templateUrl: 'import.component.html',
    styleUrls: ['import.component.css']
})
export class ImportComponent implements OnInit {

    private importMode: "DICOM" | "PACS";
    private hasOneStudy: boolean = true;

    constructor(
            private breadcrumbsService: BreadcrumbsService, 
            private rightsService: StudyRightsService,
            private route: ActivatedRoute) {

        this.importMode = route.snapshot.firstChild.data['importMode'];
        this.rightsService.hasOnStudyToImport().then(hasOne => this.hasOneStudy = hasOne);

        breadcrumbsService.markMilestone();
        if (this.importMode == 'DICOM') breadcrumbsService.nameStep('1. Upload');
        else if (this.importMode == 'PACS') breadcrumbsService.nameStep('1. Query');
    }
        
    ngOnInit() {
        this.breadcrumbsService.currentStep.importStart = true;  
    }
}