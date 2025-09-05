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
import { Component, OnInit, ViewChild } from '@angular/core';

import { AcquisitionEquipment } from '../../acquisition-equipments/shared/acquisition-equipment.model';
import { Step } from '../../breadcrumbs/breadcrumbs.service';
import { Event } from '../../datasets/dataset/eeg/dataset.eeg.model';
import { EegDatasetDTO } from '../../datasets/shared/dataset.dto';
import { CoordSystems } from '../../enum/coord-system.enum';
import { Examination } from '../../examinations/shared/examination.model';
import { preventInitialChildAnimations, slideDown } from '../../shared/animations/animations';
import { BrowserPaging } from '../../shared/components/table/browser-paging.model';
import { FilterablePageable, Page } from '../../shared/components/table/pageable.model';
import { TableComponent } from '../../shared/components/table/table.component';
import { ColumnDefinition } from '../../shared/components/table/column.definition.type';
import { IdName } from '../../shared/models/id-name.model';
import { Option } from '../../shared/select/select.component';
import { ImagedObjectCategory } from '../../subjects/shared/imaged-object-category.enum';
import { Subject } from '../../subjects/shared/subject.model';
import { AbstractClinicalContextComponent } from '../clinical-context/clinical-context.abstract.component';
import { EegImportJob } from '../shared/eeg-data.model';
import { EegContextData } from '../shared/import.data-service';
import {UnitOfMeasure} from "../../enum/unitofmeasure.enum";


@Component({
    selector: 'eeg-clinical-context',
    templateUrl: 'eeg-clinical-context.component.html',
    styleUrls: ['../clinical-context/clinical-context.component.css', '../shared/import.step.css'],
    animations: [slideDown, preventInitialChildAnimations],
    standalone: false
})

export class EegClinicalContextComponent extends AbstractClinicalContextComponent implements OnInit {

    @ViewChild('eventsTable', { static: false }) table: TableComponent;

    columnDefs: ColumnDefinition[];
    hasPosition: boolean;
    coordSystemOptions: Option<CoordSystems>[];
    coordsystem : string;
    firstDate: Date;
    useStudyCard: boolean = false;

    private browserPaging: BrowserPaging<EventContext>;

    postConstructor() {
        this.coordSystemOptions = CoordSystems.options;
        // Check for position to know if we have to display systemCoord or not
        this.hasPosition = false;
        for (let dataset of this.importDataService.eegImportJob.datasets) {
            if (dataset.coordinatesSystem == "true") {
                this.hasPosition = true;
            }
        }
        this.modality = 'EEG';
        this.findEegDate();
    }

    protected exitCondition(): boolean {
        return !this.importDataService?.eegImportJob?.datasets;
    }

    ngOnInit(): void {
        super.ngOnInit();
        this.initEventsTable();
    }

    private initEventsTable(): void {
        if (!this.importDataService.eegImportJob) return;
        this.columnDefs = [
            {headerName: "Dataset name", field: "name", type: "string", cellRenderer: function (params: any) {
                    return params.data.dataset_name;
            }},
           {headerName: "Description", field: "description", type: "string", cellRenderer: function (params: any) {
                    return params.data.description;
            }},
            {headerName: "Number", field: "number", type: "number", cellRenderer: function (params: any) {
                    return params.data.number;
            }},
        ]
        this.browserPaging = new BrowserPaging([], this.columnDefs);
        this.browserPaging.setItems(this.getEventContexts());
    }

    private getEventContexts(): EventContext[] {
        let context = [];
        let contextDict = {};
        for (let dataset of this.importDataService.eegImportJob.datasets) {
            if (!contextDict[dataset.name]) {
                contextDict[dataset.name] = {};
            }
            for (let event of dataset.events) {
                if (contextDict[dataset.name][event.description]) {
                    // Update the context value
                    contextDict[dataset.name][event.description].number += 1;
                } else {
                    // Create the context value
                    let cont: EventContext = new EventContext();
                    cont.number = 1;
                    cont.description = event.description;
                    cont.dataset_name = dataset.name;
                    contextDict[dataset.name][event.description] = cont;
                    context.push(cont);
                }
            }
        }
        return context;
    }

