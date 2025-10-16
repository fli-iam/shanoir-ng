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

import {Component, ViewChild, OnInit} from '@angular/core';
import {UntypedFormGroup, Validators} from '@angular/forms';
import {ActivatedRoute} from '@angular/router';
import {formatDate} from "@angular/common";

import {ExecutionMonitoringService} from 'src/app/vip/execution-monitorings/execution-monitoring.service';
import {ExecutionMonitoring} from 'src/app/vip/models/execution-monitoring.model';
import { Selection } from 'src/app/studies/study/tree.service';

import {Option} from '../../shared/select/select.component';
import {EntityComponent} from '../../shared/components/entity/entity.component.abstract';
import {DatasetProcessingType} from '../../enum/dataset-processing-type.enum';
import {Dataset} from '../shared/dataset.model';
import {DatasetService} from '../shared/dataset.service';
import {DatasetProcessing} from '../../datasets/shared/dataset-processing.model';
import {DatasetProcessingService} from '../shared/dataset-processing.service';
import {StudyService} from '../../studies/shared/study.service';
import {Study} from '../../studies/shared/study.model';
import {Subject} from '../../subjects/shared/subject.model';
import {EntityService} from '../../shared/components/entity/entity.abstract.service';
import {TableComponent} from '../../shared/components/table/table.component';
import {ColumnDefinition} from '../../shared/components/table/column.definition.type';
import {ExecutionService} from "../../vip/execution/execution.service";
import * as AppUtils from "../../utils/app.utils";
import {dateDisplay} from "../../shared/localLanguage/localDate.abstract";

@Component({
    selector: 'dataset-processing-detail',
    templateUrl: 'dataset-processing.component.html',
    styleUrls: ['dataset-processing.component.css'],
    standalone: false
})

export class DatasetProcessingComponent extends EntityComponent<DatasetProcessing> implements OnInit {

    @ViewChild('inputDatasetsTable', {static: false}) inputDatasetsTable: TableComponent;
    @ViewChild('outputDatasetsTable', {static: false}) outputDatasetsTable: TableComponent;

    public datasetProcessingTypes: Option<DatasetProcessingType>[] = DatasetProcessingType.options;
    public study: Study;
    public subject: Subject;
    public studyOptions: Option<Study>[] = [];
    public subjectOptions: Option<Subject>[] = [];
    public inputDatasetOptions: Option<Dataset>[] = [];
    public outputDatasetOptions: Option<Dataset>[] = [];
    public inputDatasetsColumnDefs: ColumnDefinition[];
    public outputDatasetsColumnDefs: ColumnDefinition[];
    public isExecutionMonitoring: boolean = false;
    public executionMonitoring: ExecutionMonitoring;
    prefilledStudy: Study;
    prefilledSubject: Subject;


    constructor(
            private route: ActivatedRoute,
            private studyService: StudyService,
            private datasetService: DatasetService,
            private datasetProcessingService: DatasetProcessingService,
            private executionMonitoringService: ExecutionMonitoringService,
            private vipClientService: ExecutionService
            ) {

        super(route, 'dataset-processing');
    }

    ngOnInit(): void {
        this.createColumnDefs();
    }

    get datasetProcessing(): DatasetProcessing { return this.entity; }
    set datasetProcessing(datasetProcessing: DatasetProcessing) { this.entity = datasetProcessing; }

    getService(): EntityService<DatasetProcessing> {
        return this.datasetProcessingService;
    }

    protected getTreeSelection: () => Selection = () => {
        return Selection.fromProcessing(this.datasetProcessing);
    }

    initView(): Promise<void> {
        // checking if the datasetProcessing is not execution monitoring
        this.executionMonitoringService.getExecutionMonitoring(this.datasetProcessing.id).subscribe(
            (executionMonitoring: ExecutionMonitoring) => {
                this.setExecutionMonitoring(executionMonitoring);
            }, () => {
                // 404 : if it's not found then it's not execution monitoring !
                this.resetExecutionMonitoring();
            }
        )
        this.fetchOneStudy(this.datasetProcessing?.studyId).then(() => {
            this.study = this.studyOptions?.[0]?.value;
        });
        return Promise.resolve();
    }

