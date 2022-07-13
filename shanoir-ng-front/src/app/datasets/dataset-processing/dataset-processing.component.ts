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
import { Option } from '../../shared/select/select.component';
import { EntityComponent } from '../../shared/components/entity/entity.component.abstract';
import { DatasetProcessingType } from '../../enum/dataset-processing-type.enum';
import { Dataset } from '../shared/dataset.model';
import { DatasetService } from '../shared/dataset.service';
import { DatasetProcessing } from '../../datasets/shared/dataset-processing.model';
import { DatasetProcessingService } from '../shared/dataset-processing.service';
import { StudyService } from '../../studies/shared/study.service';
import { Study } from '../../studies/shared/study.model';
import { Subject } from '../../subjects/shared/subject.model';
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

    @ViewChild('inputDatasetsTable', {static: false}) inputDatasetsTable: TableComponent;
    @ViewChild('outputDatasetsTable', {static: false}) outputDatasetsTable: TableComponent;

    public datasetProcessingTypes: Option<DatasetProcessingType>[] = DatasetProcessingType.options;
    public study: Study;
    public subject: Subject;
    public studyOptions: Option<Study>[] = [];
    public inputDatasetOptions: Option<Dataset>[] = [];
    private inputDatasetsPromise: Promise<any>;
    private outputDatasetsPromise: Promise<any>;
    private inputDatasetsBrowserPaging: BrowserPaging<Dataset>;
    private outputDatasetsBrowserPaging: BrowserPaging<Dataset>;
    private inputDatasetsToRemove: Dataset[] = [];
    private inputDatasetsToAdd: Dataset[] = [];
    private outputDatasetsToRemove: Dataset[] = [];
    public inputDatasetsColumnDefs: any[];
    public outputDatasetsColumnDefs: any[];

    constructor(
            private route: ActivatedRoute,
            private studyService: StudyService,
            private datasetService: DatasetService,
            private datasetProcessingService: DatasetProcessingService
            ) {

        super(route, 'dataset-processing');
    }
    
    ngOnInit(): void {
        super.ngOnInit();
        this.createColumnDefs();
        this.outputDatasetsPromise = Promise.resolve().then(() => {
            this.outputDatasetsBrowserPaging = new BrowserPaging([], this.outputDatasetsColumnDefs);
        });
        this.inputDatasetsPromise = Promise.resolve().then(() => {
            this.inputDatasetsBrowserPaging = new BrowserPaging([], this.inputDatasetsColumnDefs);
        });
    }

    get datasetProcessing(): DatasetProcessing { return this.entity; }
    set datasetProcessing(datasetProcessing: DatasetProcessing) { this.entity = datasetProcessing; }

    getService(): EntityService<DatasetProcessing> {
        return this.datasetProcessingService;
    }


    initView(): Promise<void> {
        return this.datasetProcessingService.get(this.id).then((entity)=> {
            this.setDatasetProcessing(entity);
        })
    }

    initEdit(): Promise<void> {
        return this.datasetProcessingService.get(this.id).then((entity)=> {
            this.setDatasetProcessing(entity);
        });
    }

    initCreate(): Promise<void> {
        this.datasetProcessing = new DatasetProcessing();
        return Promise.resolve();
    }

    setDatasetProcessing(datasetProcessing: DatasetProcessing): Promise<void> {
        this.datasetProcessing = datasetProcessing;
        return this.studyService.get(this.datasetProcessing.studyId).then((study)=> {
            this.studyOptions = [new Option<Study>(study, study.name)];
            this.study = study;
            return this.datasetService.getByStudyId(this.datasetProcessing.studyId);
        }).then(datasets=> {
            for(let dataset of datasets) {
                this.inputDatasetOptions.push(new Option<Dataset>(dataset, dataset.name));
                this.subject = dataset.subject;
            }
        }).then(() => {
            datasetProcessing.outputDatasets.forEach((dataset, index) => {
                datasetProcessing.outputDatasets[index].subject = this.subject;
                datasetProcessing.outputDatasets[index].study = this.study;
            });
            datasetProcessing.inputDatasets.forEach((dataset, index) => {
                datasetProcessing.inputDatasets[index].subject = this.subject;
                datasetProcessing.inputDatasets[index].study = this.study;
            });
            this.outputDatasetsBrowserPaging.setItems(datasetProcessing.outputDatasets);
            this.outputDatasetsTable.refresh();
            this.inputDatasetsBrowserPaging.setItems(datasetProcessing.inputDatasets);
            this.inputDatasetsTable.refresh();
        })
    }

    private prefill() {
        if (this.breadcrumbsService.currentStep.isPrefilled('subject')) {
            let contextSubject = this.breadcrumbsService.currentStep.getPrefilledValue('subject');
            this.subject = contextSubject;
        }
        if (this.breadcrumbsService.currentStep.isPrefilled('study')) {
            let study = this.breadcrumbsService.currentStep.getPrefilledValue('study');
            this.studyOptions = [new Option<Study>(study, study.name)];
            this.study = study;
            this.datasetProcessing.studyId = this.study.id;
            this.datasetService.getByStudyIdAndSubjectId(this.study.id, this.subject.id).then(datasets=> {
                for(let dataset of datasets) {
                    this.inputDatasetOptions.push(new Option<Dataset>(dataset, dataset.name));
                }   
            })
        }
    }

    buildForm(): FormGroup {
        this.prefill();
        return this.formBuilder.group({
            'study': [this.study ? this.study.name : '', Validators.required],
            'processingType': [this.datasetProcessing.datasetProcessingType, Validators.required],
            'processingDate': [this.datasetProcessing.processingDate, Validators.required],
            'inputDatasetList': [this.datasetProcessing.inputDatasets],
            'outputDatasetList': [this.datasetProcessing.outputDatasets],
            'comment': [this.datasetProcessing.comment]
        });
    }

    public async hasEditRight(): Promise<boolean> {
        return false;
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
                return checkNullValue(params.data.id);
            }},
            {headerName: "Name", field: "name", type: "string", cellRenderer: function (params: any) {
                return checkNullValue(params.data.name);
            }},
            {headerName: "Dataset type", field: "type", type: "string", cellRenderer: function (params: any) {
                return checkNullValue(params.data.type);
            }},
            {headerName: "Study", field: "study", type: "reference", cellRenderer: function (params: any) {
                return checkNullValue(params.data.study.name);
            }},
            {headerName: "Subject", field: "subject", type: "reference", cellRenderer: function (params: any) {
                return checkNullValue(params.data.subject.name);
            }},
            {headerName: "Creation date", field: "creationDate", type: "date", cellRenderer: function (params: any) {
                return dateRenderer(params.data.creationDate);
            }}
        ];

        if (this.mode != 'view' && this.keycloakService.isUserAdminOrExpert()) {
            this.inputDatasetsColumnDefs.push({ headerName: "", type: "button", awesome: "fa-regular fa-trash-can", action: (item) => this.removeInputDataset(item) });
        }

        this.outputDatasetsColumnDefs = [
            {headerName: "ID", field: "id", type: "reference", cellRenderer: function (params: any) {
                return checkNullValue(params.data.id);
            }},
            {headerName: "Name", field: "name", type: "string", cellRenderer: function (params: any) {
                return checkNullValue(params.data.name);
            }},
            {headerName: "Dataset type", field: "type", type: "string", cellRenderer: function (params: any) {
                return checkNullValue(params.data.type);
            }},
            {headerName: "Study", field: "study", type: "reference", cellRenderer: function (params: any) {
                return checkNullValue(params.data.study.name);
            }},
            {headerName: "Subject", field: "subject", type: "reference", cellRenderer: function (params: any) {
                return checkNullValue(params.data.subject.name);
            }},
            {headerName: "Creation date", field: "creationDate", type: "date", cellRenderer: function (params: any) {
                return dateRenderer(params.data.creationDate);
            }}
        ];
        
        if (this.mode != 'view' && this.keycloakService.isUserAdminOrExpert()) {
            this.outputDatasetsColumnDefs.push({
                title: "Add", awesome: "fa-solid fa-plus", action: item => this.router.navigate(['dataset-processing/edit-input-datasets/' + this.id])
            });
        }

        if (this.mode != 'view' && this.keycloakService.isUserAdminOrExpert()) {
            this.outputDatasetsColumnDefs.push({ headerName: "", type: "button", awesome: "fa-regular fa-trash-can", action: (item) => this.removeOutputDataset(item) });
        }
    }

    public onAddInputDataset(selectedDataset: Dataset) {
        if (!selectedDataset) {
            return;
        }
        if (this.datasetProcessing.inputDatasets.filter(dataset => dataset.id == selectedDataset.id).length > 0){
            return;   
        }
        this.inputDatasetsToAdd.push(selectedDataset);
        this.datasetProcessing.inputDatasets.push(selectedDataset);
        this.inputDatasetsBrowserPaging.setItems(this.datasetProcessing.inputDatasets);
        this.inputDatasetsTable.refresh();
        this.form.markAsDirty();
        this.form.updateValueAndValidity();
    }

    removeInputDataset(item: Dataset) {
        const index: number = this.datasetProcessing.inputDatasets.indexOf(item);
        if (index !== -1) {
            this.datasetProcessing.inputDatasets.splice(index, 1);
        }
        this.inputDatasetsToRemove.push(item);
        this.inputDatasetsBrowserPaging.setItems(this.datasetProcessing.inputDatasets);
        this.inputDatasetsTable.refresh();
    }

    removeOutputDataset(item: Dataset) {
        const index: number = this.datasetProcessing.outputDatasets.indexOf(item);
        if (index !== -1) {
            this.datasetProcessing.outputDatasets.splice(index, 1);
        }
        this.outputDatasetsToRemove.push(item);
        this.outputDatasetsBrowserPaging.setItems(this.datasetProcessing.outputDatasets);
        this.outputDatasetsTable.refresh();
    }

}