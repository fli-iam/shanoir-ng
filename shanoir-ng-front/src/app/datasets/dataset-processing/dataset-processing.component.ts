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

import {Component, ViewChild} from '@angular/core';
import {UntypedFormGroup, Validators} from '@angular/forms';
import {ActivatedRoute} from '@angular/router';
import {Option} from '../../shared/select/select.component';
import {EntityComponent} from '../../shared/components/entity/entity.component.abstract';
import {DatasetProcessingType} from '../../enum/dataset-processing-type.enum';
import {Dataset} from '../shared/dataset.model';
import {DatasetProcessing} from '../shared/dataset-processing.model';
import {DatasetProcessingService} from '../shared/dataset-processing.service';
import {StudyService} from '../../studies/shared/study.service';
import {Study} from '../../studies/shared/study.model';
import {Subject} from '../../subjects/shared/subject.model';
import {EntityService} from '../../shared/components/entity/entity.abstract.service';
import {TableComponent} from '../../shared/components/table/table.component';
import {ColumnDefinition} from '../../shared/components/table/column.definition.type';
import {ExecutionMonitoringService} from 'src/app/vip/execution-monitorings/execution-monitoring.service';
import {ExecutionMonitoring} from 'src/app/vip/models/execution-monitoring.model';
import {ExecutionService} from "../../vip/execution/execution.service";
import * as AppUtils from "../../utils/app.utils";
import {formatDate} from "@angular/common";
import {Selection} from 'src/app/studies/study/tree.service';
import {DatasetService} from "../shared/dataset.service";
import {SubjectService} from "../../subjects/shared/subject.service";
import {dateDisplay} from "../../shared/localLanguage/localDate.abstract";

@Component({
    selector: 'dataset-processing-detail',
    templateUrl: 'dataset-processing.component.html',
    styleUrls: ['dataset-processing.component.css'],
    standalone: false
})

export class DatasetProcessingComponent extends EntityComponent<DatasetProcessing> {

    @ViewChild('inputDatasetsTable', {static: false}) inputDatasetsTable: TableComponent;

    public datasetProcessingTypes: Option<DatasetProcessingType>[] = DatasetProcessingType.options;
    public study: Study;
    public subject: Subject;
    public inputDatasetOptions: Option<Dataset>[] = [];
    public inputDatasetsColumnDefs: ColumnDefinition[];
    public isExecutionMonitoring: boolean = false;
    public executionMonitoring: ExecutionMonitoring;


    constructor(
            private route: ActivatedRoute,
            private studyService: StudyService,
            private subjectService: SubjectService,
            private datasetProcessingService: DatasetProcessingService,
            private datasetService: DatasetService,
            private executionMonitoringService: ExecutionMonitoringService,
            private vipClientService: ExecutionService) {
        super(route, 'dataset-processing');
        const state = this.router.getCurrentNavigation().extras?.state;

        if (state) {
            this.study = state['study'];
            this.subject = state['subject'];
        }
    }

    get datasetProcessing(): DatasetProcessing {return this.entity}
    set datasetProcessing(datasetProcessing: DatasetProcessing) {this.entity = datasetProcessing}

    getService(): EntityService<DatasetProcessing> {return this.datasetProcessingService}

    protected getTreeSelection: () => Selection = () => {
        return Selection.fromProcessing(this.datasetProcessing);
    }

    initView(): Promise<void> {
        // checking if the datasetProcessing is not execution monitoring
        this.executionMonitoringService.getExecutionMonitoring(this.datasetProcessing.id).subscribe(
            (executionMonitoring: ExecutionMonitoring) => {
                this.setExecutionMonitoring(executionMonitoring);
            }, (error) => {
                // 404 : if it's not found then it's not execution monitoring !
                this.resetExecutionMonitoring();
            }
        )
        this.fetchOneStudy(this.datasetProcessing.studyId).then(study => {this.study = study;});
        this.fetchOneSubject(this.getProcessingSubject()).then(subject => {this.subject = subject;});
        return Promise.resolve();
    }

    initEdit(): Promise<void> {return Promise.resolve();}

