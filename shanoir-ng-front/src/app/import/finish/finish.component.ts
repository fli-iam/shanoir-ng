import { Component } from '@angular/core';
import { Router } from '@angular/router';

import { BreadcrumbsService, Step } from '../../breadcrumbs/breadcrumbs.service';
import { MsgBoxService } from '../../shared/msg-box/msg-box.service';
import { ImagesUrlUtil } from '../../shared/utils/images-url.util';
import { Subject } from '../../subjects/shared/subject.model';
import { SubjectService } from '../../subjects/shared/subject.service';
import { ImportJob, PatientDicom } from '../shared/dicom-data.model';
import { ImportService } from '../shared/import.service';
import { ImportDataService, ContextData } from '../shared/import.data-service';

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
    private importMode: "DICOM" | "PACS";

    constructor(
            private importService: ImportService,
            private subjectService: SubjectService,
            private msgService: MsgBoxService,
            private router: Router,
            private breadcrumbsService: BreadcrumbsService,
            private importDataService: ImportDataService) {
            
        if (!this.importDataService.patientList || !importDataService.patients 
            || !importDataService.patients[0] || !importDataService.contextData) {
            this.router.navigate(['imports'], {replaceUrl: true});
            return;
        }
        
        this.importJob = this.importDataService.patientList;
        if (this.importJob.fromDicomZip) {
            this.importMode = "DICOM";
            if (!this.importDataService.inMemoryExtracted) {
                this.router.navigate(['imports'], {replaceUrl: true});
                return;
            }
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
                            that.msgService.log('info', 'The data has been successfully imported')
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
            // this.patient.subject = new IdNameObject(this.context.subject.id, this.context.subject.name);
            this.patient.subject = Subject.makeSubject(
                    this.context.subject.id, 
                    this.context.subject.name, 
                    this.context.subject.identifier, 
                    this.context.subject.subjectStudy);
            importJob.patients.push(this.patient);
            importJob.workFolder = this.importJob.workFolder;
            if (this.importMode == 'DICOM') importJob.fromDicomZip = true;
            else if (this.importMode == 'PACS') importJob.fromPacs = true;
            importJob.examinationId = this.context.examination.id;
            importJob.frontStudyId = this.context.study.id;
            importJob.frontAcquisitionEquipmentId = this.context.acquisitionEquipment.id;
            importJob.frontConverterId = this.context.niftiConverter.id;
            return this.importService.startImportJob(importJob);
        }
    }
}