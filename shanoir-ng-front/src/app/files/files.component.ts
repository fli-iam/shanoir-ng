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

import { Component } from '@angular/core';

import { BreadcrumbsService } from '../breadcrumbs/breadcrumbs.service';
import { StudyService } from '../studies/shared/study.service';
import { DatasetService } from '../datasets/shared/dataset.service';
import { ExtraDataService } from '../preclinical/extraData/extraData/shared/extradata.service';
import { PathologyModelService } from '../preclinical/pathologies/pathologyModel/shared/pathologyModel.service';

@Component({
    selector: 'files',
    templateUrl: 'files.component.html',
    standalone: false
})

export class FilesComponent {

    constructor(private studyService: StudyService,
                private datasetService: DatasetService,
                private extraDataService: ExtraDataService,
                private pathologyModelService: PathologyModelService,
                private breadcrumbsService: BreadcrumbsService) {
        setTimeout(() => {
            breadcrumbsService.currentStepAsMilestone();
            breadcrumbsService.currentStep.label = 'Files';
        });
    }

    openStudiesFilesJson(): void {
        this.studyService.getStudiesFiles().then(json => {
            const blob = new Blob([JSON.stringify(json, null, 2)], { type: 'application/json' });
            const url = URL.createObjectURL(blob);
            const newTab = window.open(url, '_blank');
            // Revoke the object URL after the tab has loaded to free memory
            newTab?.addEventListener('load', () => URL.revokeObjectURL(url));
        });
    }

    openDatasetsFilesJson(): void {
        this.datasetService.getDatasetsFiles().then(json => {
            const blob = new Blob([JSON.stringify(json, null, 2)], { type: 'application/json' });
            const url = URL.createObjectURL(blob);
            const newTab = window.open(url, '_blank');
            // Revoke the object URL after the tab has loaded to free memory
            newTab?.addEventListener('load', () => URL.revokeObjectURL(url));
        });
    }

    openPreclinicalExtraDataFilesJson(): void {
        this.extraDataService.getExtraDataFiles().then(json => {
            const blob = new Blob([JSON.stringify(json, null, 2)], { type: 'application/json' });
            const url = URL.createObjectURL(blob);
            const newTab = window.open(url, '_blank');
            // Revoke the object URL after the tab has loaded to free memory
            newTab?.addEventListener('load', () => URL.revokeObjectURL(url));
        });
    }

    openPreclinicalPathologyModelFilesJson(): void {
        this.pathologyModelService.getPathologyModelFiles().then(json => {
            const blob = new Blob([JSON.stringify(json, null, 2)], { type: 'application/json' });
            const url = URL.createObjectURL(blob);
            const newTab = window.open(url, '_blank');
            // Revoke the object URL after the tab has loaded to free memory
            newTab?.addEventListener('load', () => URL.revokeObjectURL(url));
        });
    }

}
