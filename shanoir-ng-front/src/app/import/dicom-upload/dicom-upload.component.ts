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

import { HttpEvent, HttpEventType, HttpResponse } from '@angular/common/http';
import { Component, ViewChild } from '@angular/core';
import { Router } from '@angular/router';
import { BreadcrumbsService } from '../../breadcrumbs/breadcrumbs.service';
import { slideDown } from '../../shared/animations/animations';
import { ImportDataService } from '../shared/import.data-service';
import { ImportService } from '../shared/import.service';
import { LoadingBarComponent } from '../../shared/components/loading-bar/loading-bar.component';
import { Subscription } from 'rxjs';
import { OnDestroy } from '@angular/core';
import { Study } from '../../studies/shared/study.model';
import { StudyService } from '../../studies/shared/study.service';
import { Center } from '../../centers/shared/center.model';
import { StudyCard } from '../../study-cards/shared/study-card.model';
import { StudyCardService } from '../../study-cards/shared/study-card.service';
import { Option } from '../../shared/select/select.component';
import { ImportJob } from '../shared/dicom-data.model';
import { TaskState } from 'src/app/async-tasks/task.model';
import { StudyLight } from 'src/app/studies/shared/study.dto';
import {CenterService} from "../../centers/shared/center.service";
import {AcquisitionEquipment} from "../../acquisition-equipments/shared/acquisition-equipment.model";
import {AcquisitionEquipmentPipe} from "../../acquisition-equipments/shared/acquisition-equipment.pipe";

type Status = 'none' | 'uploading' | 'uploaded' | 'error';

@Component({
    selector: 'dicom-upload',
    templateUrl: 'dicom-upload.component.html',
    styleUrls: ['dicom-upload.component.css', '../shared/import.step.css'],
    animations: [slideDown],
    standalone: false
})
export class DicomUploadComponent implements OnDestroy {

    subscriptions: Subscription[] = [];
    archiveStatus: Status = 'none';
    extensionError: boolean;
    dicomDirMissingError: boolean;
    fileTooBigError: boolean;
    multipleExamImport: boolean = false;
    study: Study;
    studyCard: StudyCard;
    center: Center;
    modality: string;
    studyOptions: Option<Study>[] = [];
    studycardOptions: Option<StudyCard>[] = [];
    otherErrorMessage: string;
    uploadState: TaskState = new TaskState();
    useStudyCard: boolean = true;
    public centerOptions: Option<Center>[] = [];
    public acquisitionEquipment: AcquisitionEquipment;
    public acquisitionEquipmentOptions: Option<AcquisitionEquipment>[] = [];

    constructor(
            private importService: ImportService,
            private studyService: StudyService,
            private studyCardService: StudyCardService,
            private router: Router,
            private breadcrumbsService: BreadcrumbsService,
            private importDataService: ImportDataService,
            public centerService: CenterService,
            public acqEqPipe: AcquisitionEquipmentPipe) {

        setTimeout(() => {
            breadcrumbsService.currentStepAsMilestone();
            breadcrumbsService.currentStep.label = '1. Upload';
            breadcrumbsService.currentStep.importStart = true;
            breadcrumbsService.currentStep.importMode = 'DICOM';
        });

        this.studyService.getAll().then(allStudies => {
            for (let study of allStudies) {
                    let studyOption: Option<Study> = new Option(study, study.name);
                    this.studyOptions.push(studyOption);
                }
        });
    }

    public uploadArchive(fileEvent: any): void {
        if (fileEvent.target.files.length > 0) {
            this.setArchiveStatus('uploading');
            this.uploadToServer(fileEvent.target.files);
        } else {
            this.setArchiveStatus('none');
            this.modality = null;
        }
    }

