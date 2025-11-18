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
import {Component, OnDestroy} from '@angular/core';
import { AbstractControl, UntypedFormGroup, ValidatorFn, Validators, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { NgClass } from '@angular/common';
import shajs from 'sha.js';

import { EntityService } from 'src/app/shared/components/entity/entity.abstract.service';
import { Selection } from 'src/app/studies/study/tree.service';
import { MassDownloadService } from 'src/app/shared/mass-download/mass-download.service';
import { TaskState } from 'src/app/async-tasks/task.model';
import { StudyUserRight } from 'src/app/studies/shared/study-user-right.enum';
import { StudyRightsService } from 'src/app/studies/shared/study-rights.service';

import { EntityComponent } from '../../shared/components/entity/entity.component.abstract';
import { DatepickerComponent } from '../../shared/date-picker/date-picker.component';
import { IdName } from '../../shared/models/id-name.model';
import { Option, SelectBoxComponent } from '../../shared/select/select.component';
import { Study } from '../../studies/shared/study.model';
import { StudyService } from '../../studies/shared/study.service';
import { ImagedObjectCategory } from '../shared/imaged-object-category.enum';
import { Subject } from '../shared/subject.model';
import { SubjectService } from '../shared/subject.service';
import {Tag} from "../../tags/tag.model";
import {dateDisplay} from "../../shared/./localLanguage/localDate.abstract";
import {isDarkColor} from "../../utils/app.utils";
import { FormFooterComponent } from '../../shared/components/form-footer/form-footer.component';
import { CheckboxComponent } from '../../shared/checkbox/checkbox.component';
import { TagInputComponent } from '../../tags/tag.input.component';
import { LocalDateFormatPipe } from '../../shared/localLanguage/localDateFormat.pipe';

@Component({
    selector: 'subject-detail',
    templateUrl: 'subject.component.html',
    styleUrls: ['subject.component.css'],
    imports: [FormsModule, ReactiveFormsModule, NgClass, FormFooterComponent, SelectBoxComponent, DatepickerComponent, CheckboxComponent, TagInputComponent, LocalDateFormatPipe]
})

export class SubjectComponent extends EntityComponent<Subject> implements OnDestroy {

    readonly ImagedObjectCategory = ImagedObjectCategory;
    private readonly HASH_LENGTH: number = 14;
    studies: IdName[] = [];
    //isAlreadyAnonymized: boolean = false;
    firstName: string = "";
    lastName: string = "";
    subjectNamePrefix: string = "";
    pattern: RegExp = /[^:|<>&/]+/;
    private nameValidators = [Validators.required, Validators.minLength(2), Validators.maxLength(64), Validators.pattern(this.pattern)];
    forceStudy: Study = null;
    dicomPatientName: string;
    downloadState: TaskState = new TaskState();
    hasDownloadRight: boolean = false;
    importMode: string = "";
    isImporting: boolean = false;
    tags: Tag[] = [];

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

    public subjectTypes: Option<string>[] = [
        new Option<string>('HEALTHY_VOLUNTEER', 'Healthy Volunteer'),
        new Option<string>('PATIENT', 'Patient'),
        new Option<string>('PHANTOM', 'Phantom')
    ];

    constructor(private route: ActivatedRoute,
                private subjectService: SubjectService,
                private studyService: StudyService,
                private downloadService: MassDownloadService,
                private studyRightsService: StudyRightsService) {

        super(route, 'subject');
    }

    public get subject(): Subject { return this.entity; }
    public set subject(subject: Subject) {
        this.entity = subject;
    }

    getService(): EntityService<Subject> {
        return this.subjectService;
    }

    protected getTreeSelection: () => Selection = () => {
        return Selection.fromSubject(this.subject);
    }

    init() {
        super.init();
        if (this.mode == 'create') {
            this.breadcrumbsService.currentStep.getPrefilledValue("firstName").then( res => {
                this.firstName = res;
                this.form.get('firstName').setValue(this.firstName);
            });
            this.breadcrumbsService.currentStep.getPrefilledValue("lastName").then( res => {
                this.lastName = res;
                this.form.get('lastName').setValue(this.lastName);
            });
            this.breadcrumbsService.currentStep.getPrefilledValue("forceStudy").then( res => this.forceStudy = res);
            this.breadcrumbsService.currentStep.getPrefilledValue("birthDate").then( res => this.subject.birthDate = res);
            this.breadcrumbsService.currentStep.getPrefilledValue("subjectStudyList").then( () => this.subject.subjectStudyList = []);
            this.breadcrumbsService.currentStep.getPrefilledValue("isAlreadyAnonymized").then( res => this.subject.isAlreadyAnonymized = res);

            if (this.breadcrumbsService.currentStep?.data.patientName) this.dicomPatientName = this.breadcrumbsService.currentStep.data.patientName;
            if (this.breadcrumbsService.currentStep?.data.subjectNamePrefix) {
                if (this.forceStudy?.name) this.subjectNamePrefix = this.forceStudy.name + '-';
                this.subjectNamePrefix += this.breadcrumbsService.currentStep.data.subjectNamePrefix + '-';
            }
            if (this.subjectNamePrefix) {
                this.subject.name = this.subjectNamePrefix;
            }
            this.isImporting = this.breadcrumbsService.isImporting();
            if (this.isImporting)
                this.importMode = this.breadcrumbsService.findImportMode();
        }
    }

    initView(): Promise<void> {
        this.loadAllStudies();
        if (this.keycloakService.isUserAdmin()) {
            this.hasDownloadRight = true;
            return;
        } else {
            this.treeService.studyPromise.then(study => {
                return this.studyRightsService.getMyRightsForStudy(study.id).then(rights => {
                    this.hasDownloadRight = rights.includes(StudyUserRight.CAN_DOWNLOAD);
                });
            });
        }
        return Promise.resolve();
    }

    initEdit(): Promise<void> {
        this.loadAllStudies();
        return Promise.resolve();
    }

    initCreate(): Promise<void> {
        this.loadAllStudies();
        this.subject = new Subject();
        this.subject.imagedObjectCategory = ImagedObjectCategory.LIVING_HUMAN_BEING;
        return Promise.resolve();
    }

    buildForm(): UntypedFormGroup {
        const subjectForm = this.formBuilder.group({
            'imagedObjectCategory': [this.subject.imagedObjectCategory, [Validators.required]],
            'isAlreadyAnonymized': [this.subject.isAlreadyAnonymized],
            'name': [this.subject.name, this.nameValidators.concat([this.registerOnSubmitValidator('unique', 'name')]).concat(this.forbiddenNameValidator([this.subjectNamePrefix])).concat([this.notEmptyValidator()])],
            'firstName': [this.firstName],
            'lastName': [this.lastName],
            'birthDate': [this.subject.birthDate],
            'sex': [this.subject.sex],
            'manualHemisphericDominance': [this.subject.manualHemisphericDominance],
            'languageHemisphericDominance': [this.subject.languageHemisphericDominance],
            'studyIdentifier': [this.subject.identifier],
            'physicallyInvolved': [this.subject.physicallyInvolved],
            'tags': [this.subject.tags],
            'study': [this.subject.study, (this.mode == 'view' || this.mode == 'edit') ? [] : [Validators.required]],
            'subjectType': [this.subject.subjectType, Validators.required],
            'personalComments': []
        });
        this.updateFormControl(subjectForm);
        this.subscriptions.push(
            subjectForm.get('imagedObjectCategory').valueChanges.subscribe(() => {
                this.subject.isAlreadyAnonymized = false;
                this.updateFormControl(subjectForm);
            })
        );
        this.subscriptions.push(
            subjectForm.get('isAlreadyAnonymized').valueChanges.subscribe(() => {
                this.toggleAnonymised();
                this.updateFormControl(subjectForm);
            })
        );
        if (!this.subject.name && this.subjectNamePrefix) {
            this.subject.name = this.subjectNamePrefix;
        }

        return subjectForm;
    }

    public onSelectStudy() {
        this.studyService.get(this.subject.study?.id).then(study => {
            this.subject.study = study;
            this.studyService.getTagsFromStudyId(this.subject.study.id).then(tags => {
                this.subject.study.tags = tags ? tags : [];
            })
        });
    }

    private forbiddenNameValidator(forbiddenValues: string[]): ValidatorFn {
        return (c: AbstractControl): Record<string, boolean> | null => {
            if (forbiddenValues.indexOf(c.value) !== -1) {
                return { 'subjectNamePrefix': true };
            }
            return null;
        };
    }

    private notEmptyValidator(): ValidatorFn {
        return (c: AbstractControl): Record<string, boolean> | null => {
            if (!c.value || c.value.trim().length < 2) {
                return { 'notEmptyValidator': true };
            }
            return null;
        };
    }

    private updateFormControl(formGroup: UntypedFormGroup) {
        if (formGroup.get('imagedObjectCategory').value == ImagedObjectCategory.LIVING_HUMAN_BEING && !this.subject.isAlreadyAnonymized && this.mode == 'create') {
            if (this.importMode != 'EEG') {
                formGroup.get('firstName').setValidators(this.nameValidators);
                formGroup.get('lastName').setValidators(this.nameValidators);
            }
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
        this.subject = { ...this.subject, study: { id: this.subject.study.id } as Study };
        this.subject.subjectStudyList = null;
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

    studyNameForSubject() {
        this.studies = this.studies.filter(s => s.id == this.entity.study.id);
        return this.studies[0] ? this.studies[0].name : "";
    }

    private generateSubjectIdentifier(): string {
        let hash;
        if (this.humanSelected() && !this.subject.isAlreadyAnonymized) {
            hash = this.firstName + this.lastName + this.subject.birthDate;
        }
        else {
            hash = this.subject.name + this.subject.birthDate;
        }
        return this.getHash(hash);
    }

    getHash(stringToBeHashed: string): string {
        const hash = shajs('sha').update(stringToBeHashed).digest('hex');
        const hex = hash.substring(0, this.HASH_LENGTH);
        return hex;
    }

    humanSelected(): boolean {
        return this.subject.imagedObjectCategory != null
            && (this.subject.imagedObjectCategory == ImagedObjectCategory.HUMAN_CADAVER
                || this.subject.imagedObjectCategory == ImagedObjectCategory.LIVING_HUMAN_BEING);
    }

    private setSubjectBirthDateToFirstOfJanuary(): void {
        const newDate: Date = new Date(new Date(this.subject.birthDate).getFullYear(), 0, 1);
        this.subject.birthDate = newDate;
    }

    public async hasEditRight(): Promise<boolean> {
        return this.keycloakService.isUserAdminOrExpert();
    }

    public toggleAnonymised() {
        if (this.subject.isAlreadyAnonymized && this.subjectNamePrefix) {
            this.subject.name = this.subjectNamePrefix + this.dicomPatientName;
        } else if (!this.subject.isAlreadyAnonymized && this.subjectNamePrefix && this.dicomPatientName) {
            this.subject.name = this.subjectNamePrefix;
        }
    }

    download() {
        // TODO : select study
        this.downloadService.downloadAllByStudyIdAndSubjectId(this.treeService.study.id, this.subject.id, this.downloadState);
    }

    getOnDeleteConfirmMessage(entity: Subject): Promise<string> {
        let studyListStr : string = "";
        if (entity.subjectStudyList.length > 0) {
            studyListStr = "\n\nThis subject belongs to the studies: \n- ";
            const studiesNames = entity.subjectStudyList.map(study => study.study.name).join('\n- ');
            studyListStr += studiesNames;
        }
        studyListStr += '\n\nWarning: this action deletes ALL datasets ';
        if (entity.subjectStudyList.length > 0) {
            studyListStr += 'from ALL studies listed above.';
        } else {
            studyListStr += 'from this subject.';
        }
        return Promise.resolve(studyListStr);
    }

    ngOnDestroy() {
        super.ngOnDestroy();
        this.breadcrumbsService.currentStep.addPrefilled("firstName", this.firstName);
        this.breadcrumbsService.currentStep.addPrefilled("lastName", this.lastName);
        this.breadcrumbsService.currentStep.addPrefilled("forceStudy", this.forceStudy);
    }

    getFontColor(colorInp: string): boolean {
        return isDarkColor(colorInp);
    }

    protected readonly dateDisplay = dateDisplay;
}