    initEdit(): Promise<void> {
        this.fetchStudies().then(() => {
            this.study = this.studyOptions?.find(opt => opt.value.id == this.datasetProcessing.studyId)?.value;
            const subjectId = this.datasetProcessing.inputDatasets?.[0]?.subject?.id;
            this.fetchSubjects().then(() => {
                this.subject = this.subjectOptions?.find(opt => opt.value.id == subjectId)?.value;
            }).then(() => {
                return this.fetchDatasets();
            });
        });
        return Promise.resolve();
    }

    initCreate(): Promise<void> {
        this.datasetProcessing = new DatasetProcessing();
        return Promise.all([
            this.breadcrumbsService.currentStep.getPrefilledValue('study').then(res => this.prefilledStudy = res),
            this.breadcrumbsService.currentStep.getPrefilledValue('subject').then(res => this.prefilledSubject = res)
        ])
        .then(([study, subject]) => {
            this.prefilledStudy = study;
            this.prefilledSubject = subject;

            this.studyOptions = [new Option(this.prefilledStudy, this.prefilledStudy.name)];
            this.study = this.prefilledStudy;
            this.datasetProcessing.studyId = this.study?.id;

            this.subjectOptions = [new Option(this.prefilledSubject, this.prefilledSubject.name)];
            this.subject = this.prefilledSubject;
        })
        .then(() => this.fetchDatasets());
    }

    onStudyChange() {
        this.datasetProcessing.studyId = this.study?.id;
        this.subjectOptions = [];
        this.subject = null;
        if (this.study) {
            this.fetchSubjects();
        }
    }

    onSubjectChange() {
        this.inputDatasetOptions = [];
        this.outputDatasetOptions = [];
        if (this.datasetProcessing.inputDatasets?.length > 0 || this.datasetProcessing.outputDatasets?.length > 0) {
            this.confirmDialogService.confirm('Change Subject', 'Are you sure you want to change the subject for this processing ? Every dataset input and output will be removed from the current lists.')
            .then(response => {
                if (response) {
                    this.datasetProcessing.inputDatasets = [];
                    this.datasetProcessing.outputDatasets = [];
                    this.fetchDatasets();
                }
            });
        } else {
            this.fetchDatasets();
        }
    }

    setExecutionMonitoring(executionMonitoring: ExecutionMonitoring){
        this.isExecutionMonitoring = true;
        this.executionMonitoring = executionMonitoring;
    }

    resetExecutionMonitoring(){
        this.isExecutionMonitoring = false;
    }

    fetchStudies(): Promise<void> {
        return this.studyService.getAll().then(studies => {
            this.studyOptions = studies?.map(study => new Option<Study>(study, study.name));
        });
    }

    fetchOneStudy(studyId: number): Promise<void> {
        return this.studyService.get(studyId).then(study=> {
            this.studyOptions = [new Option<Study>(study, study.name)];
        });
    }

    fetchSubjects(): Promise<void> {
        if (!this.study?.id) return Promise.resolve();
        return this.studyService.findSubjectsByStudyId(this.study.id).then(subjects => {
            this.subjectOptions = subjects?.map(sub => {
                const subject: Subject = new Subject();
                subject.id = sub.id;
                subject.name = sub.name;
                subject.identifier = sub.identifier;
                return new Option<Subject>(subject, sub.name);
            });
        }).then();
    }

    fetchDatasets(): Promise<void> {
        if (!this.study?.id || !this.subject?.id) return Promise.resolve();
        return this.datasetService.getByStudyIdAndSubjectId(this.study.id, this.subject.id).then(datasets => {
            for (const dataset of datasets) {
                this.inputDatasetOptions.push(new Option<Dataset>(dataset, dataset.name));
                this.outputDatasetOptions.push(new Option<Dataset>(dataset, dataset.name));
            }
        });
    }