    private uploadToServer(file: any) {
        this.dicomDirMissingError = false;
        this.fileTooBigError = false;
        this.extensionError = file[0].name.substring(file[0].name.lastIndexOf("."), file[0].name.length) != '.zip';

        this.modality = null;
        let formData: FormData = new FormData();
        formData.append('file', file[0], file[0].name);
        if (!this.multipleExamImport) {
            this.subscriptions.push(
            this.importService.uploadFile(formData)
                .subscribe(
                    event => {
                    if (event.type === HttpEventType.Sent) {
                        this.uploadState.progress = 0;
                    } else if (event.type === HttpEventType.UploadProgress) {
                        this.uploadState.progress = (event.loaded / (event.total + 0.05));
                    } else if (event instanceof HttpResponse) {
                        let patientDicomList =  event.body;
                        this.modality = patientDicomList.patients[0]?.studies[0]?.series[0]?.modality?.toString();
                        this.importDataService.patientList = patientDicomList;
                        this.setArchiveStatus('uploaded');
                        this.uploadState.progress = 1;
                    }
                }, error => {
                    this.setArchiveStatus('error');
                    this.uploadState.progress = 0;
                    if (error && error.error && error.error.message) {
                        this.dicomDirMissingError = error.error.message.indexOf("DICOMDIR is missing") != -1
                    }
                    this.fileTooBigError = error.status === 413;
                })
            );
        } else {
            // Send to multiple
            let job = new ImportJob();
            job.acquisitionEquipmentId = this.studyCard.acquisitionEquipment.id;
            job.studyId = this.study.id;
            job.studyName = this.study.name;
            job.studyCardId = this.studyCard.id;
            job.acquisitionEquipmentId = this.studyCard.acquisitionEquipment.id;
            job.centerId = this.studyCard.acquisitionEquipment.center.id;
            job.anonymisationProfileToUse = this.study.profile.profileName;

            this.subscriptions.push(
            this.importService.uploadFileMultiple(formData, job)
                .subscribe(
                    event => {
                    if (event.type === HttpEventType.Sent) {
                        this.uploadState.progress = -1;
                    } else if (event.type === HttpEventType.UploadProgress) {
                        this.uploadState.progress = (event.loaded / event.total);
                    } else if (event instanceof HttpResponse) {
                        let patientDicomList =  event.body;
                        this.modality = patientDicomList.patients[0]?.studies[0]?.series[0]?.modality?.toString();
                        if (this.modality) {
                            this.importDataService.patientList = patientDicomList;
                        }
                        this.setArchiveStatus('uploaded');
                    }
                }, error => {
                    this.setArchiveStatus('error');
                    this.uploadState.progress = 0;
                    if (error?.error?.message) {
                        if (error.error.message.indexOf("DICOMDIR is missing") != -1) {
                            this.dicomDirMissingError = true;
                        } else {
                            this.otherErrorMessage = error.error.message;
                        }
                    } else {
                        this.fileTooBigError = error.status === 413;
                    }
                })
            );
        }
    }

    private setArchiveStatus(status: Status) {
        this.archiveStatus = status;
    }

    get valid(): boolean {
        return this.archiveStatus == 'uploaded';
    }

    onSelectStudy() {
        this.useStudyCard = this.study.studyCardPolicy == "MANDATORY" ? true : false;
        if (this.useStudyCard) {
            this.studyCardService.getAllForStudy(this.study.id).then(studycards => {
                if (!studycards) studycards = [];
                this.studycardOptions = studycards.map(sc => {
                    let opt = new Option(sc, sc.name);
                    return opt;
                });
            });
        } else {
            this.getCenterOptions(this.study).then(options => {
                this.centerOptions = options;
                return this.selectDefaultCenter(options);
            });
            this.getEquipmentOptions(this.center);
        }
    }

    private getCenterOptions(study: Study): Promise<Option<Center>[]> {
        if (study && study.id && study.studyCenterList) {
            return this.centerService.getCentersByStudyId(study.id).then(centers => {
                return centers.map(center => {
                    let centerOption = new Option<Center>(center, center.name);
                    return centerOption;
                });
            });
        } else {
            return Promise.resolve([]);
        }
    }

    private selectDefaultCenter(options: Option<Center>[]): Promise<void> {
        let founded = options?.find(option => option.compatible)?.value;
        if (founded) {
            this.center = founded;
            return this.onSelectCenter();
        } else if (options?.length == 1) {
            this.center = options[0].value;
            return this.onSelectCenter();
        }
    }

    public onSelectCenter(): Promise<any> {
        this.acquisitionEquipment = null;
        this.acquisitionEquipmentOptions = this.getEquipmentOptions(this.center);
        return Promise.resolve();
    }

    private getEquipmentOptions(center: Center): Option<AcquisitionEquipment>[] {
        return center?.acquisitionEquipments?.map(acqEq => {
            let option = new Option<AcquisitionEquipment>(acqEq, this.acqEqPipe.transform(acqEq));
            option.compatible = this.acqEqCompatible(acqEq);
            return option;
        });
    }

    acqEqCompatible(acquisitionEquipment: AcquisitionEquipment): boolean | undefined {
        return undefined;
    }

    onSelectStudyCard(){
        this.center = this.studyCard.acquisitionEquipment.center;
    }

    next() {
        this.router.navigate(['imports/series']);
    }

    ngOnDestroy() {
        for(let subscription of this.subscriptions) {
            subscription.unsubscribe();
        }
    }
}
