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
import { Component, inject } from '@angular/core';

import { Step } from '../../breadcrumbs/breadcrumbs.service';
import { DatasetProcessingPipe } from '../../datasets/dataset-processing/dataset-processing.pipe';
import { DatasetProcessing } from '../../datasets/shared/dataset-processing.model';
import { DatasetProcessingService } from '../../datasets/shared/dataset-processing.service';
import { DatasetType } from '../../datasets/shared/dataset-type.model';
import { ProcessedDatasetType } from '../../enum/processed-dataset-type.enum';
import { preventInitialChildAnimations, slideDown } from '../../shared/animations/animations';
import { AbstractClinicalContextComponent } from '../clinical-context/clinical-context.abstract.component';
import { ProcessedContextData } from '../shared/import.data-service';
import { ProcessedDatasetImportJob } from '../shared/processed-dataset-data.model';
import { TooltipComponent } from '../../shared/components/tooltip/tooltip.component';
import { SelectBoxComponent } from '../../shared/select/select.component';
import { FormsModule } from '@angular/forms';
import { NgIf } from '@angular/common';

@Component({
    selector: 'processed-dataset-clinical-context',
    templateUrl: 'processed-dataset-clinical-context.component.html',
    styleUrls: ['../clinical-context/clinical-context.component.css', '../shared/import.step.css', './processed-dataset-clinical-context.component.css'],
    animations: [slideDown, preventInitialChildAnimations],
    imports: [TooltipComponent, SelectBoxComponent, FormsModule, NgIf]
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
    public datasetProcessings: DatasetProcessing[] = [];
    public useStudyCard: boolean = false;
    private datasetProcessingService: DatasetProcessingService = inject(DatasetProcessingService);
    public datasetProcessingLabelPipe: DatasetProcessingPipe = inject(DatasetProcessingPipe);

    getNextUrl(): string {
        return '/imports/processed-dataset';
    }

    protected exitCondition(): boolean {
        return !this.importDataService.processedDatasetImportJob;
    }

    importData(timestamp: number): Promise<any> {
        const context = this.importDataService.contextData;
        const importJob = new ProcessedDatasetImportJob();
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
        const importStep: Step = this.breadcrumbsService.currentStep;
        const createDatasetProcessingRoute: string = '/dataset-processing/create';
        this.breadcrumbsService.addNextStepPrefilled('study', this.study, true);
        this.breadcrumbsService.addNextStepPrefilled('subject', this.subject, true);
        this.router.navigate([createDatasetProcessingRoute]).then(() => {
            this.subscriptions.push(
                importStep.waitFor(this.breadcrumbsService.currentStep, false).subscribe(entity => {
                    this.datasetProcessing = entity;
                    this.onContextChange();
                    this.importDataService.contextBackup(this.stepTs).datasetProcessing = entity;
                })
            );
        });
    }

    protected fillCreateAcqEqStep() {
        return;
    }

    protected fillCreateSubjectStep() {
        return;
    }

    protected fillCreateExaminationStep() {
        return;
    }

    protected reloadSavedData(): Promise<void> {
        if (this.importDataService.contextBackup(this.stepTs)) {
            this.reloading = true;
            const processedDatasetFilePath = this.importDataService.contextBackup(this.stepTs).processedDatasetFilePath;
            const datasetType = this.importDataService.contextBackup(this.stepTs).datasetType;
            const processedDatasetType = this.importDataService.contextBackup(this.stepTs).processedDatasetType;
            const processedDatasetName = this.importDataService.contextBackup(this.stepTs).processedDatasetName;
            const processedDatasetComment = this.importDataService.contextBackup(this.stepTs).processedDatasetComment;
            const datasetProcessing = this.importDataService.contextBackup(this.stepTs).datasetProcessing;
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
            const study = this.importDataService.contextBackup(this.stepTs).study;
            const subject = this.importDataService.contextBackup(this.stepTs).subject;

            this.study = study;
            return this.onSelectStudy().then(() => {
                if (subject) {
                    this.subject = subject;
                    if (datasetProcessing) {
                        this.datasetProcessing = datasetProcessing;
                    }
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
        const context = this.getContext();
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
