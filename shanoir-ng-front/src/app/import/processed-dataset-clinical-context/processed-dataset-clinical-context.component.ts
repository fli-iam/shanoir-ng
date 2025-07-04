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

import { AcquisitionEquipment } from '../../acquisition-equipments/shared/acquisition-equipment.model';
import { Step } from '../../breadcrumbs/breadcrumbs.service';
import { Center } from '../../centers/shared/center.model';
import { DatasetProcessingPipe } from '../../datasets/dataset-processing/dataset-processing.pipe';
import { DatasetProcessing } from '../../datasets/shared/dataset-processing.model';
import { DatasetProcessingService } from '../../datasets/shared/dataset-processing.service';
import { DatasetType } from '../../datasets/shared/dataset-type.model';
import { ProcessedDatasetType } from '../../enum/processed-dataset-type.enum';
import { preventInitialChildAnimations, slideDown } from '../../shared/animations/animations';
import { ServiceLocator } from '../../utils/locator.service';
import { AbstractClinicalContextComponent } from '../clinical-context/clinical-context.abstract.component';
import { ProcessedContextData } from '../shared/import.data-service';
import { ProcessedDatasetImportJob } from '../shared/processed-dataset-data.model';

@Component({
    selector: 'processed-dataset-clinical-context',
    templateUrl: 'processed-dataset-clinical-context.component.html',
    styleUrls: ['../clinical-context/clinical-context.component.css', '../shared/import.step.css','./processed-dataset-clinical-context.component.css'],
    animations: [slideDown, preventInitialChildAnimations],
    standalone: false
})
export class ProcessedDatasetClinicalContextComponent extends AbstractClinicalContextComponent {

    DatasetType = DatasetType;
    ProcessedDatasetType = ProcessedDatasetType;
    public datasetType: DatasetType;
    public processedDatasetType: ProcessedDatasetType;
    private processedDatasetFilePath: string;
    public processedDatasetName: string;
    public processedDatasetComment: string;
    public datasetProcessing: DatasetProcessing;
    public useStudyCard: boolean = false;

    getNextUrl(): string {
        return '/imports/processed-dataset';
    }

    protected exitCondition(): boolean {
        return !this.importDataService.processedDatasetImportJob;
    }

    importData(timestamp: number): Promise<any> {
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
        importJob.timestamp = timestamp;
        return this.importService.startProcessedDatasetImportJob(importJob);
    }

    public postConstructor(): void {
        this.breadcrumbsService.nameStep('2. Context');
        if(this.importDataService.processedDatasetImportJob != null) {
            this.processedDatasetFilePath = this.importDataService.processedDatasetImportJob.processedDatasetFilePath;
        }
    }

    public openCreateDatasetProcessing() {
        let importStep: Step = this.breadcrumbsService.currentStep;
        let createDatasetProcessingRoute: string = '/dataset-processing/create';
        this.router.navigate([createDatasetProcessingRoute],   {state: {study: this.study, subject: this.subject}}).then(success => {
            this.subscriptions.push(
                importStep.waitFor(this.breadcrumbsService.currentStep, false).subscribe(entity => {
                    this.datasetProcessing = entity;
                    this.onContextChange();
                    this.importDataService.contextBackup(this.stepTs).datasetProcessing = entity;
                })
            );
        });
    }

    protected reloadSavedData(): Promise<void> {
        if (this.importDataService.contextBackup(this.stepTs)) {
            this.reloading = true;
            let processedDatasetFilePath = this.importDataService.contextBackup(this.stepTs).processedDatasetFilePath;
            let datasetType = this.importDataService.contextBackup(this.stepTs).datasetType;
            let processedDatasetType = this.importDataService.contextBackup(this.stepTs).processedDatasetType;
            let processedDatasetName = this.importDataService.contextBackup(this.stepTs).processedDatasetName;
            let processedDatasetComment = this.importDataService.contextBackup(this.stepTs).processedDatasetComment;
            let datasetProcessing = this.importDataService.contextBackup(this.stepTs).datasetProcessing;
            if (processedDatasetFilePath) {
                this.processedDatasetFilePath = processedDatasetFilePath;
            }
            if (datasetType) {
                this.datasetType = datasetType;
            }
            if (processedDatasetType) {
                this.processedDatasetType = processedDatasetType;
            }
            if (processedDatasetName) {
                this.processedDatasetName = processedDatasetName;
            }
            if (processedDatasetComment) {
                this.processedDatasetComment = processedDatasetComment;
            }
            let study = this.importDataService.contextBackup(this.stepTs).study;
            let subject = this.importDataService.contextBackup(this.stepTs).subject;

            this.study = study;
            return this.onSelectStudy().then(() => {
                if (subject) {
                    this.subject = subject;
                    return this.onSelectSubject().then(() => {
                        if (datasetProcessing) {
                            this.datasetProcessing = datasetProcessing;
                        }
                    });
                }
            });
        }
    }

    protected getContext(): ProcessedContextData {
        return new ProcessedContextData(this.study,
                            this.subject,
                            this.datasetType,
                            this.processedDatasetFilePath,
                            this.processedDatasetType,
                            this.processedDatasetName,
                            this.processedDatasetComment,
                            this.datasetProcessing);
    }

    get valid(): boolean {
        let context = this.getContext();
        return (
            context.study != null
            && context.subject != null
            && context.datasetType != null
			&& context.processedDatasetName != null && context.processedDatasetName != ""
            && context.processedDatasetFilePath != null
            && context.processedDatasetType != null
            && context.datasetProcessing != null
        );
    }

}
