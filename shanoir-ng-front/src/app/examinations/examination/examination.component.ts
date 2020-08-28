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

import { Component, ViewChild, ElementRef } from '@angular/core';
import { FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { BreadcrumbsService } from '../../breadcrumbs/breadcrumbs.service';
import { CenterService } from '../../centers/shared/center.service';
import { EntityComponent } from '../../shared/components/entity/entity.component.abstract';
import { ModalComponent } from '../../shared/components/modal/modal.component';
import { DatepickerComponent } from '../../shared/date-picker/date-picker.component';
import { IdName } from '../../shared/models/id-name.model';
import { StudyService } from '../../studies/shared/study.service';
import { SubjectWithSubjectStudy } from '../../subjects/shared/subject.with.subject-study.model';
import { Examination } from '../shared/examination.model';
import { ExaminationService } from '../shared/examination.service';
import { DatasetService } from '../../datasets/shared/dataset.service'
import { ImagesUrlUtil } from '../../shared/utils/images-url.util';

@Component({
    selector: 'examination',
    templateUrl: 'examination.component.html'
})

export class ExaminationComponent extends EntityComponent<Examination> {

    @ViewChild('instAssessmentModal') instAssessmentModal: ModalComponent;
    @ViewChild('input') private fileInput: ElementRef;

    private centers: IdName[];
    public studies: IdName[];
    private subjects: SubjectWithSubjectStudy[];
    private examinationExecutives: Object[];
    private files: File[] = [];
    private inImport: boolean; 
    protected readonly ImagesUrlUtil = ImagesUrlUtil;  
    protected bidsLoading: boolean = false;

    constructor(
            private route: ActivatedRoute,
            private examinationService: ExaminationService,
            private centerService: CenterService,
            private studyService: StudyService,
            private datasetService: DatasetService,
            protected breadcrumbsService: BreadcrumbsService) {

        super(route, 'examination');
        this.inImport = this.breadcrumbsService.isImporting();
    }
    
    private setFile() {
        this.fileInput.nativeElement.click();
    }
    
    set examination(examination: Examination) { this.entity = examination; }
    get examination(): Examination { return this.entity; }
    
    set entity(exam: Examination) {
        super.entity = exam;
        this.getSubjects();
    }

    get entity(): Examination {
        return super.entity;
    }

    initView(): Promise<void> {
        return this.examinationService.get(this.id).then((examination: Examination) => {
            this.examination = examination
        });
    }

    initEdit(): Promise<void> {
        this.getCenters();
        this.getStudies();
        return this.examinationService.get(this.id).then((examination: Examination) => {
            this.examination = examination
        }).then(exam => this.getSubjects());
    }

    initCreate(): Promise<void> {
        this.getCenters();
        this.getStudies();
        this.examination = new Examination();
        return Promise.resolve();
    }

    buildForm(): FormGroup {
        return this.formBuilder.group({
            'study': [{value: this.examination.study, disabled: this.inImport}, Validators.required],
            'subject': [{value: this.examination.subject, disabled: this.inImport}],
            'center': [{value: this.examination.center, disabled: this.inImport}, Validators.required],
            'examinationDate': [this.examination.examinationDate, [Validators.required, DatepickerComponent.validator]],
            'comment': [this.examination.comment],
            'note': [this.examination.note],
            'subjectWeight': [this.examination.subjectWeight]
        });
    }

    private getCenters(): void {
        this.centerService
            .getCentersNames()
            .then(centers => {
                this.centers = centers;
            });
    }

    private getStudies(): void {
        this.studyService
            .getStudiesNames()
            .then(studies => {
                this.studies = studies;
            });
    }

    private getSubjects(): void {
        if (!this.examination || !this.examination.study) return;
        this.studyService
            .findSubjectsByStudyId(this.examination.study.id)
            .then(subjects => this.subjects = subjects);
    }

    private onStudyChange() {
        this.getSubjects();
    }

    private instAssessment() {
    }

    public hasEditRight(): boolean {
        return false;
    }

    protected deleteFile(file: any) {
        this.examination.extraDataFilePathList = this.examination.extraDataFilePathList.filter(fileToKeep => fileToKeep != file);
        this.files = this.files.filter(fileToKeep => fileToKeep.name != file);
    }

    private attachNewFile(event: any) {
        let newFile = event.target.files[0];
        this.examination.extraDataFilePathList.push(newFile.name);
        this.files.push(newFile);
    }

    protected save(): Promise<void> {
        let prom = super.save().then(result => {
            // Once the exam is saved, save associated files
            for (let file of this.files) {
                this.examinationService.postFile(file, this.entity.id);
            }            
        });
        return prom;
    }

    getFileName(element): string {
        return element.split('\\').pop().split('/').pop();
    }
}