    buildForm(): UntypedFormGroup {
        const formGroup: UntypedFormGroup = this.formBuilder.group({
            'study': [{value: this.study?.id, disabled: !!this.prefilledStudy}, Validators.required],
            'subject': [{value: this.subject, disabled: (!!this.prefilledSubject || !this.study)}, Validators.required],
            'processingType': [this.datasetProcessing.datasetProcessingType, Validators.required],
            'processingDate': [this.datasetProcessing.processingDate, Validators.required],
            'inputDatasetList': [{value: this.datasetProcessing.inputDatasets, disabled: !this.subject}, [Validators.required, Validators.minLength(1)]],
            'outputDatasetList': [{value: this.datasetProcessing.outputDatasets, disabled: !this.subject}],
            'comment': [this.datasetProcessing.comment]
        });
        this.subscriptions.push(
            formGroup.get('study').valueChanges.subscribe(studyVal => {
                if (!!this.prefilledSubject || !studyVal) formGroup.get('subject').disable();
                else formGroup.get('subject').enable();
            }), formGroup.get('subject').valueChanges.subscribe(subjectVal => {
                if (!subjectVal) {
                    formGroup.get('inputDatasetList').disable();
                    formGroup.get('outputDatasetList').disable();
                } else {
                    formGroup.get('inputDatasetList').enable();
                    formGroup.get('outputDatasetList').enable();
                }
            })
        );
        return formGroup;
    }

    public async hasEditRight(): Promise<boolean> {
        return false;
    }

    private createColumnDefs() {
        this.inputDatasetsColumnDefs = [
            {headerName: "ID", field: "id", type: "number"},
            {headerName: "Name", field: "name", route: (dataset : Dataset)=>{
                return `/dataset/details/${dataset.id}`
            }},
            {headerName: "Dataset type", field: "type"},
            {headerName: "Study", field: "study.name"},
            {headerName: "Subject", field: "subject.name"},
            {headerName: "Creation date", field: "creationDate", type: "date"}
        ];
        this.outputDatasetsColumnDefs = [...this.inputDatasetsColumnDefs];
    }

    public downloadStdout() {

        if(!this.executionMonitoring){
            return;
        }

        const filename = this.executionMonitoring.name + ".stdout.log";

        this.vipClientService.getStdout(this.executionMonitoring.identifier).toPromise().then(response => {
            this.downloadLogIntoBrowser(response, filename );
        });
    }

    public downloadStderr() {

        if(!this.executionMonitoring){
            return;
        }

        const filename = this.executionMonitoring.name + ".stderr.log";

        this.vipClientService.getStderr(this.executionMonitoring.identifier).toPromise().then(response => {
            this.downloadLogIntoBrowser(response, filename );
        });
    }

    private downloadLogIntoBrowser(response: string, filename: string){
        const blob = new Blob([response], {
            type: 'text/plain'
        });
        AppUtils.browserDownloadFile(blob, filename);
    }

    public formatDate(millis: number) : string {
        return millis ? formatDate(new Date(millis), 'dd/MM/YYYY HH:mm:ss', 'en-US') : "";
    }

    public getDuration(){

        const start = this.executionMonitoring?.startDate;
        const end = this.executionMonitoring?.endDate;

        if(!start || !end){
            return "";
        }

        const duration = end - start;

        if(duration <= 0){
            return "";
        }

        const milliseconds = Math.floor((duration % 1000));
        const seconds = Math.floor((duration / 1000) % 60);
        const minutes = Math.floor((duration / (1000 * 60)) % 60);
        const hours = Math.floor((duration / (1000 * 60 * 60)));

        return String(hours).padStart(2, "0") + ":" +
            String(minutes).padStart(2, "0") + ":" +
            String(seconds).padStart(2, "0") + "." +
            String(milliseconds).padStart(3, "0")
    }

    protected readonly dateDisplay = dateDisplay;
}
