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
import { Router } from '@angular/router';

import { AcquisitionEquipment } from '../../acquisition-equipments/shared/acquisition-equipment.model';
import { BreadcrumbsService, Step } from '../../breadcrumbs/breadcrumbs.service';
import { Center } from '../../centers/shared/center.model';
import { CenterService } from '../../centers/shared/center.service';
import { CoordSystems } from '../../enum/coord-system.enum';
import { Examination } from '../../examinations/shared/examination.model';
import { ExaminationService } from '../../examinations/shared/examination.service';
import { SubjectExamination } from '../../examinations/shared/subject-examination.model';
import { SubjectExaminationPipe } from '../../examinations/shared/subject-examination.pipe';
import { slideDown } from '../../shared/animations/animations';
import { BrowserPaging } from '../../shared/components/table/browser-paging.model';
import { FilterablePageable, Page } from '../../shared/components/table/pageable.model';
import { TableComponent } from '../../shared/components/table/table.component';
import { IdName } from '../../shared/models/id-name.model';
import { StudyCenter } from '../../studies/shared/study-center.model';
import { Study } from '../../studies/shared/study.model';
import { StudyService } from '../../studies/shared/study.service';
import { ImagedObjectCategory } from '../../subjects/shared/imaged-object-category.enum';
import { SubjectStudy } from '../../subjects/shared/subject-study.model';
import { Subject } from '../../subjects/shared/subject.model';
import { SubjectWithSubjectStudy } from '../../subjects/shared/subject.with.subject-study.model';
import { ContextData, ImportDataService } from '../shared/import.data-service';
import { Option } from '../../shared/select/select.component';
import { AcquisitionEquipmentPipe } from '../../acquisition-equipments/shared/acquisition-equipment.pipe';


@Component({
    selector: 'eeg-clinical-context',
    templateUrl: 'eeg-clinical-context.component.html',
    styleUrls: ['eeg-clinical-context.component.css', '../shared/import.step.css'],
    animations: [slideDown]
})

export class EegClinicalContextComponent implements OnInit {
    
    @ViewChild('eventsTable', { static: false }) table: TableComponent;
    
    public studies: Study[] = [];
    public centers: Center[] = [];
    public acquisitionEquipments: AcquisitionEquipment[] = [];
    public examinations: SubjectExamination[] = [];
    public study: Study;
    public center: Center;
    public acquisitionEquipment: AcquisitionEquipment;
    public examination: SubjectExamination;
    public subjects: SubjectWithSubjectStudy[] = [];
    public subject: SubjectWithSubjectStudy;
    public columnDefs: any[];
    public hasPosition: boolean;
    openSubjectStudy: boolean = false;
    
    public coordSystemOptions: Option<CoordSystems>[];
    public coordsystem : string;

    private browserPaging: BrowserPaging<EventContext>;
    private eventsPromise: Promise<any>;

    public subjectTypes: Option<string>[] = [
        new Option<string>('HEALTHY_VOLUNTEER', 'Healthy Volunteer'),
        new Option<string>('PATIENT', 'Patient'),
        new Option<string>('PHANTOM', 'Phantom')
    ];
    
    constructor(
            private studyService: StudyService,
            private centerService: CenterService,
            private examinationService: ExaminationService,
            private router: Router,
            private breadcrumbsService: BreadcrumbsService,
            private importDataService: ImportDataService,
            public acqEqPipe: AcquisitionEquipmentPipe,
            public subjectExaminationPipe: SubjectExaminationPipe) {

        this.coordSystemOptions = CoordSystems.options;

        // No channels => no import
        if (!this.importDataService?.eegImportJob?.datasets) {
            this.router.navigate(['imports'], {replaceUrl: true});
            return;
        }
        breadcrumbsService.nameStep('3. Context');
        
        // Check for position to know if we have to display systemCoord or not
        this.hasPosition = false;
        for (let dataset of this.importDataService.eegImportJob.datasets) {
            if (dataset.coordinatesSystem == "true") {
                this.hasPosition = true;
            }
        }
        
        // Initialize studies;
        Promise.all([this.studyService.getStudyNamesAndCenters(), this.centerService.getAll()])
        .then(([allStudies, allCenters]) => {
            this.studies = allStudies;
            
            for (let study of allStudies) {
                for (let studyCenter of study.studyCenterList) {
                    let center = allCenters.filter(center => center.id === studyCenter.center.id)[0];
                    if (center) {
                        studyCenter.center = center;
                    }
                }
            }
            
            this.reloadSavedData();
            this.onContextChange();
        });
    }
    
