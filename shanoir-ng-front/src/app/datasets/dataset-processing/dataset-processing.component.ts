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

import { formatDate } from "@angular/common";
import { Component, OnInit } from '@angular/core';
import { UntypedFormGroup, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { distinctUntilChanged, takeUntil } from 'rxjs';

import { Selection } from 'src/app/studies/study/tree.service';
import { ExecutionMonitoringService } from 'src/app/vip/execution-monitorings/execution-monitoring.service';
import { ExecutionMonitoring } from 'src/app/vip/models/execution-monitoring.model';

import { DatasetProcessing } from '../../datasets/shared/dataset-processing.model';
import { DatasetProcessingType } from '../../enum/dataset-processing-type.enum';
import { EntityService } from '../../shared/components/entity/entity.abstract.service';
import { EntityComponent } from '../../shared/components/entity/entity.component.abstract';
import { ColumnDefinition } from '../../shared/components/table/column.definition.type';
import { dateDisplay } from "../../shared/localLanguage/localDate.abstract";
import { Option } from '../../shared/select/select.component';
import { Study } from '../../studies/shared/study.model';
import { StudyService } from '../../studies/shared/study.service';
import { Subject } from '../../subjects/shared/subject.model';
import * as AppUtils from "../../utils/app.utils";
import { ExecutionService } from "../../vip/execution/execution.service";
import { DatasetProcessingService } from '../shared/dataset-processing.service';
import { Dataset } from '../shared/dataset.model';
import { DatasetService } from '../shared/dataset.service';

@Component({
    selector: 'dataset-processing-detail',
    templateUrl: 'dataset-processing.component.html',
    styleUrls: ['dataset-processing.component.css'],
    standalone: false
})

export class DatasetProcessingComponent extends EntityComponent<DatasetProcessing> implements OnInit {

    public datasetProcessingTypes: Option<DatasetProcessingType>[] = DatasetProcessingType.options;
    public _subject: Subject;
    public studyOptions: Option<number>[] = [];
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

    get subject(): Subject { return this._subject; }
    
    set subject(subject: Subject) { 
        this._subject = subject;
        this.form.get('subject').setValue(subject);
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
        this.studyService.get(this.datasetProcessing.studyId, "lazy").then(study => {
            this.studyOptions = [new Option<number>(study.id, study.name)];
        });
        // checking if the datasetProcessing is not execution monitoring
        this.subscriptions.push(
            this.executionMonitoringService.getExecutionMonitoring(this.datasetProcessing.id).subscribe(
                (executionMonitoring: ExecutionMonitoring) => {
                    this.setExecutionMonitoring(executionMonitoring);
                }, () => {
                    // 404 : if it's not found then it's not execution monitoring !
                    this.resetExecutionMonitoring();
                }
            )
        );
        return Promise.resolve();
    }

    initEdit(): Promise<void> {
        this.fetchStudies().then(() => {
            const firstDatasetId: number = this.datasetProcessing.inputDatasets?.[0]?.id;
            if (firstDatasetId) {
                Promise.all([
                    this.datasetService.get(this.datasetProcessing.inputDatasets?.[0]?.id),
                    this.fetchSubjects()
                ]).then(([dataset]) => {
                    if (dataset) {
                        const subjectId = dataset.subject?.id;
                        this.fetchDatasets();
                        this.subject = this.subjectOptions?.find(opt => opt.value.id == subjectId)?.value;
                    }   
                });

            }
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
            this.study = study;
            this.subject = subject;
            this.datasetProcessing.studyId = this.study?.id;

            this.studyOptions = [new Option(study, study.name)];
            this.subjectOptions = [new Option(subject, subject.name)];
        })
        .then(() => this.fetchDatasets());
    }

    onStudyChange(newValue: number) {
        this.subjectOptions = [];
        if (newValue) {
            this.fetchSubjects();
        }
        this.subject = null;
        this.onSubjectChange(null);
    }

    onSubjectChange(newValue: Subject) {
        this.subject = newValue;
        this.datasetProcessing.inputDatasets = [];
        this.datasetProcessing.outputDatasets = [];
        this.inputDatasetOptions = [];
        this.outputDatasetOptions = [];
        if (newValue) {
            this.fetchDatasets();
        }
    }

    askForSubjectChangeConfirmation(): Promise<boolean> {
        if (this.datasetProcessing.inputDatasets?.length > 0 || this.datasetProcessing.outputDatasets?.length > 0) {
            return this.confirmDialogService.confirm('Change Subject', 'Are you sure you want to change the subject for this processing ? Every dataset input and output will be removed from the current lists.');
        } else {
            return Promise.resolve(true);
        }
    }

    askForStudyChangeConfirmation(): Promise<boolean> {
        if (this.datasetProcessing.inputDatasets?.length > 0 || this.datasetProcessing.outputDatasets?.length > 0) {
            return this.confirmDialogService.confirm('Change Study', 'Are you sure you want to change the study for this processing ? Every dataset input and output will be removed from the current lists.');
        } else {
            return Promise.resolve(true);
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
            this.studyOptions = studies?.map(study => new Option<number>(study.id, study.name));
        });
    }

    fetchOneStudy(studyId: number): Promise<void> {
        return this.studyService.get(studyId).then(study=> {
            this.studyOptions = [new Option<number>(study.id, study.name)];
        });
    }

    get studyName(): string {
        const studyOption = this.studyOptions?.find(opt => opt.value == this.datasetProcessing?.studyId);
        return studyOption ? studyOption.label : '';
    }

    fetchSubjects(): Promise<void> {
        if (!this.datasetProcessing?.studyId) return Promise.resolve();
        return this.studyService.findSubjectsByStudyId(this.datasetProcessing.studyId).then(subjects => {
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
        if (!this.datasetProcessing?.studyId || !this.subject?.id) return Promise.resolve();
        return this.datasetService.getByStudyIdAndSubjectId(this.datasetProcessing.studyId, this.subject.id).then(datasets => {
            for (const dataset of datasets) {
                this.inputDatasetOptions.push(new Option<Dataset>(dataset, dataset.name));
                this.outputDatasetOptions.push(new Option<Dataset>(dataset, dataset.name));
            }
        });
    }

    buildForm(): UntypedFormGroup {
        const formGroup: UntypedFormGroup = this.formBuilder.group({
            'studyId': [{value: this.datasetProcessing?.studyId , disabled: !!this.prefilledStudy}, Validators.required],
            'subject': [{value: this.subject, disabled: (!!this.prefilledSubject || !this.datasetProcessing?.studyId)}, Validators.required],
            'datasetProcessingType': [this.datasetProcessing.datasetProcessingType, Validators.required],
            'processingDate': [this.datasetProcessing.processingDate, Validators.required],
            'inputDatasets': [{value: this.datasetProcessing.inputDatasets, disabled: !this.subject}, [Validators.required, Validators.minLength(1)]],
            'outputDatasets': [{value: this.datasetProcessing.outputDatasets, disabled: !this.subject}],
            'comment': [this.datasetProcessing.comment]
        });
        formGroup.get('studyId').valueChanges
            .pipe(
                distinctUntilChanged(),
                takeUntil(this.destroy$)
            ).subscribe(studyVal => {
                if (!!this.prefilledSubject || !studyVal) {
                    formGroup.get('subject').disable();
                } else {
                    formGroup.get('subject').enable();
                }
            });
        formGroup.get('subject').valueChanges
            .pipe(
                distinctUntilChanged(),
                takeUntil(this.destroy$)
            ).subscribe(subjectVal => {
                if (!subjectVal) {
                    formGroup.get('inputDatasets').disable();
                    formGroup.get('outputDatasets').disable();
                } else {
                    formGroup.get('inputDatasets').enable();
                    formGroup.get('outputDatasets').enable();
                }
            })
        return formGroup;
    }

    protected prefillProperties(): void {
        super.prefillProperties();
        this.breadcrumbsService.currentStep.getPrefilledValue('study').then(res => this.prefilledStudy = res);
        this.breadcrumbsService.currentStep.getPrefilledValue('subject').then(res => this.prefilledSubject = res);
        if (this.prefilledStudy) {
            this.studyOptions = [new Option(this.prefilledStudy.id, this.prefilledStudy.name)];
            this.datasetProcessing.studyId = this.prefilledStudy.id;
        } else {
            this.fetchStudies();
        }
        if (this.prefilledSubject) {
            this.subjectOptions = [new Option(this.prefilledSubject, this.prefilledSubject.name)];
            this.subject = this.prefilledSubject;
            this.fetchDatasets();
        }
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
