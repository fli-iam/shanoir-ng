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
import { FormGroup, Validators, AbstractControl, ValidatorFn } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import * as shajs from 'sha.js';

import { preventInitialChildAnimations, slideDown } from '../../shared/animations/animations';
import { EntityComponent } from '../../shared/components/entity/entity.component.abstract';
import { DatepickerComponent } from '../../shared/date-picker/date-picker.component';
import { IdName } from '../../shared/models/id-name.model';
import { Option } from '../../shared/select/select.component';
import { StudyService } from '../../studies/shared/study.service';
import { ReverseSubjectNode, SubjectNode } from '../../tree/tree.model';
import { ImagedObjectCategory } from '../shared/imaged-object-category.enum';
import { Subject } from '../shared/subject.model';
import { SubjectService } from '../shared/subject.service';
import { EntityService } from 'src/app/shared/components/entity/entity.abstract.service';
import { Study } from '../../studies/shared/study.model';

@Component({
    selector: 'subject-detail',
    templateUrl: 'subject.component.html',
    styleUrls: ['subject.component.css'],
    animations: [slideDown, preventInitialChildAnimations]
})

export class SubjectComponent extends EntityComponent<Subject> implements OnInit {

    readonly ImagedObjectCategory = ImagedObjectCategory;
    private readonly HASH_LENGTH: number = 14;
    studies: IdName[] = [];
    isAlreadyAnonymized: boolean;
    firstName: string = "";
    lastName: string = "";
    subjectNamePrefix: string = "";
    pattern: string = '[^:|<>&\/]+';
    private nameValidators = [Validators.required, Validators.minLength(2), Validators.maxLength(64), Validators.pattern(this.pattern)];
    forceStudy: Study = null;
    dicomPatientName: string;
    subjectNode: Subject | SubjectNode;

    catOptions: Option<ImagedObjectCategory>[] = [
        new Option<ImagedObjectCategory>(ImagedObjectCategory.PHANTOM, 'Phantom'),
        new Option<ImagedObjectCategory>(ImagedObjectCategory.LIVING_HUMAN_BEING, 'Living human being'),
        new Option<ImagedObjectCategory>(ImagedObjectCategory.HUMAN_CADAVER, 'Human cadaver'),
        new Option<ImagedObjectCategory>(ImagedObjectCategory.ANATOMICAL_PIECE, 'Anatomical piece')
    ];

    genderOptions: Option<string>[] = [
        new Option<string>('F', 'Female'),
        new Option<string>('M', 'Male'),
        new Option<string>('O', 'Other'),
    ];

    constructor(private route: ActivatedRoute,
            private subjectService: SubjectService,
            private studyService: StudyService) {

        super(route, 'subject');
    }

    public get subject(): Subject { return this.entity; }
    public set subject(subject: Subject) { 
        this.entity = subject;
        this.subjectNode = this.breadcrumbsService.currentStep.data.subjectNode ? this.breadcrumbsService.currentStep.data.subjectNode : subject;
    }

    getService(): EntityService<Subject> {
        return this.subjectService;
    }

    ngOnInit() {
        super.ngOnInit();
        if (this.mode == 'create') {
            this.firstName = this.breadcrumbsService.currentStep.data.firstName;
            this.lastName = this.breadcrumbsService.currentStep.data.lastName;
            this.forceStudy = this.breadcrumbsService.currentStep.data.forceStudy;
	        if (this.breadcrumbsService.currentStep.data.patientName) this.dicomPatientName = this.breadcrumbsService.currentStep.data.patientName;
            if (this.breadcrumbsService.currentStep.data.subjectNamePrefix) {
                if (this.forceStudy?.name) this.subjectNamePrefix = this.forceStudy.name + '-';
                this.subjectNamePrefix += this.breadcrumbsService.currentStep.data.subjectNamePrefix + '-';
            }
            if (this.subjectNamePrefix) {
                this.subject.name = this.subjectNamePrefix;
	        }
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
            'name': [this.subject.name, this.nameValidators.concat([this.registerOnSubmitValidator('unique', 'name')]).concat(this.forbiddenNameValidator([this.subjectNamePrefix]))],
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
        if (!this.subject.name && this.subjectNamePrefix) {
            this.subject.name = this.subjectNamePrefix;
        }
        return subjectForm;
    }
    
    private forbiddenNameValidator(forbiddenValues: string[]): ValidatorFn {
      return (c: AbstractControl): { [key: string]: boolean } | null => {
        if (forbiddenValues.indexOf(c.value) !== -1) {
          return { 'subjectNamePrefix': true };
        }
        return null;
      };
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

    save(): Promise<Subject> {
        let savedDate: Date;
        if (this.mode == 'create') {
            this.subject.identifier = this.generateSubjectIdentifier();
            this.setSubjectBirthDateToFirstOfJanuary();
        }
        return super.save()
            .then(() => { if (savedDate) this.subject.birthDate = savedDate; return this.subject; })
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

    getHash(stringToBeHashed: string): string {
        let hash = shajs('sha').update(stringToBeHashed).digest('hex');
        let hex = hash.substring(0, this.HASH_LENGTH);
        return hex;
    }

    humanSelected(): boolean {
        return this.subject.imagedObjectCategory != null
            && (this.subject.imagedObjectCategory == ImagedObjectCategory.HUMAN_CADAVER
                || this.subject.imagedObjectCategory == ImagedObjectCategory.LIVING_HUMAN_BEING);
    }

    private setSubjectBirthDateToFirstOfJanuary(): void {
        let newDate: Date = new Date(new Date(this.subject.birthDate).getFullYear(), 0, 1);
        this.subject.birthDate = newDate;
    }

    public async hasEditRight(): Promise<boolean> {
        return this.keycloakService.isUserAdminOrExpert();
    }

    onSubjectNodeInit(node: ReverseSubjectNode) {
        this.breadcrumbsService.currentStep.data.subjectNode = node;
    }

    public toggleAnonymised() {
        if (this.isAlreadyAnonymized && this.subjectNamePrefix) {
            this.subject.name = this.subjectNamePrefix + this.dicomPatientName;
        } else if (!this.isAlreadyAnonymized && this.subjectNamePrefix && this.dicomPatientName) {
            this.subject.name = this.subjectNamePrefix; 
        }
    }
}