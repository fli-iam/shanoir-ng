import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';

import { MsgBoxService } from '../../shared/msg-box/msg-box.service';
import { Subject } from '../../subjects/shared/subject.model';
import { SubjectService } from '../../subjects/shared/subject.service';
import { ContextData } from '../clinical-context/clinical-context.component';
import { ImportJob, PatientDicom } from '../dicom-data.model';
import { ImportService } from '../import.service';
import { BreadcrumbsService, Step } from '../../breadcrumbs/breadcrumbs.service';
import { ImagesUrlUtil } from '../../shared/utils/images-url.util';

@Component({
    selector: 'finish-import',
    templateUrl: 'finish.component.html',
    styleUrls: ['finish.component.css', '../import.step.css']
})
export class FinishImportComponent implements OnInit {

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
        private breadcrumbsService: BreadcrumbsService) {
            
            breadcrumbsService.nameStep('Import : Finish');
    }
    
    ngOnInit() {
        this.step = this.breadcrumbsService.currentStep;
        const currentStepIndex: number = this.breadcrumbsService.currentStepIndex;
        let uploadStep: Step = this.breadcrumbsService.steps[currentStepIndex - 3];
        let seriesStep: Step = this.breadcrumbsService.steps[currentStepIndex - 2];
        let contextStep: Step = this.breadcrumbsService.steps[currentStepIndex - 1];
        this.importJob = uploadStep.data.archiveUploaded;
        this.selectedPatients = seriesStep.data.patients;
        this.context = contextStep.data.context;
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
            // this.patient.subject = new IdNameObject(this.context.subject.id, this.context.subject.name);
            this.patient.subject = Subject.makeSubject(
                    this.context.subject.id, 
                    this.context.subject.name, 
                    this.context.subject.identifier, 
                    this.context.subject.subjectStudy);
            importJob.patients.push(this.patient);
            importJob.workFolder = this.importJob.workFolder;
            importJob.fromDicomZip = true;
            importJob.examinationId = this.context.examination.id;
            importJob.frontStudyId = this.context.study.id;
            importJob.frontStudyCardId = this.context.studycard.id;
            importJob.frontConverterId = this.context.studycard.niftiConverter.id;
            return this.importService.startImportJob(importJob);
        }
    }
}