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
import { FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';

import { EntityComponent } from '../../shared/components/entity/entity.component.abstract';
import { DatasetProcessingType } from '../../enum/dataset-processing-type.enum';
import { DatasetProcessing } from '../../datasets/shared/dataset-processing.model';
import { DatasetProcessingService } from '../shared/dataset-processing.service';
import { StudyService } from '../../studies/shared/study.service';
import { Study } from '../../studies/shared/study.model';
import { EntityService } from '../../shared/components/entity/entity.abstract.service';
import { BrowserPaging } from '../../shared/components/table/browser-paging.model';
import { Dataset } from '../shared/dataset.model';
import { FilterablePageable, Page } from 'src/app/shared/components/table/pageable.model';

@Component({
    selector: 'dataset-processing-detail',
    templateUrl: 'dataset-processing.component.html',
    styleUrls: ['dataset-processing.component.css']
})

export class DatasetProcessingComponent extends EntityComponent<DatasetProcessing> {

    study: Study

    private inputDatasetsPromise: Promise<any>;
    private outputDatasetsPromise: Promise<any>;
    private inputDatasetsBrowserPaging: BrowserPaging<Dataset>;
    private outputDatasetsBrowserPaging: BrowserPaging<Dataset>;
    public inputDatasetsColumnDefs: any[];
    public outputDatasetsColumnDefs: any[];
    
    constructor(
            private route: ActivatedRoute,
            private studyService: StudyService,
            private datasetProcessingService: DatasetProcessingService
            ) {

        super(route, 'dataset-processing');
    }

    get datasetProcessing(): DatasetProcessing { return this.entity; }
    set datasetProcessing(datasetProcessing: DatasetProcessing) { this.entity = datasetProcessing; }

    getService(): EntityService<DatasetProcessing> {
        return this.datasetProcessingService;
    }

    initializeTables() {
        this.createColumnDefs();
        this.inputDatasetsPromise = Promise.resolve().then(() => {
            this.inputDatasetsBrowserPaging = new BrowserPaging([], this.inputDatasetsColumnDefs);
        });
        this.outputDatasetsPromise = Promise.resolve().then(() => {
            this.outputDatasetsBrowserPaging = new BrowserPaging([], this.outputDatasetsColumnDefs);
        });
    }

    setDatasetProcessing(datasetProcessing: DatasetProcessing) {
        this.datasetProcessing = datasetProcessing;
        if(this.datasetProcessing.studyId != null) {
            this.studyService.get(this.datasetProcessing.studyId).then((study)=> this.study = study)
        }
    }

    initView(): Promise<void> {
        this.initializeTables();
        return this.datasetProcessingService.get(this.id).then(this.setDatasetProcessing);
    }

    initEdit(): Promise<void> {
        this.initializeTables();
        return this.datasetProcessingService.get(this.id).then(this.setDatasetProcessing);
    }

    initCreate(): Promise<void> {
        this.initializeTables();
        this.entity = new DatasetProcessing();
        return Promise.resolve();
    }

    buildForm(): FormGroup {

        return this.formBuilder.group({
            'study': [this.study ? this.study.name : ''],
            'processingType': [this.datasetProcessing.datasetProcessingType],
            'processingDate': [this.datasetProcessing.processingDate],
            'comment': [this.datasetProcessing.comment]
        });
    }

    public async hasEditRight(): Promise<boolean> {
        return this.keycloakService.isUserAdminOrExpert();
    }


    getInputDatasetsPage(pageable: FilterablePageable): Promise<Page<Dataset>> {
        return new Promise((resolve) => {
            this.inputDatasetsPromise.then(() => {
                resolve(this.inputDatasetsBrowserPaging.getPage(pageable));
            });
        });
    }

    getOutputDatasetsPage(pageable: FilterablePageable): Promise<Page<Dataset>> {
        return new Promise((resolve) => {
            this.outputDatasetsPromise.then(() => {
                resolve(this.outputDatasetsBrowserPaging.getPage(pageable));
            });
        });
    }

    private createColumnDefs() {
        function checkNullValueReference(reference: any) {
            if(reference){
                return reference.value;
            }
            return '';
        };
        function checkNullValue(value: any) {
            if(value){
                return value;
            }
            return '';
        };
        this.inputDatasetsColumnDefs = [
            {headerName: "Name", field: "name.value", type: "reference", cellRenderer: function (params: any) {
                return checkNullValueReference(params.data.name);
            }},
            {headerName: "Concentration", field: "concentration", type: "number", cellRenderer: function (params: any) {
                return checkNullValue(params.data.concentration);
            }},
            {headerName: "Concentration Unit", field: "concentration_unit.value", type: "reference", cellRenderer: function (params: any) {
                return checkNullValueReference(params.data.concentration_unit);
            }}
        ];

        if (this.mode != 'view' && this.keycloakService.isUserAdminOrExpert()) {
            this.inputDatasetsColumnDefs.push({ headerName: "", type: "button", awesome: "fa-edit", action: item => this.editIngredient(item) });
        }
        if (this.mode != 'view' && this.keycloakService.isUserAdminOrExpert()) {
            this.inputDatasetsColumnDefs.push({ headerName: "", type: "button", awesome: "fa-trash", action: (item) => this.removeIngredient(item) });
        }
    }

}