    ngOnInit(): void {
        // Init events table
        this.initEventsTable();
    }

    private reloadSavedData() {
        if (this.importDataService.contextBackup) {
            let study = this.importDataService.contextBackup.study;
            let center = this.importDataService.contextBackup.center;
            let acquisitionEquipment = this.importDataService.contextBackup.acquisitionEquipment;
            let subject = this.importDataService.contextBackup.subject;
            let examination = this.importDataService.contextBackup.examination;
            if (study) {
                this.study = study;
                this.onSelectStudy();
            }
            if (center) {
                this.center = center;
                this.onSelectCenter();
            }
            if (acquisitionEquipment) {
                // reload acquisition equipments if we just added one acquisitionEquipment
                if (this.acquisitionEquipments.indexOf(acquisitionEquipment) == -1) {
                    this.acquisitionEquipments.push(acquisitionEquipment);
                }
                this.acquisitionEquipment = acquisitionEquipment;
                this.onSelectAcquisitonEquipment();
            }
            if (subject) {
                this.subject = subject;
                this.onSelectSubject();
            }
            if (examination) {
                this.examination = examination;
                this.onSelectExam();
            }
        }
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

       this.eventsPromise = Promise.resolve().then(() => {
            this.browserPaging = new BrowserPaging([], this.columnDefs);
            this.browserPaging.setItems(context);
            this.table.refresh();
        });
    }

   public getPage(pageable: FilterablePageable): Promise<Page<EventContext>> {
        return new Promise((resolve) => {
            this.eventsPromise.then(() => {
                resolve(this.browserPaging.getPage(pageable));
            });
        });
    }

    public onSelectStudy(): void {
        this.centers = this.acquisitionEquipments = this.subjects = this.examinations = [];
        this.openSubjectStudy = false;
        this.center = this.acquisitionEquipment = this.subject = this.examination = null;
        if (this.study && this.study.id && this.study.studyCenterList) {
            this.center = this.study.studyCenterList[0].center;
            this.onSelectCenter();
            for (let studyCenter of this.study.studyCenterList) {
                this.centers.push(studyCenter.center);
            }
        }
    }

    public onSelectCenter(): void {
        this.acquisitionEquipments = this.subjects = this.examinations = [];
        this.openSubjectStudy = false;
        this.acquisitionEquipment = this.subject = this.examination = null;
        if (this.center && this.center.acquisitionEquipments) {
            this.acquisitionEquipment = this.center.acquisitionEquipments[0];
            this.onSelectAcquisitonEquipment();
            this.acquisitionEquipments = this.center.acquisitionEquipments;
        }
    }

    public onSelectAcquisitonEquipment(): void {
        this.subjects = this.examinations = [];
        this.openSubjectStudy = false;
        this.subject = this.examination = null;
        if (this.acquisitionEquipment) {
            this.studyService
                .findSubjectsByStudyId(this.study.id)
                .then(subjects => this.subjects = subjects);
        }
    }

    public onSelectSubject(): void {
        this.examinations = [];
        this.examination = null;
        if (this.subject) {
            this.examinationService
            .findExaminationsBySubjectAndStudy(this.subject.id, this.study.id)
            .then(examinations => this.examinations = examinations);
        }
    }

    public onSelectExam(): void {
    }

    public onSelectCoord(): void {
    }

    public onContextChange() {
        this.importDataService.contextBackup = this.getContext();
        if (this.valid) {
            this.importDataService.contextData = this.getContext();
        }
    }
    
    private getContext(): ContextData {
        return new ContextData(this.study, null, false, this.center, this.acquisitionEquipment,
            this.subject, this.examination, null, this.coordsystem);
    }

    private getPrefilledCenter(): Center {
        let studyCenter = new StudyCenter();
        studyCenter.study = this.study;
        let newCenter = new Center();
        newCenter.studyCenterList = [studyCenter];
        return newCenter;
    }

    private updateStudyCenter(center: Center): Center {
        if (!center) return;
        let studyCenter: StudyCenter = center.studyCenterList[0];
        this.study.studyCenterList.push(studyCenter);
        return center;
    }

