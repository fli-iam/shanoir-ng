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
import { ImportJob, PatientDicom } from '../shared/dicom-data.model';
import { ContextData, ImportDataService } from '../shared/import.data-service';
import { ImportService } from '../shared/import.service';


@Component({
    selector: 'finish-import',
    templateUrl: 'finish.component.html',
    styleUrls: ['finish.component.css', '../shared/import.step.css']
})
export class FinishImportComponent {

    private importJob: ImportJob;
    private selectedPatients: PatientDicom[];
    private context: ContextData;
    private importing: boolean = false;
    private step: Step;
    private readonly ImagesUrlUtil = ImagesUrlUtil;
    private importMode: "DICOM" | "PACS" | "BRUKER";

    constructor(
            private importService: ImportService,
            private subjectService: SubjectService,
            private msgService: MsgBoxService,
            private router: Router,
            private breadcrumbsService: BreadcrumbsService,
            private importDataService: ImportDataService) {
            
        if (!importDataService.patients || !importDataService.patients[0]
            || !importDataService.contextData || !this.importDataService.patientList) {
            this.router.navigate(['imports'], {replaceUrl: true});
            return;
        }
        
        this.importJob = this.importDataService.patientList;
        if (this.importJob.fromDicomZip) {
            this.importMode = "DICOM";
        } else if (this.importJob.fromPacs) {
            this.importMode = "PACS";
        }
        breadcrumbsService.nameStep('4. Finish');
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
                    .then((importJob: ImportJob) => {
                        this.importDataService.reset();
                        this.importing = false;
                        setTimeout(function () {
                            that.msgService.log('info', 'the import successfully started.')
                        }, 0);
                        // go back to the first step of import
                        if (this.importMode == 'PACS') this.router.navigate(['/imports/pacs']);
                        else if (this.importMode == 'DICOM') this.router.navigate(['/imports/upload']);
                        else this.router.navigate(['/home']);
                    }).catch(error => {
                        this.importing = false;
                        throw error;
                    });
            }).catch(error => {
                throw new Error('Could not save the subjectStudy object, the import job has been stopped. Cause : ' + error);
            });
    }

    private importData(): Promise<any> {
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
            let filteredPatient: PatientDicom = this.patient;
            filteredPatient.studies = this.patient.studies.map(study => {
                study.series = study.series.filter(serie => serie.selected);
                return study;
            });
            importJob.patients.push(filteredPatient);
            importJob.workFolder = this.importJob.workFolder;
            if (this.importMode == 'DICOM') importJob.fromDicomZip = true;
            else if (this.importMode == 'PACS') importJob.fromPacs = true;
            importJob.examinationId = this.context.examination.id;
            importJob.studyId = this.context.study.id;
            importJob.studyCardId = this.context.studyCard.id;
            importJob.acquisitionEquipmentId = this.context.acquisitionEquipment.id;
            importJob.converterId = this.context.niftiConverter.id;
            importJob.subjectName = this.context.subject.name;
            importJob.studyName = this.context.study.name;
            return this.importService.startImportJob(importJob);
        }
    }
}