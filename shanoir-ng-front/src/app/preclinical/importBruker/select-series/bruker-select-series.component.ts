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


import { ImportDataService } from '../../../import/shared/import.data-service';
import { BreadcrumbsService, Step } from '../../../breadcrumbs/breadcrumbs.service';
import { Router } from '@angular/router';
import { slideDown } from '../../../shared/animations/animations';
import * as AppUtils from '../../../utils/app.utils';
import { PatientDicom, SerieDicom } from '../../../import/shared/dicom-data.model';
import { ImportService } from '../../../import/shared/import.service';

@Component({
    selector: 'bruker-select-series',
    templateUrl: 'bruker-select-series.component.html',
    styleUrls: ['bruker-select-series.component.css', '../../../import/shared/import.step.css'],
    animations: [slideDown],
    standalone: false
})
export class BrukerSelectSeriesComponent {

    public patients: PatientDicom[];
    private workFolder: string;
    public detailedPatient: any;
    public detailedSerie: any;
    public papayaLoadingCallback: () => Promise<any[]>;

    constructor(
            private importService: ImportService,
            private breadcrumbsService: BreadcrumbsService,
            private router: Router,
            private importDataService: ImportDataService) {

        if (!this.importDataService.archiveUploaded ) {
            this.router.navigate(['importsBruker'], {replaceUrl: true});
            return;
        }
        breadcrumbsService.nameStep('2. Series');
        this.patients = this.importDataService.archiveUploaded.patients;
        this.workFolder = this.importDataService.archiveUploaded.workFolder;
    }


    public showSerieDetails(serie: SerieDicom): void {
        this.detailedPatient = null;
        if (serie && this.detailedSerie && serie.seriesInstanceUID == this.detailedSerie["seriesInstanceUID"]) {
            this.detailedSerie = null;
        } else {
            this.detailedSerie = serie;
            if (serie && serie.images) this.papayaLoadingCallback = () => this.initPapaya(serie);
        }
    }

    public showPatientDetails(nodeParams: any): void {
        this.detailedSerie = null;
        if (nodeParams && this.detailedPatient && nodeParams.patientID == this.detailedPatient["patientID"]) {
            this.detailedPatient = null;
        } else {
            this.detailedPatient = nodeParams;
        }
    }
    
    private initPapaya(serie: SerieDicom): Promise<any[]> {
        let listOfPromises = serie.images.map((image) => {
            return this.importService.downloadImage(AppUtils.BACKEND_API_GET_DICOM_URL, this.workFolder + '/' + image.path);
        });
        let promiseOfList = Promise.all(listOfPromises);
        return promiseOfList.then((values) => {
            let params: object[] = [];
            params['binaryImages'] = [values];
            return params;
        });
    }

    onStudyCheckChange(checked: boolean, study) {
        if (study.series) {
            study.series.forEach(serie => serie.selected = checked)
        }
        this.onPatientUpdate();
    }

    public onPatientUpdate(): void {
        this.importDataService.patients = this.patients;
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

    public next() {
        this.router.navigate(['imports/preclinical-context']);
    }
}