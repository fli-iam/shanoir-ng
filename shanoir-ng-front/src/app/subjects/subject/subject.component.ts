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

import { Component, OnInit } from '@angular/core';
import { FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import * as shajs from 'sha.js';
import { preventInitialChildAnimations, slideDown } from '../../shared/animations/animations';
import { EntityComponent } from '../../shared/components/entity/entity.component.abstract';
import { DatepickerComponent } from '../../shared/date-picker/date-picker.component';
import { IdName } from '../../shared/models/id-name.model';
import { StudyService } from '../../studies/shared/study.service';
import { ImagedObjectCategory } from '../shared/imaged-object-category.enum';
import { Subject } from '../shared/subject.model';
import { SubjectService } from '../shared/subject.service';

@Component({
    selector: 'subject-detail',
    templateUrl: 'subject.component.html',
    styleUrls: ['subject.component.css'],
    animations: [slideDown, preventInitialChildAnimations]
})

export class SubjectComponent extends EntityComponent<Subject> implements OnInit {

    private readonly ImagedObjectCategory = ImagedObjectCategory;
    private readonly HASH_LENGTH: number = 14;
    private studies: IdName[] = [];
    private isAlreadyAnonymized: boolean;
    private firstName: string = "";
    private lastName: string = "";
    private nameValidators = [Validators.required, Validators.minLength(2), Validators.maxLength(64)];

    constructor(private route: ActivatedRoute,
            private subjectService: SubjectService,
            private studyService: StudyService) {

        super(route, 'subject');
    }

    public get subject(): Subject { return this.entity; }
    public set subject(subject: Subject) { this.entity = subject; }

    ngOnInit() {
        super.ngOnInit();
        if (this.mode == 'create') {
            this.firstName = this.breadcrumbsService.currentStep.data.firstName;
            this.lastName = this.breadcrumbsService.currentStep.data.lastName;
        }
    }

    initView(): Promise<void> {
        this.loadAllStudies();
        return this.subjectService.get(this.id).then(subject => { this.subject = subject; });
    }

    initEdit(): Promise<void> {
        this.loadAllStudies();
        return this.subjectService.get(this.id).then(subject => { this.subject = subject; });
    }

    initCreate(): Promise<void> {
        this.loadAllStudies();
        this.subject = new Subject();
        this.subject.imagedObjectCategory = ImagedObjectCategory.LIVING_HUMAN_BEING;
        return Promise.resolve();
    }

    buildForm(): FormGroup {
        let subjectForm = this.formBuilder.group({
            'imagedObjectCategory': [this.subject.imagedObjectCategory, [Validators.required]],
            'isAlreadyAnonymized': [],
            'name': [this.subject.name, this.nameValidators.concat([this.registerOnSubmitValidator('unique', 'name')])],
            'firstName': [this.firstName],
            'lastName': [this.lastName],
            'birthDate': [this.subject.birthDate],
            'sex': [this.subject.sex],
            'subjectStudyList': [],
            'manualHemisphericDominance': [this.subject.manualHemisphericDominance],
            'languageHemisphericDominance': [this.subject.languageHemisphericDominance],
            'personalComments': []
        });
        this.updateFormControl(subjectForm);
        this.subscribtions.push(
            subjectForm.get('imagedObjectCategory').valueChanges.subscribe(val => {
                this.isAlreadyAnonymized = false;
                this.updateFormControl(subjectForm);
            })
        );
        this.subscribtions.push(
            subjectForm.get('isAlreadyAnonymized').valueChanges.subscribe(val => {
                this.updateFormControl(subjectForm);
            })
        );
        return subjectForm;
    }

    private updateFormControl(formGroup: FormGroup) {
        if (this.subject.imagedObjectCategory == ImagedObjectCategory.LIVING_HUMAN_BEING && !this.isAlreadyAnonymized && this.mode == 'create') {
            formGroup.get('firstName').setValidators(this.nameValidators);
            formGroup.get('lastName').setValidators(this.nameValidators);
            formGroup.get('birthDate').setValidators([Validators.required, DatepickerComponent.validator])
        } else {
            formGroup.get('firstName').setValidators([]);
            formGroup.get('lastName').setValidators([]);
            formGroup.get('birthDate').setValidators([DatepickerComponent.validator])
        }
        formGroup.get('firstName').updateValueAndValidity();
        formGroup.get('lastName').updateValueAndValidity();
        formGroup.get('birthDate').updateValueAndValidity();
        this.reloadRequiredStyles();
    }

    save(): Promise<void> {
        let savedDate: Date;
        if (this.mode == 'create') {
            this.subject.identifier = this.generateSubjectIdentifier();
            this.setSubjectBirthDateToFirstOfJanuary();
        }
        return super.save()
            .then(() => { if (savedDate) this.subject.birthDate = savedDate })
            .catch(reason => { if (savedDate) this.subject.birthDate = savedDate; throw reason; })
    }

    loadAllStudies(): void {
        this.studyService
            .getStudiesNames()
            .then(studies => {
                this.studies = studies;
            });
    }

    private generateSubjectIdentifier(): string {
        let hash;
        if (this.humanSelected() && !this.isAlreadyAnonymized) {
            hash = this.firstName + this.lastName + this.subject.birthDate;
        }
        else {
            hash = this.subject.name + this.subject.birthDate;
        }
        return this.getHash(hash);
    }

    private getHash(stringToBeHashed: string): string {
        let hash = shajs('sha').update(stringToBeHashed).digest('hex');
        let hex = "";
        hex = hash.substring(0, this.HASH_LENGTH);
        return hex;
    }

    private humanSelected(): boolean {
        return this.subject.imagedObjectCategory != null
            && (this.subject.imagedObjectCategory == ImagedObjectCategory.HUMAN_CADAVER
                || this.subject.imagedObjectCategory == ImagedObjectCategory.LIVING_HUMAN_BEING);
    }

    private setSubjectBirthDateToFirstOfJanuary(): void {
        let newDate: Date = new Date(new Date(this.subject.birthDate).getFullYear(), 0, 1);
        this.subject.birthDate = newDate;
    }

    public hasEditRight(): boolean {
        return this.keycloakService.isUserAdminOrExpert();
    }
}