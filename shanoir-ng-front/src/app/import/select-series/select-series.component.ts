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
import { Component, HostListener } from '@angular/core';

import { BreadcrumbsService } from '../../breadcrumbs/breadcrumbs.service';
import { Router } from '@angular/router';
import { slideDown } from '../../shared/animations/animations';
import * as AppUtils from '../../utils/app.utils';
import { PatientDicom, SerieDicom, StudyDicom } from '../shared/dicom-data.model';
import { ImportDataService } from '../shared/import.data-service';
import { ImportService } from '../shared/import.service';


@Component({
    selector: 'select-series',
    templateUrl: 'select-series.component.html',
    styleUrls: ['select-series.component.css', '../shared/import.step.css'],
    animations: [slideDown],
    standalone: false
})
export class SelectSeriesComponent {

    patients: PatientDicom[];

    public workFolder: string;
    public dataFiles: any;
    public detailedPatient: any;
    public detailedSerie: any;
    public detailedStudy: any;
    public papayaLoadingCallback: () => Promise<any[]>;
    studiesCheckboxes: any = {};

    constructor(
            private importService: ImportService,
            private breadcrumbsService: BreadcrumbsService,
            private router: Router,
            private importDataService: ImportDataService) {

        if (!this.importDataService.patientList) {
            this.router.navigate(['imports'], {replaceUrl: true});
            return;
        }
        breadcrumbsService.nameStep('2. Series');

        this.patients = this.importDataService.patientList.patients;

        this.workFolder = this.importDataService.patientList.workFolder;
    }

    showSerieDetails(serie: SerieDicom): void {
        this.detailedPatient = null;
        this.detailedStudy = null;
        if (serie && this.detailedSerie && serie.seriesInstanceUID == this.detailedSerie["seriesInstanceUID"]) {
            this.detailedSerie = null;
        } else {
            this.detailedSerie = serie;
            if (serie && serie.images) this.papayaLoadingCallback = () => this.initPapaya(serie);
        }
    }

    showStudyDetails(nodeParams: any): void {
        this.detailedSerie = null;
        this.detailedPatient = null;
        if (nodeParams && this.detailedStudy && nodeParams.studyID == this.detailedStudy["studyID"]) {
            this.detailedStudy = null;
        } else {
            this.detailedStudy = nodeParams;
        }
    }

    showPatientDetails(nodeParams: any): void {
        this.detailedSerie = null;
        this.detailedStudy = null;
        if (nodeParams && this.detailedPatient && nodeParams.patientID == this.detailedPatient["patientID"]) {
            this.detailedPatient = null;
        } else {
            this.detailedPatient = nodeParams;
        }
    }

    onStudyCheckChange(checked: boolean, study: StudyDicom, patient: PatientDicom) {
        study.selected = checked;
        if (study.series) study.series.forEach(serie => serie.selected = checked)

        this.onPatientUpdate();
    }

    onSerieCheckChange(study: StudyDicom, patient: PatientDicom) {
        if (study.series) {
            let nbChecked: number = 0;
            study.series.forEach(serie => {
                if (serie.selected) nbChecked++;
            });
            if (nbChecked == study.series.length) {
                this.studiesCheckboxes[study.studyInstanceUID] = true;
                study.selected = true;
            } else if (nbChecked == 0) {
                study.selected = false;
                this.studiesCheckboxes[study.studyInstanceUID] = false;
            } else {
                this.studiesCheckboxes[study.studyInstanceUID] = 'indeterminate';
                study.selected = true;
            }
        }
        this.onPatientUpdate();
    }

    onPatientUpdate(): void {
        this.importDataService.patients = this.patients;
    }

    private initPapaya(serie: SerieDicom): Promise<any[]> {
        let listOfPromises = serie.images.map((image) => {
            return this.importService.downloadImage(AppUtils.BACKEND_API_GET_DICOM_URL, this.workFolder + '/' + image.path);
        });
        let promiseOfList = Promise.all(listOfPromises);
        return promiseOfList.then((values) => {
            let params: any[] = [];
            params['binaryImages'] = [values];
            return params;
        });
    }

    get valid(): boolean {
        if (!this.patients || this.patients.length == 0) return false;
        let studiesNb = 0;
        for (let patient of this.patients) {
            for (let study of patient.studies) {
                if(study.selected){
                  studiesNb += 1;
                }
            }
        }
        return studiesNb == 1;
    }

    next() {
        if (this.breadcrumbsService.findImportMode() == 'PACS') {
            this.router.navigate(['imports/pacs-context']);
        } else {
            this.router.navigate(['imports/context']);
        }
    }

    @HostListener('document:keypress', ['$event']) onKeydownHandler(event: KeyboardEvent) {
        if (event.key == '²') {
            console.log('patients', this.patients);
        }
    }

}
