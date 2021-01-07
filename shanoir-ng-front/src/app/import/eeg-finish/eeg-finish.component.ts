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
import { Router } from '@angular/router';

import { BreadcrumbsService, Step } from '../../breadcrumbs/breadcrumbs.service';
import { MsgBoxService } from '../../shared/msg-box/msg-box.service';
import { SubjectService } from '../../subjects/shared/subject.service';
import { EegImportJob } from '../shared/eeg-data.model';
import { ImagesUrlUtil } from '../../shared/utils/images-url.util';
import { ContextData, ImportDataService } from '../shared/import.data-service';
import { ImportService } from '../shared/import.service';
import { EegDataset, EegDatasetDTO } from '../../datasets/dataset/eeg/dataset.eeg.model'

@Component({
    selector: 'eeg-finish-import',
    templateUrl: 'eeg-finish.component.html',
    styleUrls: ['eeg-finish.component.css', '../shared/import.step.css']
})
export class FinishEegImportComponent {

    private importJob: EegImportJob;
    private context: ContextData;
    public importing: boolean = false;
    public readonly ImagesUrlUtil = ImagesUrlUtil;
    private step: Step;

    constructor(
            private importService: ImportService,
            private subjectService: SubjectService,
            private msgService: MsgBoxService,
            private router: Router,
            private breadcrumbsService: BreadcrumbsService,
            private importDataService: ImportDataService) {
        
        // Initialize context    
        if (!importDataService.eegImportJob) {
            this.router.navigate(['imports'], {replaceUrl: true});
            return;
        }
        
        this.importJob = this.importDataService.eegImportJob;

        breadcrumbsService.nameStep('4. Finish');
        this.context = this.importDataService.contextData;
    }
    
    public startEegImportJob(): void {
        this.subjectService
            .updateSubjectStudyValues(this.context.subject.subjectStudy)
            .then(() => {
                let that = this;
                this.importing = true;
                this.importData()
                    .then((importJob: EegImportJob) => {
                        this.importDataService.reset();
                        this.importing = false;
                        setTimeout(function () {
                            that.msgService.log('info', 'The data has been successfully imported')
                        }, 0);
                        // go back to the first step of import
                        this.router.navigate(['/imports/eeg']);
                    }).catch(error => {
                        this.importing = false;
                        throw error;
                    });
            }).catch(error => {
                throw new Error('Could not save the subjectStudy object, the import job has been stopped. Cause : ' + error);
            });
    }

    private importData(): Promise<any> {
        let importJob = new EegImportJob();
        importJob.datasets = [];

        for (let dataset of this.importJob.datasets) {
            let datasetToSet = new EegDatasetDTO();
            datasetToSet.channels = dataset.channels;
            datasetToSet.name = dataset.name;
            datasetToSet.files = dataset.files;
            datasetToSet.events = dataset.events;
            datasetToSet.samplingFrequency = dataset.samplingFrequency;
            datasetToSet.channelCount = dataset.channelCount;
            datasetToSet.coordinatesSystem = this.context.coordinatesSystem;
            importJob.datasets.push(datasetToSet);
        }
        importJob.subjectId = this.context.subject.id;
        importJob.subjectName = this.context.subject.name;
        importJob.studyName = this.context.study.name;
        importJob.workFolder = this.importJob.workFolder;
        importJob.examinationId = this.context.examination.id;
        importJob.studyId = this.context.study.id;
        importJob.acquisitionEquipmentId = this.context.acquisitionEquipment.id;
        return this.importService.startEegImportJob(importJob);
    }
}