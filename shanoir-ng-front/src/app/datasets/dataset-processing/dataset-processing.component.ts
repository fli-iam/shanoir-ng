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

import { Component, ViewChild } from '@angular/core';
import { FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';

import { EntityComponent } from '../../shared/components/entity/entity.component.abstract';
import { DatasetProcessingType } from '../../enum/dataset-processing-type.enum';
import { Dataset } from '../shared/dataset.model';
import { DatasetProcessing } from '../../datasets/shared/dataset-processing.model';
import { DatasetProcessingService } from '../shared/dataset-processing.service';
import { StudyService } from '../../studies/shared/study.service';
import { Study } from '../../studies/shared/study.model';
import { EntityService } from '../../shared/components/entity/entity.abstract.service';
import { BrowserPaging } from '../../shared/components/table/browser-paging.model';
import { FilterablePageable, Page } from '../../shared/components/table/pageable.model';
import { TableComponent } from '../../shared/components/table/table.component';

@Component({
    selector: 'dataset-processing-detail',
    templateUrl: 'dataset-processing.component.html',
    styleUrls: ['dataset-processing.component.css']
})

export class DatasetProcessingComponent extends EntityComponent<DatasetProcessing> {

    @ViewChild('inputDatasetsTable', { static: false }) inputDatasetsTable: TableComponent;
    @ViewChild('outputDatasetsTable', { static: false }) outputDatasetsTable: TableComponent;

    study: Study

    private inputDatasetsPromise: Promise<any>;
    private outputDatasetsPromise: Promise<any>;
    private inputDatasetsBrowserPaging: BrowserPaging<Dataset>;
    private outputDatasetsBrowserPaging: BrowserPaging<Dataset>;
    private inputDatasetsToDelete: Dataset[] = [];
    private inputDatasetsToCreate: Dataset[] = [];
    private outputDatasetsToDelete: Dataset[] = [];
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

        function dateRenderer(date: number) {
            if (date) {
                return new Date(date).toLocaleDateString();
            }
            return null;
        };

        this.inputDatasetsColumnDefs = [
            {headerName: "ID", field: "id", type: "reference", cellRenderer: function (params: any) {
                return checkNullValueReference(params.data.id);
            }},
            {headerName: "Name", field: "name", type: "string", cellRenderer: function (params: any) {
                return checkNullValue(params.data.name);
            }},
            {headerName: "Dataset type", field: "type", type: "string", cellRenderer: function (params: any) {
                return checkNullValueReference(params.data.type);
            }},
            {headerName: "Study", field: "study", type: "reference", cellRenderer: function (params: any) {
                return checkNullValueReference(params.data.study);
            }},
            {headerName: "Subject", field: "subject", type: "reference", cellRenderer: function (params: any) {
                return checkNullValueReference(params.data.subject);
            }},
            {headerName: "Creation date", field: "creationDate", type: "date", cellRenderer: function (params: any) {
                return dateRenderer(params.data.creationDate);
            }}
        ];

        if (this.mode != 'view' && this.keycloakService.isUserAdminOrExpert()) {
            this.inputDatasetsColumnDefs.push({ headerName: "", type: "button", awesome: "fa-trash", action: (item) => this.removeInputDataset(item) });
        }


        this.outputDatasetsColumnDefs = [
            {headerName: "ID", field: "id", type: "reference", cellRenderer: function (params: any) {
                return checkNullValueReference(params.data.id);
            }},
            {headerName: "Name", field: "name", type: "string", cellRenderer: function (params: any) {
                return checkNullValue(params.data.name);
            }},
            {headerName: "Dataset type", field: "type", type: "string", cellRenderer: function (params: any) {
                return checkNullValueReference(params.data.type);
            }},
            {headerName: "Study", field: "study", type: "reference", cellRenderer: function (params: any) {
                return checkNullValueReference(params.data.study);
            }},
            {headerName: "Subject", field: "subject", type: "reference", cellRenderer: function (params: any) {
                return checkNullValueReference(params.data.subject);
            }},
            {headerName: "Creation date", field: "creationDate", type: "date", cellRenderer: function (params: any) {
                return dateRenderer(params.data.creationDate);
            }}
        ];

        if (this.mode != 'view' && this.keycloakService.isUserAdminOrExpert()) {
            this.outputDatasetsColumnDefs.push({ headerName: "", type: "button", awesome: "fa-trash", action: (item) => this.removeOutputDataset(item) });
        }
    }

    removeInputDataset(item: Dataset) {
        const index: number = this.datasetProcessing.inputDatasets.indexOf(item);
        if (index !== -1) {
            this.datasetProcessing.inputDatasets.splice(index, 1);
        }
        this.inputDatasetsToDelete.push(item);
        this.inputDatasetsBrowserPaging.setItems(this.datasetProcessing.inputDatasets);
        this.inputDatasetsTable.refresh();
    }

    removeOutputDataset(item: Dataset) {
        const index: number = this.datasetProcessing.outputDatasets.indexOf(item);
        if (index !== -1) {
            this.datasetProcessing.outputDatasets.splice(index, 1);
        }
        this.outputDatasetsToDelete.push(item);
        this.outputDatasetsBrowserPaging.setItems(this.datasetProcessing.outputDatasets);
        this.outputDatasetsTable.refresh();
    }

    manageSaveEntity(): void {
        this.subscribtions.push(
            this.onSave.subscribe(response => {
                if (this.inputDatasetsToDelete) {
                    for (let inputDataset of this.inputDatasetsToDelete) {
                        this.datasetProcessingService.deleteInputDataset(response.id, inputDataset.id);
                    }
                }
                if (this.inputDatasetsToCreate) {
                    for (let inputDataset of this.inputDatasetsToCreate) {
                        this.datasetProcessingService.createInputDataset(response.id, inputDataset).subscribe();
                    }
                }
                if (this.outputDatasetsToDelete) {
                    for (let outputDataset of this.outputDatasetsToDelete) {
                        this.datasetProcessingService.deleteOutputDataset(response.id, outputDataset.id);
                    }
                }
            })
        );
       
    }
}