    initCreate(): Promise<void> {
        this.datasetProcessing = new DatasetProcessing();
        this.fetchDatasets().then();
        return Promise.resolve();
    }

    setExecutionMonitoring(executionMonitoring: ExecutionMonitoring){
        this.isExecutionMonitoring = true;
        this.executionMonitoring = executionMonitoring;
    }

    resetExecutionMonitoring(){
        this.isExecutionMonitoring = false;
    }

    async fetchDatasets(): Promise<void> {
        if (!this.study?.id || !this.subject?.id) return Promise.resolve();
        return this.datasetService.getByStudyIdAndSubjectId(this.study.id, this.subject.id).then(datasets => {
            if(Array.isArray(datasets)){
                for (let dataset of datasets) {this.inputDatasetOptions.push(new Option<Dataset>(dataset, dataset.name));}
            }
        });
    }


    async fetchOneStudy(studyId: number): Promise<Study> {
        return await this.studyService.get(studyId);
    }

    async fetchOneSubject(subjectId: number): Promise<Subject> {
        return await this.subjectService.get(subjectId);
    }


    buildForm(): UntypedFormGroup {
        if(this.study?.id){this.datasetProcessing.studyId = this.study?.id;}

        this.inputDatasetsColumnDefs = [
            {headerName: "ID", field: "id", type: "number"},
            {headerName: "Name", field: "name", route: (dataset : Dataset)=>{return `/dataset/details/${dataset.id}`}},
            {headerName: "Dataset type", field: "type"},
            {headerName: "Study", field: "study.name"},
            {headerName: "Subject", field: "subject.name"},
            {headerName: "Creation date", field: "creationDate", type: "date"}
        ];

        return this.formBuilder.group({
            'study': [this.study?.id],
            'subject': [this.subject],
            'processingType': [this.datasetProcessing.datasetProcessingType, Validators.required],
            'processingDate': [this.datasetProcessing.processingDate, Validators.required],
            'inputDatasetList': [[], [Validators.required, Validators.minLength(1)]],
            'comment': [this.datasetProcessing.comment]
        })
    }

    public async hasEditRight(): Promise<boolean> {return false;}

    public downloadStdout() {
        if(!this.executionMonitoring){return;}

        let filename = this.executionMonitoring.name + ".stdout.log";
        this.vipClientService.getStdout(this.executionMonitoring.identifier).toPromise().then(response => {
            this.downloadLogIntoBrowser(response, filename );
        });
    }

    public downloadStderr() {
        if(!this.executionMonitoring){return;}

        let filename = this.executionMonitoring.name + ".stderr.log";
        this.vipClientService.getStderr(this.executionMonitoring.identifier).toPromise().then(response => {
            this.downloadLogIntoBrowser(response, filename );
        });
    }

    private downloadLogIntoBrowser(response: string, filename: string){
        let blob = new Blob([response], {type: 'text/plain'});
        AppUtils.browserDownloadFile(blob, filename);
    }

    public formatDate(millis: number) : string {
        return millis ? formatDate(new Date(millis), 'dd/MM/YYYY HH:mm:ss', 'en-US') : "";
    }

    public getDuration(){
        let start = this.executionMonitoring?.startDate;
        let end = this.executionMonitoring?.endDate;
        if(!start || !end){return "";}

        let duration = end - start;
        if(duration <= 0){return "";}

        let milliseconds = Math.floor((duration % 1000));
        let seconds = Math.floor((duration / 1000) % 60);
        let minutes = Math.floor((duration / (1000 * 60)) % 60);
        let hours = Math.floor((duration / (1000 * 60 * 60)));

        return String(hours).padStart(2, "0") + ":" +
            String(minutes).padStart(2, "0") + ":" +
            String(seconds).padStart(2, "0") + "." +
            String(milliseconds).padStart(3, "0")
    }

    protected readonly dateDisplay = dateDisplay;

    private getProcessingSubject(): number {
        return this.datasetProcessing.inputDatasets
            ?.map(ds => ds?.subject?.id)
            .find(id => id != null); // catches both null and undefined
    }
}
