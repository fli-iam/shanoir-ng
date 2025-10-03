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
import { Router } from '@angular/router';

import { BreadcrumbsService } from '../../breadcrumbs/breadcrumbs.service';
import { slideDown } from '../../shared/animations/animations';
import { ImportJob } from '../shared/dicom-data.model';
import { ImportService } from '../shared/import.service';
import { StudyService } from '../../studies/shared/study.service';
import { Study } from '../../studies/shared/study.model';
import { Option } from '../../shared/select/select.component';
import { Center } from '../../centers/shared/center.model';
import { CenterService } from '../../centers/shared/center.service';

type Status = 'none' | 'uploading' | 'uploaded' | 'error';

@Component({
    selector: 'bids-upload',
    templateUrl: 'bids-upload.component.html',
    styleUrls: ['bids-upload.component.css'],
    animations: [slideDown],
    standalone: false
})
export class BidsUploadComponent {

    public archiveStatus: Status = 'none';
    protected extensionError: boolean;
    public errorMessage: string;
    public studyOptions: Option<Study>[] = [];
    public study: Study;
    public center: Center;
    public centerOptions: Option<Center>[] = [];

    constructor(
            private importService: ImportService,
            private centerService: CenterService,
            private router: Router,
            private breadcrumbsService: BreadcrumbsService,
            private studyService: StudyService) {

    Promise.all([this.studyService.getAll(), this.centerService.getAll()])
            .then(([allStudies, allCenters]) => {
                this.studyOptions = [];
                for (let study of allStudies) {
                    let studyOption: Option<Study> = new Option(study, study.name);
                    if (study.studyCenterList) {
                        for (let studyCenter of study.studyCenterList) {
                            let center: Center = allCenters.find(center => center.id === studyCenter.center.id);
                            if (center) {
                                studyCenter.center = center;
                            }
                        }
                        this.studyOptions.push(studyOption);
                        // update the selected study as well
                        if (this.study && this.study.id == study.id) {
                            this.study.studyCenterList = study.studyCenterList;
                        }
                    }
                }
            });

        setTimeout(() => {
            breadcrumbsService.currentStepAsMilestone();
            breadcrumbsService.currentStep.label = '1. Upload';
            breadcrumbsService.currentStep.importStart = true;
            breadcrumbsService.currentStep.importMode = 'BIDS';
        });
    }

    public onSelectStudy() {
        this.centerOptions = [];
        if (this.study && this.study.id && this.study.studyCenterList) {
            for (let studyCenter of this.study.studyCenterList) {
                let centerOption = new Option<Center>(studyCenter.center, studyCenter.center.name);
                this.centerOptions.push(centerOption);
            }
        }
        if (this.study.studyCenterList.length == 1) {
            this.center = this.study.studyCenterList[0].center;
        }
    }

    public uploadArchive(fileEvent: any): void {
        if (fileEvent.target.files.length > 0) {
            this.setArchiveStatus('uploading');
            this.uploadToServer(fileEvent.target.files);
        } else {
            this.setArchiveStatus('none');
        }
    }

    private uploadToServer(file: any) {
        this.extensionError = file[0].name.substring(file[0].name.lastIndexOf("."), file[0].name.length) != '.zip';

        let formData: FormData = new FormData();
        formData.append('file', file[0], file[0].name);
        this.importService.uploadBidsFile(formData, this.study.id, this.study.name, this.center.id)
            .then((importJob: ImportJob) => {
                this.setArchiveStatus('uploaded');
                this.errorMessage = "";
            }).catch(error => {
                this.setArchiveStatus('error');
                if (error && error.error && error.error.message) {
                        this.errorMessage = error.error.message;
                    }
            });
    }

    private setArchiveStatus(status: Status) {
        this.archiveStatus = status;
    }

    get valid(): boolean {
        return this.archiveStatus == 'uploaded';
    }
}
