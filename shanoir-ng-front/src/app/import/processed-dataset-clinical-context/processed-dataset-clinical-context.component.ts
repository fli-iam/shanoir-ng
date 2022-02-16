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
import { Component, OnDestroy } from '@angular/core';
import { Router } from '@angular/router';
import { Subscription } from 'rxjs';

import { BreadcrumbsService, Step } from '../../breadcrumbs/breadcrumbs.service';
import { preventInitialChildAnimations, slideDown } from '../../shared/animations/animations';
import { KeycloakService } from '../../shared/keycloak/keycloak.service';
import { Option } from '../../shared/select/select.component';
import { StudyRightsService } from '../../studies/shared/study-rights.service';
import { StudyUserRight } from '../../studies/shared/study-user-right.enum';
import { Study } from '../../studies/shared/study.model';
import { StudyService } from '../../studies/shared/study.service';
import { Subject } from '../../subjects/shared/subject.model';
import { SubjectService } from '../../subjects/shared/subject.service';
import { SubjectWithSubjectStudy } from '../../subjects/shared/subject.with.subject-study.model';
import { ContextData, ImportDataService } from '../shared/import.data-service';
import { DatasetProcessing } from '../../datasets/shared/dataset-processing.model'
import { DatasetProcessingService } from '../../datasets/shared/dataset-processing.service';
import { ProcessedDatasetType } from '../../enum/processed-dataset-type.enum';
import { DatasetType } from '../../datasets/shared/dataset-type.model';
import { DatasetProcessingPipe } from '../../datasets/dataset-processing/dataset-processing.pipe';
import { ImportMode } from '../../import/import.component';
import { SimpleSubject } from '../../subjects/shared/subject.model';
import { ProcessedDatasetImportJob } from '../shared/processed-dataset-data.model';
import { ImportService } from '../shared/import.service';
import { MsgBoxService } from '../../shared/msg-box/msg-box.service';

@Component({
    selector: 'processed-dataset-clinical-context',
    templateUrl: 'processed-dataset-clinical-context.component.html',
    styleUrls: ['processed-dataset-clinical-context.component.css', '../shared/import.step.css'],
    animations: [slideDown, preventInitialChildAnimations]
})
export class ProcessedDatasetClinicalContextComponent implements OnDestroy {
    DatasetType = DatasetType;
    ProcessedDatasetType = ProcessedDatasetType;
    public datasetType: DatasetType;
    public processedDatasetType: ProcessedDatasetType;
    private processedDatasetFilePath: string;
    public processedDatasetName: string;
    public processedDatasetComment: string;
    public studyOptions: Option<Study>[] = [];
    public subjects: SubjectWithSubjectStudy[] = [];
    public study: Study;
    public subject: SubjectWithSubjectStudy;
    public datasetProcessing: DatasetProcessing;
    public datasetProcessings: DatasetProcessing[] = [];
    
    private subscribtions: Subscription[] = [];
    public subjectTypes: Option<string>[] = [
        new Option<string>('HEALTHY_VOLUNTEER', 'Healthy Volunteer'),
        new Option<string>('PATIENT', 'Patient'),
        new Option<string>('PHANTOM', 'Phantom')
    ];
    public importMode: ImportMode;

    openSubjectStudy: boolean = false;
    
    constructor(
            public studyService: StudyService,
            public subjectService: SubjectService,
            private datasetProcessingService: DatasetProcessingService,
            public datasetProcessingLabelPipe: DatasetProcessingPipe,
            private router: Router,
            private breadcrumbsService: BreadcrumbsService,
            private importDataService: ImportDataService,
            public studyRightsService: StudyRightsService,
            private keycloakService: KeycloakService,
            private msgService: MsgBoxService,
            private importService: ImportService
            ) {

        breadcrumbsService.nameStep('2. Context'); 
        this.importMode = this.breadcrumbsService.findImportMode();
        if(importDataService.processedDatasetImportJob != null) {
            this.processedDatasetFilePath = importDataService.processedDatasetImportJob.processedDatasetFilePath;
        }
        this.getStudiesAndDatasetProcessings();
    }

