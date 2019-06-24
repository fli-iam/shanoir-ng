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
import { BreadcrumbsService, Step } from '../../breadcrumbs/breadcrumbs.service';
import { MsgBoxService } from '../../shared/msg-box/msg-box.service';
import { ImagesUrlUtil } from '../../shared/utils/images-url.util';
import { SimpleSubject } from '../../subjects/shared/subject.model';
import { SubjectService } from '../../subjects/shared/subject.service';
import { ImportJob, PatientDicom } from '../dicom-data.model';
import { ContextData, ImportDataService } from '../import.data-service';
import { ImportService } from '../import.service';

@Component({
    selector: 'finish-import',
    templateUrl: 'finish.component.html',
    styleUrls: ['finish.component.css', '../import.step.css']
})
export class FinishImportComponent {

    private importJob: ImportJob;
    private selectedPatients: PatientDicom[];
    private context: ContextData;
    private importing: boolean = false;
    private step: Step;
    private readonly ImagesUrlUtil = ImagesUrlUtil;

    constructor(
            private importService: ImportService,
            private subjectService: SubjectService,
            private msgService: MsgBoxService,
            private router: Router,
            private breadcrumbsService: BreadcrumbsService,
            private importDataService: ImportDataService) {
            
        if (!this.importDataService.archiveUploaded || !this.importDataService.inMemoryExtracted
                || !importDataService.patients || !importDataService.patients[0]
                || !importDataService.contextData) {
            this.router.navigate(['imports'], {replaceUrl: true});
            return;
        }
        
        breadcrumbsService.nameStep('4. Finish');
        this.importJob = this.importDataService.archiveUploaded;
        this.selectedPatients = this.importDataService.patients;
        this.context = this.importDataService.contextData;
    }

    private get patient(): PatientDicom {
        if (!this.selectedPatients || this.selectedPatients.length <= 0) return null;
        return this.selectedPatients[0];
    }
    
    private startImportJob(): void {
        this.subjectService
            .updateSubjectStudyValues(this.context.subject.subjectStudy)
            .then(() => {
                let that = this;
                this.importing = true;
                this.importData()
                    .then(() => {
                        this.importDataService.reset();
                        this.importing = false;
                        setTimeout(function () {
                            that.msgService.log('info', 'The data has been successfully imported')
                        }, 0);
                        this.router.navigate(['/dataset/list']);
                    }).catch(error => {
                        this.importing = false;
                        throw error;
                    });
            }).catch(error => {
                throw new Error('Could not save the subjectStudy object, the import job has been stopped. Cause : ' + error);
            });
    }

    private importData (): Promise<any> {
        if (true) {
            let importJob = new ImportJob();
            importJob.patients = new Array<PatientDicom>();
            let simpleSubject: SimpleSubject = {
                id: this.context.subject.id,
                name: this.context.subject.name,
                identifier: this.context.subject.identifier, 
                subjectStudyList: [this.context.subject.subjectStudy]
            };
            this.patient.subject = simpleSubject;
            importJob.patients.push(this.patient);
            importJob.workFolder = this.importJob.workFolder;
            importJob.fromDicomZip = true;
            importJob.examinationId = this.context.examination.id;
            importJob.frontStudyId = this.context.study.id;
            importJob.frontAcquisitionEquipmentId = this.context.acquisitionEquipment.id;
            importJob.frontConverterId = this.context.niftiConverter.id;
            return this.importService.startImportJob(importJob);
        }
    }
}