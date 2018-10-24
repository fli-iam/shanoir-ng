import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';

import { BreadcrumbsService, Step } from '../../breadcrumbs/breadcrumbs.service';
import { slideDown } from '../../shared/animations/animations';
import * as AppUtils from '../../utils/app.utils';
import { PatientDicom, SerieDicom } from '../dicom-data.model';
import { ImportService } from '../import.service';

@Component({
    selector: 'select-series',
    templateUrl: 'select-series.component.html',
    styleUrls: ['select-series.component.css', '../import.step.css'],
    animations: [slideDown]
})
export class SelectSeriesComponent implements OnInit {

    private patients: PatientDicom[];
    private workFolder: string;
    private dataFiles: any;
    private detailedPatient: Object;
    private detailedSerie: Object;
    private papayaParams: object[];
    private step: Step;

    constructor(
            private importService: ImportService,
            private breadcrumbsService: BreadcrumbsService,
            private router: Router) {

        breadcrumbsService.nameStep('Import : Series');
    }
        
    ngOnInit() {
        this.step = this.breadcrumbsService.currentStep;
        let previousStep: Step = this.breadcrumbsService.previousStep;
        this.dataFiles = previousStep.data.inMemoryExtracted;
        this.patients = previousStep.data.archiveUploaded.patients;
        this.workFolder = previousStep.data.archiveUploaded.workFolder;
    }

    private showSerieDetails(nodeParams: any): void {
        this.detailedPatient = null;
        if (nodeParams && this.detailedSerie && nodeParams.seriesInstanceUID == this.detailedSerie["seriesInstanceUID"]) {
            this.detailedSerie = null;
        } else {
            this.detailedSerie = nodeParams;
        }
    }

    private showPatientDetails(nodeParams: any): void {
        this.detailedSerie = null;
        if (nodeParams && this.detailedPatient && nodeParams.patientID == this.detailedPatient["patientID"]) {
            this.detailedPatient = null;
        } else {
            this.detailedPatient = nodeParams;
        }
    }

    private onPatientUpdate(): void {
        this.step.data.patients = this.patients;
    }

    private initPapaya(serie: SerieDicom): void {
        if (!serie) return;
        let listOfPromises;
        if (this.dataFiles) {
            listOfPromises = serie.images.map((image) => {
                return this.dataFiles.files[image.path].async("arraybuffer");
            });
        } else {
            listOfPromises = serie.images.map((image) => {
                let url = AppUtils.BACKEND_API_IMAGE_VIEWER_URL + this.workFolder + '/' + image.path;
                return this.importService.downloadImage(url);
            });
         }
        let promiseOfList = Promise.all(listOfPromises);
        promiseOfList.then((values) => {
            let params: object[] = [];
            params['binaryImages'] = [values];
            this.papayaParams = params;
        });
    }

    get valid(): boolean {
        if (!this.patients || this.patients.length == 0) return false;
        for (let patient of this.patients) {
            for (let study of patient.studies) {
                for (let serie of study.series) {
                    if (serie.selected) return true;
                }
            }
        }
        return false;
    }

    private next() {
        this.router.navigate(['imports/context']);
    }
}