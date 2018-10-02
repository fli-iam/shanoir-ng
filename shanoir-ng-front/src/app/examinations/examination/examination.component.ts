import { Component, ViewChild } from '@angular/core';
import { FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';

import { CenterService } from '../../centers/shared/center.service';
import { EntityComponent } from '../../shared/components/entity/entity.component.abstract';
import { ModalComponent } from '../../shared/components/modal/modal.component';
import { IdNameObject } from '../../shared/models/id-name-object.model';
import { StudyService } from '../../studies/shared/study.service';
import { Examination } from '../shared/examination.model';
import { ExaminationService } from '../shared/examination.service';


@Component({
    selector: 'examination',
    templateUrl: 'examination.component.html',
    styleUrls: ['examination.component.css'],
})

export class ExaminationComponent extends EntityComponent<Examination> {

    @ViewChild('instAssessmentModal') instAssessmentModal: ModalComponent;
    @ViewChild('attachNewFilesModal') attachNewFilesModal: ModalComponent;
    private centers: IdNameObject[];
    public studies: IdNameObject[];
    private subjects: IdNameObject[];
    private examinationExecutives: Object[];

    constructor(
            private route: ActivatedRoute,
            private examinationService: ExaminationService,
            private centerService: CenterService,
            private studyService: StudyService) {

        super(route, 'examination');
    }

    set examination(examination: Examination) { this.entity = examination; }
    get examination(): Examination { return this.entity; }

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
        });
    }

    initCreate(): Promise<void> {
        this.getCenters();
        this.getStudies();
        this.examination = new Examination();
        return Promise.resolve();
    }

    buildForm(): FormGroup {
        return this.formBuilder.group({
            'id': [this.examination.id],
            'studyId': [this.examination.studyId, Validators.required],
            // 'Examination executive': [this.examination.examinationExecutive],
            'centerId': [this.examination.centerId, Validators.required],
            // 'Subject': [this.examination.subject],
            'examinationDate': [this.examination.examinationDate, Validators.required],
            'comment': [this.examination.comment],
            'note': [this.examination.note],
            'subjectWeight': [this.examination.subjectWeight]
        });
    }

    private getCenters(): void {
        this.centerService
            .getCentersNamesForExamination()
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

    private instAssessment() {
    }

    private attachNewFiles() {
    }

}