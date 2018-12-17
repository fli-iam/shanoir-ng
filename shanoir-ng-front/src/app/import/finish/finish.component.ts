import { Component } from '@angular/core';
import { Router } from '@angular/router';

import { BreadcrumbsService, Step } from '../../breadcrumbs/breadcrumbs.service';
import { MsgBoxService } from '../../shared/msg-box/msg-box.service';
import { ImagesUrlUtil } from '../../shared/utils/images-url.util';
import { Subject } from '../../subjects/shared/subject.model';
import { SubjectService } from '../../subjects/shared/subject.service';
import { ImportJob, PatientDicom } from '../dicom-data.model';
import { ImportService } from '../import.service';
import { ImportDataService, ContextData } from '../import.data-service';

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
            // this.patient.subject = new IdNameObject(this.context.subject.id, this.context.subject.name);
            this.patient.subject = Subject.makeSubject(
                    this.context.subject.id, 
                    this.context.subject.name, 
                    this.context.subject.identifier, 
                    this.context.subject.subjectStudy);
            let filteredPatient: PatientDicom = this.patient;
            filteredPatient.studies = this.patient.studies.map(study => {
                study.series = study.series.filter(serie => serie.selected);
                return study;
            });
            importJob.patients.push(filteredPatient);
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