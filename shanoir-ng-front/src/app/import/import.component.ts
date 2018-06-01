import { Component, ViewChild } from '@angular/core';
import { ImportService } from './import.service';
import { slideDown, preventInitialChildAnimations } from '../shared/animations/animations';
import { ImportJob, PatientDicom, SerieDicom, EquipmentDicom } from "./dicom-data.model";
import { Router } from '@angular/router';
import { MsgBoxService } from '../shared/msg-box/msg-box.service';
import { ContextData } from './clinical-context/clinical-context.component';
import { IdNameObject } from '../shared/models/id-name-object.model';

type State = 'dicom' | 'series' | 'context' | 'final' | 'none';

@Component({
    selector: 'import-modality',
    templateUrl: 'import.component.html',
    styleUrls: ['import.component.css', 'import.step.css'],
    animations: [slideDown, preventInitialChildAnimations]
})
export class ImportComponent  {

    private extracted: any;
    private importJob: ImportJob;
    private extractedPatients: PatientDicom[];
    private selectedPatients: PatientDicom[];
    private context: ContextData;
    private opened: State = 'dicom';

    private dicomValid: boolean = false;
    private seriesValid: boolean = false;
    private contextValid: boolean = false;
    private seriesEnabled: boolean = false;
    private contextEnabled: boolean = false;
    
    constructor(
        private importService: ImportService,
        private msgService: MsgBoxService,
        private router: Router,
    ) {}
    
    private onFilesExtracted(extracted) {
        this.extracted = extracted;
    }
    
    private onArchiveUploaded(importJob: ImportJob) {
        this.extractedPatients = importJob.patients;
        this.importJob = importJob;
    }

    private onPatientsChange(patients: PatientDicom[]) {
        this.selectedPatients = patients;
    }

    private get patient(): PatientDicom {
        if (!this.selectedPatients || this.selectedPatients.length <= 0) return null;
        return this.selectedPatients[0];
    }

    private onContextChange(context: ContextData) {
        this.context = context;
    }
    
    private startImportJob (): void {
        if (true) {
            let importJob = new ImportJob();
            importJob.patients = new Array<PatientDicom>();
            this.patient.subject = this.context.subject;
            importJob.patients.push(this.patient);
            importJob.workFolder = this.importJob.workFolder;
            importJob.fromDicomZip = true;
            importJob.examinationId = this.context.examination.id;
            importJob.frontStudyId = this.context.study.id;
            importJob.frontStudyCardId = this.context.studycard.id;
            importJob.frontConverterId = this.context.studycard.niftiConverter.id;
            let that = this;
            this.importService.startImportJob(importJob)
                .then(response => {
                    setTimeout(function () {
                        that.msgService.log('info', 'The data has been successfully imported')
                     }, 0);
                    this.router.navigate(['/dataset-list']);
                });
        }
    }

    private isValid(): boolean {
        return this.patient && this.importJob 
            && this.context != undefined && this.context != null;
    }

    private updateState() {
        this.seriesEnabled = this.dicomValid;
        this.contextEnabled = this.dicomValid && this.seriesValid;
    }

    private toggle(state: State) {
        if (this.opened == state) this.opened = 'none';
        else this.opened = state;
    }
}