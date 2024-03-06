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
import { Component, ElementRef, ViewChild } from '@angular/core';
import { UntypedFormGroup, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';

import { EntityService } from 'src/app/shared/components/entity/entity.abstract.service';
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
import { SubjectWithSubjectStudy } from '../../subjects/shared/subject.with.subject-study.model';
import { ExaminationNode } from '../../tree/tree.model';
import { Examination } from '../shared/examination.model';
import { ExaminationService } from '../shared/examination.service';
import { TaskState, TaskStatus } from 'src/app/async-tasks/task.model';
import { MassDownloadService } from 'src/app/shared/mass-download/mass-download.service';
import { Format } from 'src/app/datasets/shared/dataset.service';
import { DownloadSetupOptions } from 'src/app/shared/mass-download/download-setup/download-setup.component';

@Component({
    selector: 'examination',
    templateUrl: 'examination.component.html'
})

export class ExaminationComponent extends EntityComponent<Examination> {

    @ViewChild('input') private fileInput: ElementRef;

    public centers: IdName[];
    public studies: IdName[];
    public subjects: SubjectWithSubjectStudy[];
    examinationExecutives: Object[];
    files: File[] = [];
    public inImport: boolean;
    public readonly ImagesUrlUtil = ImagesUrlUtil;
    protected bidsLoading: boolean = false;
    hasAdministrateRight: boolean = false;
    hasImportRight: boolean = false;
    hasDownloadRight: boolean = false;
    pattern: string = '[^:|<>&\/]+';
    examNode: Examination | ExaminationNode;
    downloadState: TaskState = new TaskState();

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
        this.examNode = this.breadcrumbsService.currentStep.data.examinationNode ? this.breadcrumbsService.currentStep.data.examinationNode : examination;
    }
    get examination(): Examination { return this.entity; }

    getService(): EntityService<Examination> {
        return this.examinationService;
    }

    set entity(exam: Examination) {
        super.entity = exam;
        this.getSubjects();
    }

    get entity(): Examination {
        return super.entity;
    }

    initView(): Promise<void> {
        return this.examinationService.get(this.id).then((examination: Examination) => {
            this.examination = examination;
            if(!this.examination.weightUnitOfMeasure){
                this.examination.weightUnitOfMeasure = this.defaultUnit;
            }
            if (this.keycloakService.isUserAdmin()) {
                this.hasAdministrateRight = true;
                this.hasDownloadRight = true;
                this.hasImportRight = true;
                return;
            } else {
                return this.studyRightsService.getMyRightsForStudy(examination.study.id).then(rights => {
                    this.hasImportRight = rights.includes(StudyUserRight.CAN_IMPORT);
                    this.hasAdministrateRight = rights.includes(StudyUserRight.CAN_ADMINISTRATE);
                    this.hasDownloadRight = rights.includes(StudyUserRight.CAN_DOWNLOAD);
                });
            }
        });
    }

    initEdit(): Promise<void> {
        this.getCenters();
        this.getStudies();
        return this.examinationService.get(this.id).then((examination: Examination) => {
            this.examination = examination
            if(!this.examination.weightUnitOfMeasure){
                this.examination.weightUnitOfMeasure = this.defaultUnit;
            }
        }).then(exam => this.getSubjects());
    }

    initCreate(): Promise<void> {
        this.getCenters();
        this.getStudies();
        this.examination = new Examination();
        this.examination.weightUnitOfMeasure = this.defaultUnit;
        return Promise.resolve();
    }

    buildForm(): UntypedFormGroup {
        return this.formBuilder.group({
            'study': [{value: this.examination.study, disabled: this.inImport}, Validators.required],
            'subject': [{value: this.examination.subject, disabled: this.inImport}],
            'center': [{value: this.examination.center, disabled: this.inImport}, Validators.required],
            'examinationDate': [this.examination.examinationDate, [Validators.required, DatepickerComponent.validator]],
            'comment': [this.examination.comment, Validators.pattern(this.pattern)],
            'note': [this.examination.note],
            'subjectWeight': [this.examination.subjectWeight],
            'weightUnitOfMeasure': [this.examination.weightUnitOfMeasure]
        });
    }

    download(format: Format) {
        this.downloadService.downloadAllByExaminationId(this.examination?.id, format);
    }

    downloadAll() {
        let options: DownloadSetupOptions = new DownloadSetupOptions();
        options.hasBids = this.hasBids;
        options.hasDicom = this.hasDicom;
        options.hasNii = this.hasDicom;
        options.hasEeg = this.hasEEG;
        this.downloadService.downloadAllByExaminationId(this.examination?.id, null, options, this.downloadState);
    }

    openViewer() {
	    window.open(environment.viewerUrl + '/viewer/1.4.9.12.34.1.8527.' + this.entity.id, '_blank');
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

    instAssessment() {
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
            this.consoleService.log('error', 'Examination ' + this.examination.id + ' Updating study / subject / center of an examination is forbiden.');
            return null;
        } else {
            throw reason;
        }});
    }

    getFileName(element): string {
        return element.split('\\').pop().split('/').pop();
    }

    onExaminationNodeInit(node: ExaminationNode) {
        node.open = true;
        this.breadcrumbsService.currentStep.data.examinationNode = node;
        this.fetchDatasetIdsFromTree();
    }

    fetchDatasetIdsFromTree() {
        if (!this.datasetIdsLoaded) {
            let node: ExaminationNode = this.breadcrumbsService.currentStep.data.examinationNode;
            let found: boolean = false;
            // first look into the tree
            let datasetIds: number[] = [];
            if (node && node.datasetAcquisitions != 'UNLOADED') {
                found = true;
                node.datasetAcquisitions.forEach(dsAcq => {
                    if (dsAcq.datasets != 'UNLOADED') {
                        dsAcq.datasets.forEach(ds => {
                            datasetIds.push(ds.id);
							if (ds.type == 'Eeg') {
								this.hasEEG = true;
							} else if (ds.type == 'BIDS') {
                                this.hasBids = true;
                            } else {
								this.hasDicom = true;
							}
                        });
                    } else {
                        found = false;
                        return;
                    }
                });
            }
            if (found) {
                this.datasetIdsLoaded = true;
                this.datasetIds = Promise.resolve(datasetIds);
                this.noDatasets = datasetIds.length == 0;
            }
        }
    }

    getUnit(key: string) {
        return UnitOfMeasure.getLabelByKey(key);
    }
}