    public openCreateAcqEqt() {
        let currentStep: Step = this.breadcrumbsService.currentStep;
        this.router.navigate(['/acquisition-equipment/create']).then(success => {
            this.breadcrumbsService.currentStep.entity = this.getPrefilledAcqEqt();
            currentStep.waitFor(this.breadcrumbsService.currentStep, false).subscribe(entity => {
                this.importDataService.contextBackup.acquisitionEquipment = (entity as AcquisitionEquipment);
            });
        });
    }

    private getPrefilledAcqEqt(): AcquisitionEquipment {
        let acqEpt = new AcquisitionEquipment();
        acqEpt.center = this.center;
        return acqEpt;
    }

    public openCreateSubject = () => {
        let importStep: Step = this.breadcrumbsService.currentStep;
        this.router.navigate(['/subject/create']).then(success => {
            this.breadcrumbsService.currentStep.entity = this.getPrefilledSubject();
            importStep.waitFor(this.breadcrumbsService.currentStep, false).subscribe(entity => {
                this.importDataService.contextBackup.subject = this.subjectToSubjectWithSubjectStudy(entity as Subject);
            });
        });
    }

    private getPrefilledSubject(): Subject {
        let subjectStudy = new SubjectStudy();
        subjectStudy.study = this.study;
        subjectStudy.physicallyInvolved = false;
        let newSubject = new Subject();
        newSubject.imagedObjectCategory = ImagedObjectCategory.LIVING_HUMAN_BEING;
        newSubject.subjectStudyList = [subjectStudy];
        return newSubject;
    }

    private subjectToSubjectWithSubjectStudy(subject: Subject): SubjectWithSubjectStudy {
        if (!subject) return;
        let subjectWithSubjectStudy = new SubjectWithSubjectStudy();
        subjectWithSubjectStudy.id = subject.id;
        subjectWithSubjectStudy.name = subject.name;
        subjectWithSubjectStudy.identifier = subject.identifier;
        subjectWithSubjectStudy.subjectStudy = subject.subjectStudyList[0];
        return subjectWithSubjectStudy;
    }

    public openCreateExam = () => {
        let currentStep: Step = this.breadcrumbsService.currentStep;
        this.router.navigate(['/examination/create']).then(success => {
            this.breadcrumbsService.currentStep.entity = this.getPrefilledExam();
            currentStep.waitFor(this.breadcrumbsService.currentStep, false).subscribe(entity => {
                this.importDataService.contextBackup.examination = this.examToSubjectExam(entity as Examination);
            });
        });
    }

    private getPrefilledExam(): Examination {
        let newExam = new Examination();
        newExam.study = new IdName(this.study.id, this.study.name);
        newExam.center = new IdName(this.center.id, this.center.name);
        newExam.subjectStudy = this.subject;
        newExam.subject = new Subject();
        newExam.subject.id = this.subject.id;
        newExam.subject.name = this.subject.name;
        return newExam;
    }
    
    private examToSubjectExam(examination: Examination): SubjectExamination {
        if (!examination) return;
        // Add the new created exam to the select box and select it
        let subjectExam = new SubjectExamination();
        subjectExam.id = examination.id;
        subjectExam.examinationDate = examination.examinationDate;
        subjectExam.comment = examination.comment;
        return subjectExam;
    }

    public showStudyDetails() {
        window.open('study/details/' + this.study.id, '_blank');
    }

    public showCenterDetails() {
        window.open('center/details/' + this.center.id, '_blank');
    }

    public showAcquistionEquipmentDetails() {
        window.open('acquisition-equipment/details/' + this.acquisitionEquipment.id, '_blank');
    }

    public showSubjectDetails() {
        window.open('subject/details/' + this.subject.id, '_blank');
    }

    public showExaminationDetails() {
        window.open('examination/details/' + this.examination.id, '_blank');
    }

    get valid(): boolean {
        let context = this.getContext();
        return (
            context.study != undefined && context.study != null
            && context.center != undefined && context.center != null
            && context.acquisitionEquipment != undefined && context.acquisitionEquipment != null
            && context.subject != undefined && context.subject != null
            && context.examination != undefined && context.examination != null
            && context.subject.subjectStudy.subjectType
            && ((context.coordinatesSystem != undefined && context.coordinatesSystem != null && this.hasPosition) || !(this.hasPosition))
        );
    }

    public next() {
        this.router.navigate(['imports/eegfinish']);
    }
}

export class EventContext {
    public description: string;
    public number: number;
    public dataset_name: string;
}