    public openCreateDatasetProcessing() {
        let importStep: Step = this.breadcrumbsService.currentStep;
        let createDatasetProcessingRoute: string = '/dataset-processing/create';
        this.router.navigate([createDatasetProcessingRoute]).then(success => {
	        this.breadcrumbsService.currentStep.addPrefilled('study', this.study);
            this.breadcrumbsService.currentStep.addPrefilled('subject', this.subject);
            this.subscribtions.push(
                importStep.waitFor(this.breadcrumbsService.currentStep, false).subscribe(entity => {
                    this.reloadSavedData();
                    this.datasetProcessing = entity;
                    this.importDataService.contextBackup.datasetProcessing = entity;
                     this.onContextChange();
                })
            );
        });
    }

    private reloadSavedData() {
        if (this.importDataService.contextBackup) {
            let processedDatasetFilePath = this.importDataService.contextBackup.processedDatasetFilePath;
            let datasetType = this.importDataService.contextBackup.datasetType;
            let processedDatasetType = this.importDataService.contextBackup.processedDatasetType;
            let processedDatasetName = this.importDataService.contextBackup.processedDatasetName
            let processedDatasetComment = this.importDataService.contextBackup.processedDatasetComment;
            let datasetProcessing = this.importDataService.contextBackup.datasetProcessing;
            if(processedDatasetFilePath) {
                this.processedDatasetFilePath = processedDatasetFilePath;
            }
            if(datasetType) {
                this.datasetType = datasetType;
            }
            if(processedDatasetType) {
                this.processedDatasetType = processedDatasetType;
            }
            if(processedDatasetName) {
                this.processedDatasetName = processedDatasetName;
            }
            if(processedDatasetComment) {
                this.processedDatasetComment = processedDatasetComment;
            }
            let study = this.importDataService.contextBackup.study;
            let subject = this.importDataService.contextBackup.subject;
            if (study) {
                this.study = study;
                let studyOption = this.studyOptions.find(s => s.value.id == study.id);
                if (studyOption) {
                    this.study = studyOption.value;
                }
                this.onSelectStudy();
                if (subject) {
                    this.subject = subject;
                    this.onSelectSubject();
                    if(datasetProcessing) {
                        this.datasetProcessing = datasetProcessing;
                    }
                }
            }
            this.onContextChange();
        }
    }

    private getStudiesAndDatasetProcessings(): void {
        
        Promise.all([this.studyService.getStudyNamesAndCenters()])
        .then(([allStudies]) => {
            this.studyOptions = [];
            for (let study of allStudies) {
                let studyOption: Option<Study> = new Option(study, study.name);
                if (study.studyCenterList) {
                    this.studyOptions.push(studyOption);
                    // update the selected study as well
                    if (this.study && this.study.id == study.id) {
                        this.study.studyCenterList = study.studyCenterList; 
                    }
                }
            }
            this.reloadSavedData();
        });
    }

    public onSelectStudy(): void {
        this.subjects = [];
        this.subject = null;
        if(this.study != null && this.study.id != null) {
            this.studyService.findSubjectsByStudyId(this.study.id)
                .then(subjects => this.subjects = subjects);
        }
        this.onContextChange();
    }

    public onSelectSubject(): void {
        if (this.subject && !this.subject.subjectStudy) this.subject = null;
        this.datasetProcessing = null;
        if (!this.subject) {
            this.openSubjectStudy = false;
            return;
        }
        this.loadProcessings();
        this.onContextChange();
    }

    loadProcessings(): void {
	    this.datasetProcessingService.findAllByStudyIdAndSubjectId(this.study.id, this.subject.id).then(
		    processings => this.datasetProcessings = processings
	    );
    }

    public onContextChange() {
        this.importDataService.contextBackup = this.getContext();
        if (this.valid) {
            this.importDataService.contextData = this.getContext();
        }
    }