    public getPage(pageable: FilterablePageable): Promise<Page<EventContext>> {
        return Promise.resolve(this.browserPaging.getPage(pageable));
    }

    public onSelectCoord(): void {
    }

    protected getContext(): EegContextData {
        return new EegContextData(this.study, null, this.useStudyCard, this.center, this.acquisitionEquipment,
            this.subject, this.examination, this.coordsystem, null, null, null, null, null, null);
    }

    get valid(): boolean {
        let context = this.getContext();
        return (
            !!context.study
            && !!context.center
            && !!context.acquisitionEquipment
            && !!context.subject
            && !!context.examination
            && (!!context.coordinatesSystem || !this.hasPosition)
        );
    }

    public getNextUrl(): string {
        return '/imports/eeg';
    }

    public importData(timestamp: number): Promise<any> {
        let importJob = new EegImportJob();
        importJob.datasets = [];
        let context = this.importDataService.contextData as EegContextData;
        let importJobContext = this.importDataService.eegImportJob;

        for (let dataset of importJobContext.datasets) {
            let datasetToSet = new EegDatasetDTO();
            datasetToSet.channels = dataset.channels;
            datasetToSet.name = dataset.name;
            datasetToSet.files = dataset.files;
            datasetToSet.events = dataset.events;
            datasetToSet.samplingFrequency = dataset.samplingFrequency;
            datasetToSet.channelCount = dataset.channelCount;
            datasetToSet.coordinatesSystem = context.coordinatesSystem;
            importJob.datasets.push(datasetToSet);
        }
        importJob.subjectId = context.subject.id;
        importJob.subjectName = context.subject.name;
        importJob.studyName = context.study.name;
        importJob.workFolder = importJobContext.workFolder;
        importJob.examinationId = context.examination.id;
        importJob.studyId = context.study.id;
        importJob.acquisitionEquipmentId = context.acquisitionEquipment.id;
        importJob.timestamp = timestamp;
        return this.importService.startEegImportJob(importJob);
    }

    protected fillCreateSubjectStep(step: Step) {
        this.breadcrumbsService.currentStep.addPrefilled("entity", this.getPrefilledSubject());
        this.breadcrumbsService.currentStep.addPrefilled("forceStudy", this.study);
        this.breadcrumbsService.currentStep.addPrefilled("subjectNamePrefix", this.subjectNamePrefix);

    }

    protected getPrefilledSubject(): Subject {
        let newSubject = new Subject();
        newSubject.imagedObjectCategory = ImagedObjectCategory.LIVING_HUMAN_BEING;
        newSubject.study = this.study;
        newSubject.physicallyInvolved = false;
        return newSubject;
    }

    protected fillCreateExaminationStep(step: Step) {
        this.breadcrumbsService.currentStep.addPrefilled("entity", this.getPrefilledExam());
    }

    private getPrefilledExam(): Examination {
        let newExam = new Examination();
        newExam.preclinical = true;
        newExam.hasStudyCenterData = true;
        newExam.study = new IdName(this.study.id, this.study.name);
        if (this.center) {
            newExam.center = new IdName(this.center.id, this.center.name);
        }
        newExam.subject = new Subject();
        newExam.subject.id = this.subject.id;
        newExam.subject.name = this.subject.name;
        newExam.examinationDate = this.firstDate;
        newExam.weightUnitOfMeasure = UnitOfMeasure.KG;
        return newExam;
    }

    protected fillCreateAcqEqStep(step: Step) {
        this.breadcrumbsService.currentStep.addPrefilled("entity", this.getPrefilledAcqEqt());
    }

    private getPrefilledAcqEqt(): AcquisitionEquipment {
        let acqEpt = new AcquisitionEquipment();
        acqEpt.center = this.center;
        return acqEpt;
    }

    private findEegDate() {
        this.importDataService.eegImportJob?.datasets?.find(eegds => {
            let event: Event = eegds.events?.find(event => !!event.date);
            if (event) {
                this.firstDate = new Date(event.date);
                return true;
            } else return false;
        });
    }
}

export class EventContext {
    public description: string;
    public number: number;
    public dataset_name: string;
}
