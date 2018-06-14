import { Component, ViewChild } from '@angular/core';
import { ImportBrukerService } from './importBruker.service';
import { ImportService } from '../../import/import.service';
import { slideDown, preventInitialChildAnimations } from '../../shared/animations/animations';
import { ImportJob, PatientDicom, SerieDicom, EquipmentDicom } from "../../import/dicom-data.model";
import { Router } from '@angular/router';
import { MsgBoxService } from '../../shared/msg-box/msg-box.service';
import { ContextData } from '../../import/clinical-context/clinical-context.component';
import { IdNameObject } from '../../shared/models/id-name-object.model';
import { ImagesUrlUtil } from '../../shared/utils/images-url.util';

type State = 'dicom' | 'series' | 'context' | 'final' | 'none';

@Component({
    selector: 'importBruker-modality',
    templateUrl: 'importBruker.component.html',
    styleUrls: ['../../import/import.component.css', '../../import/import.step.css'],
    animations: [slideDown, preventInitialChildAnimations]
})
export class ImportBrukerComponent  {

    private extracted: any;
    private importJob: ImportJob;
    private selectedPatients: PatientDicom[];
    private context: ContextData;
    private opened: State = 'dicom';
    private importing: boolean = false;

    private dicomValid: boolean = false;
    private seriesValid: boolean = false;
    private contextValid: boolean = false;
    private seriesEnabled: boolean = false;
    private contextEnabled: boolean = false;

    private ImagesUrlUtil = ImagesUrlUtil;
    
    constructor(
        private importBrukerService: ImportBrukerService,
        private importService: ImportService,
        private msgService: MsgBoxService,
        private router: Router,
    ) {}
    
    private onFilesExtracted(extracted) {
        this.extracted = extracted;
    }
    
    private onArchiveUploaded(importJob: ImportJob) {
        this.importJob = importJob;
    }

    private onPatientsChange(patients: PatientDicom[]) {
        this.selectedPatients = patients;
    }
    
    private onContextChange(context: ContextData) {
        this.context = context;
    }

    private get patient(): PatientDicom {
        if (!this.selectedPatients || this.selectedPatients.length <= 0) return null;
        return this.selectedPatients[0];
    }
    
    private isValid(): boolean {
        return this.dicomValid && this.seriesValid && this.contextValid;
    }

    private updateState() {
        this.seriesEnabled = this.dicomValid;
        this.contextEnabled = this.dicomValid && this.seriesValid;
    }

    private toggle(state: State) {
        if (this.opened == state) this.opened = 'none';
        else this.opened = state;
    }
    
    private startImportJob (): void {
        if (true) {
            let importJob = new ImportJob();
            importJob.patients = new Array<PatientDicom>();
            this.patient.subject = new IdNameObject(this.context.subject.id, this.context.subject.name);
            importJob.patients.push(this.patient);
            importJob.workFolder = this.importJob.workFolder;
            importJob.fromDicomZip = true;
            importJob.examinationId = this.context.examination.id;
            importJob.frontStudyId = this.context.study.id;
            importJob.frontStudyCardId = this.context.studycard.id;
            importJob.frontConverterId = this.context.studycard.niftiConverter.id;
            let that = this;
            this.importing = true;
            this.importService.startImportJob(importJob)
                .then(response => {
                    this.importing = false;
                    setTimeout(function () {
                        that.msgService.log('info', 'The data has been successfully imported')
                     }, 0);
                    this.router.navigate(['/dataset-list']);
                }).catch(error => {
                    this.importing = false;
                    throw error;
                });
        }
    }
}