    private getContext(): ContextData {
        return new ContextData(this.study, null, null, null, null,
                            this.subject, null, null, null,
                            this.datasetType,  
                            this.processedDatasetFilePath, 
                            this.processedDatasetType, 
                            this.processedDatasetName, 
                            this.processedDatasetComment, 
                            this.datasetProcessing);
    }

    public openCreateSubject() {
        let importStep: Step = this.breadcrumbsService.currentStep;
        let createSubjectRoute: string = '/subject/create';
        this.router.navigate([createSubjectRoute]).then(success => {
            this.subscribtions.push(
                importStep.waitFor(this.breadcrumbsService.currentStep, false).subscribe(entity => {
                    this.importDataService.contextBackup.subject = this.subjectToSubjectWithSubjectStudy(entity as Subject);
                })
            );
        });
    }
    
    public subjectToSubjectWithSubjectStudy(subject: Subject): SubjectWithSubjectStudy {
        if (!subject) return;
        
        let subjectWithSubjectStudy = new SubjectWithSubjectStudy();
        subjectWithSubjectStudy.id = subject.id;
        subjectWithSubjectStudy.name = subject.name;
        subjectWithSubjectStudy.identifier = subject.identifier;
        subjectWithSubjectStudy.subjectStudy = subject.subjectStudyList[0];
        return subjectWithSubjectStudy;
    }

    public showDatasetProcessingDetails() {
        this.router.navigate(['dataset-processing/details/' + this.datasetProcessing.id]);
    }

    public showStudyDetails() {
        this.router.navigate(['study/details/' + this.study.id]);
    }

    public showSubjectDetails() {
        this.router.navigate(['subject/details/' + this.subject.id]);
    }

    get valid(): boolean {
        let context = this.getContext();
        return (
            context.study != null
            && context.subject != null
            && context.datasetType != null
			&& context.processedDatasetName != null &&  context.processedDatasetName != ""
            && context.processedDatasetFilePath != null
            && context.processedDatasetType != null
            && context.datasetProcessing != null
        );
    }

    public next() {
        this.startImportJob();
    }

    public startImportJob(): void {
        let context = this.importDataService.contextData;
        this.subjectService
            .updateSubjectStudyValues(context.subject.subjectStudy)
            .then(() => {
                let that = this;
                this.importData()
                    .then((importJob: ProcessedDatasetImportJob) => {
                        this.importDataService.reset();
                        setTimeout(function () {
                            that.msgService.log('info', 'The import successfully started')
                        }, 0);
                        // go back to the first step of import
                        this.router.navigate(['/imports/processed-dataset']);
                    }).catch(error => {
                        // Clean context
                        this.importDataService.reset();
                        throw error;
                    });
            }).catch(error => {
                throw new Error('Could not save the subjectStudy object, the import job has been stopped. Cause : ' + error);
            });
    }

    private importData(): Promise<any> {
        let context = this.importDataService.contextData;
        let importJob = new ProcessedDatasetImportJob();
        importJob.subjectId = context.subject.id;
        importJob.subjectName = context.subject.name;
        importJob.studyName = context.study.name;
        importJob.studyId = context.study.id;
        importJob.datasetType = context.datasetType;
        importJob.processedDatasetFilePath = context.processedDatasetFilePath;
        importJob.processedDatasetType = context.processedDatasetType;
        importJob.processedDatasetName = context.processedDatasetName;
        importJob.processedDatasetComment = context.processedDatasetComment;
        importJob.datasetProcessing = context.datasetProcessing;
        return this.importService.startProcessedDatasetImportJob(importJob);
    }

    protected hasAdminRightOn(study: Study): Promise<boolean> {
        if (!study) return Promise.resolve(false);
        else if (this.keycloakService.isUserAdmin()) return Promise.resolve(true);
        else if (!this.keycloakService.isUserExpert()) return Promise.resolve(false);
        else return this.studyRightsService.getMyRightsForStudy(study.id).then(rights => {
            return rights && rights.includes(StudyUserRight.CAN_ADMINISTRATE);
        });
    }

    ngOnDestroy() {
        for(let subscribtion of this.subscribtions) {
            subscribtion.unsubscribe();
        }
    }
}