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
import { FormsModule } from '@angular/forms';

import { AcquisitionEquipment } from '../../acquisition-equipments/shared/acquisition-equipment.model';
import { Event } from '../../datasets/dataset/eeg/dataset.eeg.model';
import { EegDatasetDTO } from '../../datasets/shared/dataset.dto';
import { CoordSystems } from '../../enum/coord-system.enum';
import { UnitOfMeasure } from "../../enum/unitofmeasure.enum";
import { Examination } from '../../examinations/shared/examination.model';
import { preventInitialChildAnimations, slideDown } from '../../shared/animations/animations';
import { BrowserPaging } from '../../shared/components/table/browser-paging.model';
import { ColumnDefinition } from '../../shared/components/table/column.definition.type';
import { FilterablePageable, Page } from '../../shared/components/table/pageable.model';
import { TableComponent } from '../../shared/components/table/table.component';
import { IdName } from '../../shared/models/id-name.model';
import { Option, SelectBoxComponent } from '../../shared/select/select.component';
import { ImagedObjectCategory } from '../../subjects/shared/imaged-object-category.enum';
import { SubjectStudy } from '../../subjects/shared/subject-study.model';
import { Subject } from '../../subjects/shared/subject.model';
import { AbstractClinicalContextComponent } from '../clinical-context/clinical-context.abstract.component';
import { EegImportJob } from '../shared/eeg-data.model';
import { EegContextData } from '../shared/import.data-service';
import { TooltipComponent } from '../../shared/components/tooltip/tooltip.component';


@Component({
    selector: 'eeg-clinical-context',
    templateUrl: 'eeg-clinical-context.component.html',
    styleUrls: ['../clinical-context/clinical-context.component.css', '../shared/import.step.css'],
    animations: [slideDown, preventInitialChildAnimations],
    imports: [TooltipComponent, SelectBoxComponent, FormsModule, TableComponent]
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
        for (const dataset of this.importDataService.eegImportJob.datasets) {
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
        const context = [];
        const contextDict = {};
        for (const dataset of this.importDataService.eegImportJob.datasets) {
            if (!contextDict[dataset.name]) {
                contextDict[dataset.name] = {};
            }
            for (const event of dataset.events) {
                if (contextDict[dataset.name][event.description]) {
                    // Update the context value
                    contextDict[dataset.name][event.description].number += 1;
                } else {
                    // Create the context value
                    const cont: EventContext = new EventContext();
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

    protected getContext(): EegContextData {
        return new EegContextData(this.study, null, this.useStudyCard, this.center, this.acquisitionEquipment,
            this.subject, this.examination, this.coordsystem, null, null, null, null, null, null);
    }

    get valid(): boolean {
        const context = this.getContext();
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
        const importJob = new EegImportJob();
        importJob.datasets = [];
        const context = this.importDataService.contextData as EegContextData;
        const importJobContext = this.importDataService.eegImportJob;

        for (const dataset of importJobContext.datasets) {
            const datasetToSet = new EegDatasetDTO();
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

    protected fillCreateSubjectStep() {
        this.breadcrumbsService.currentStep.addPrefilled("entity", this.getPrefilledSubject());
        this.breadcrumbsService.currentStep.addPrefilled("forceStudy", this.study);
        this.breadcrumbsService.currentStep.addPrefilled("subjectNamePrefix", this.subjectNamePrefix);

    }

    protected getPrefilledSubject(): Subject {
        const subjectStudy = new SubjectStudy();
        subjectStudy.study = this.study;
        subjectStudy.physicallyInvolved = false;
        const newSubject = new Subject();
        newSubject.subjectStudyList = [];
        newSubject.imagedObjectCategory = ImagedObjectCategory.LIVING_HUMAN_BEING;
        newSubject.study = this.study;
        return newSubject;
    }

    protected getPrefilledExamination(): Examination {
        const newExam = new Examination();
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

    protected getPrefilledAcquisitionEquipment(): AcquisitionEquipment {
        const acqEpt = new AcquisitionEquipment();
        acqEpt.center = this.center;
        return acqEpt;
    }

    private findEegDate() {
        this.importDataService.eegImportJob?.datasets?.find(eegds => {
            const event: Event = eegds.events?.find(event => !!event.date);
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
