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
import {Component, ElementRef, EventEmitter, OnDestroy, Output, ViewChild} from '@angular/core';
import { UntypedFormGroup, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';

import { TaskState } from 'src/app/async-tasks/task.model';
import { EntityService } from 'src/app/shared/components/entity/entity.abstract.service';
import { MassDownloadService } from 'src/app/shared/mass-download/mass-download.service';
import { Selection } from 'src/app/studies/study/tree.service';
import { environment } from '../../../environments/environment';
import { BreadcrumbsService } from '../../breadcrumbs/breadcrumbs.service';
import { CenterService } from '../../centers/shared/center.service';
import { UnitOfMeasure } from "../../enum/unitofmeasure.enum";
import { EntityComponent } from '../../shared/components/entity/entity.component.abstract';
import { DatepickerComponent } from '../../shared/date-picker/date-picker.component';
import { IdName } from '../../shared/models/id-name.model';
import { ImagesUrlUtil } from '../../shared/utils/images-url.util';
import { StudyRightsService } from '../../studies/shared/study-rights.service';
import { StudyUserRight } from '../../studies/shared/study-user-right.enum';
import { StudyService } from '../../studies/shared/study.service';
import { Examination } from '../shared/examination.model';
import { ExaminationService } from '../shared/examination.service';
import {dateDisplay} from "../../shared/./localLanguage/localDate.abstract";
import {Subject} from "../../subjects/shared/subject.model";

@Component({
    selector: 'examination-detail',
    templateUrl: 'examination.component.html',
    standalone: false
})

export class ExaminationComponent extends EntityComponent<Examination> implements OnDestroy {

    @ViewChild('input') private fileInput: ElementRef;

    public centers: IdName[];
    public studies: IdName[];
    public subjects: Subject[];
    files: File[] = [];
    public inImport: boolean;
    public readonly ImagesUrlUtil = ImagesUrlUtil;
    protected bidsLoading: boolean = false;
    hasAdministrateRight: boolean = false;
    hasImportRight: boolean = false;
    hasDownloadRight: boolean = false;
    pattern: string = '[^:|<>&\/]+';
    downloadState: TaskState = new TaskState();
    dateDisplay = dateDisplay;
    datasetIds: Promise<number[]> = new Promise((resolve, reject) => {});
    datasetIdsLoaded: boolean = false;
    noDatasets: boolean = false;
	hasEEG: boolean = false;
	hasDicom: boolean = false;
    hasBids: boolean = false;
    unit = UnitOfMeasure;
    defaultUnit = this.unit.KG;

    constructor(
            private route: ActivatedRoute,
            private examinationService: ExaminationService,
            private centerService: CenterService,
            private studyService: StudyService,
            private studyRightsService: StudyRightsService,
            public breadcrumbsService: BreadcrumbsService,
            private downloadService: MassDownloadService) {

        super(route, 'examination');
        this.inImport = this.breadcrumbsService.isImporting();
    }

    public setFile() {
        this.fileInput.nativeElement.click();
    }

    set examination(examination: Examination) {
        this.entity = examination;
    }
    get examination(): Examination { return this.entity; }

    getService(): EntityService<Examination> {
        return this.examinationService;
    }

    protected getTreeSelection: () => Selection = () => {
        return Selection.fromExamination(this.examination);
    }

    set entity(exam: Examination) {
        super.entity = exam;
        this.getSubjects();
    }

    get entity(): Examination {
        return super.entity;
    }

    init() {
        super.init();
        if (this.mode == 'create') {
            this.breadcrumbsService.currentStep.getPrefilledValue("entity").then( res => this.examination = res);
        }
    }

    initView(): Promise<void> {
        if(!this.examination.weightUnitOfMeasure){
            this.examination.weightUnitOfMeasure = this.defaultUnit;
        }
        if (this.keycloakService.isUserAdmin()) {
            this.hasAdministrateRight = true;
            this.hasDownloadRight = true;
            this.hasImportRight = true;
            return Promise.resolve();
        } else {
            return this.studyRightsService.getMyRightsForStudy(this.examination.study.id).then(rights => {
                this.hasImportRight = rights.includes(StudyUserRight.CAN_IMPORT);
                this.hasAdministrateRight = rights.includes(StudyUserRight.CAN_ADMINISTRATE);
                this.hasDownloadRight = rights.includes(StudyUserRight.CAN_DOWNLOAD);
            });
        }
    }

    initEdit(): Promise<void> {
        this.getCenters();
        this.getStudies();

        if(!this.examination.weightUnitOfMeasure){
            this.examination.weightUnitOfMeasure = this.defaultUnit;
        }
        this.getSubjects();
        return Promise.resolve();
    }

    initCreate(): Promise<void> {
        this.getCenters();
        this.getStudies();
        this.examination = new Examination();
        this.examination.weightUnitOfMeasure = this.defaultUnit;
        this.breadcrumbsService.currentStep.addPrefilled("entity", this.examination);

        return Promise.resolve();
    }

    buildForm(): UntypedFormGroup {
        return this.formBuilder.group({
            'study': [{value: this.examination.study, disabled: this.inImport}, Validators.required],
            'subject': [{value: this.examination.subject, disabled: this.inImport}, Validators.required],
            'center': [{value: this.examination.center, disabled: this.inImport}, Validators.required],
            'examinationDate': [this.examination.examinationDate, [Validators.required, DatepickerComponent.validator]],
            'comment': [this.examination.comment, Validators.pattern(this.pattern)],
            'note': [this.examination.note],
            'subjectWeight': [this.examination.subjectWeight],
            'weightUnitOfMeasure': [this.examination.weightUnitOfMeasure]
        });
    }

    downloadAll() {
        this.downloadService.downloadAllByExaminationId(this.examination?.id,this.downloadState);
    }

    openViewer() {
	    window.open(environment.viewerUrl + '/viewer?StudyInstanceUIDs=1.4.9.12.34.1.8527.' + this.entity.id, '_blank');
    }

    openSegmentationViewer() {
        window.open(environment.viewerUrl + '/segmentation?StudyInstanceUIDs=1.4.9.12.34.1.8527.' + this.entity.id, '_blank');
    }

    getCenters(): void {
        this.centerService
            .getCentersNames()
            .then(centers => {
                this.centers = centers;
            });
    }

    getStudies(): void {
        this.studyService
            .getStudiesNames()
            .then(studies => {
                this.studies = studies;
            });
    }

    getSubjects(): void {
        if (!this.examination || !this.examination.study) return;
        this.studyService
            .findSubjectsByStudyId(this.examination.study.id)
            .then(subjects => this.subjects = subjects);
    }

    getSubjectLink(){
        if(this.examination.preclinical){
            return '/preclinical-subject/details/'+ this.examination.subject?.id;
        } else {
            return '/subject/details/'+ this.examination.subject?.id;
        }
    }

    onStudyChange() {
        this.getSubjects();
    }

    public async hasEditRight(): Promise<boolean> {
	   return this.keycloakService.isUserAdmin() || this.hasImportRight;
    }

    public async hasDeleteRight(): Promise<boolean> {
         return this.keycloakService.isUserAdmin() || this.hasAdministrateRight;
    }

    public isAdmin(): boolean {
         return this.keycloakService.isUserAdmin();
    }

    public deleteFile(file: any) {
        this.examination.extraDataFilePathList = this.examination.extraDataFilePathList.filter(fileToKeep => fileToKeep != file);
        this.files = this.files.filter(fileToKeep => fileToKeep.name != file);
        this.form.markAsDirty();
        this.form.updateValueAndValidity();
    }

    public attachNewFile(event: any) {
        let newFile = event.target.files[0];
        this.examination.extraDataFilePathList.push(newFile.name);
        this.files.push(newFile);
        this.form.markAsDirty();
        this.form.updateValueAndValidity();
    }

    public save(): Promise<Examination> {
        return super.save( () => {
            let uploads: Promise<void>[] = [];
            // Once the exam is saved, save associated files
            for (let file of this.files) {
                uploads.push(this.examinationService.postFile(file, this.entity.id));
            }
            return Promise.all(uploads).then(() => null);
        }).then(() => null).catch(reason => { if (reason.status == 403) {
            this.consoleService.log('error', 'Examination ' + this.examination.id + ' Updating study / subject / center of an examination is forbidden.');
            return null;
        } else {
            throw reason;
        }});
    }

    getFileName(element): string {
        return element.split('\\').pop().split('/').pop();
    }

    getUnit(key: string) {
        return UnitOfMeasure.getLabelByKey(key);
    }

    ngOnDestroy() {
        this.breadcrumbsService.currentStep.addPrefilled("entity", this.examination);

        for (let subscribtion of this.subscriptions) {
            subscribtion.unsubscribe();
        }
    }

    downloadFile(file) {
        this.examinationService.downloadFile(file, this.examination.id, this.downloadState);
    }
}
