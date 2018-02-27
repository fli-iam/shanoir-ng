import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { Location } from '@angular/common';
import { ActivatedRoute, Router, Params } from '@angular/router';
import { Observable } from 'rxjs/Observable';
import { FormGroup, FormBuilder, Validators, FormControl, FormArray } from '@angular/forms';
import { IMyDate, IMyDateModel, IMyInputFieldChanged, IMyOptions } from 'mydatepicker';

import { KeycloakService } from "../../shared/keycloak/keycloak.service";
import { Subject } from '../shared/subject.model';
import { SubjectService } from '../shared/subject.service';
import { Enum } from "../../shared/utils/enum";
import { ImagedObjectCategory } from '../shared/imaged-object-category.enum';
import { Sex } from '../shared/sex.enum';
import { HemisphericDominance } from '../shared/hemispheric-dominance.enum';
import * as shajs from 'sha.js';
import { StudyService } from '../../studies/shared/study.service';
import { IdNameObject } from '../../shared/models/id-name-object.model';
import { SubjectType } from '../shared/subject-type.enum';
import { SubjectStudy } from '../shared/subject-study.model';

@Component({
    selector: 'subject-detail',
    templateUrl: 'subject.component.html',
    styleUrls: ['subject.component.css']
})

export class SubjectComponent implements OnInit {

    private subject: Subject = new Subject();
    public subjectForm: FormGroup;
    public subjectStudyForm: FormGroup;
    private subjectId: number;
    public mode: "view" | "edit" | "create";
    @Input() modeFromImport: "view" | "edit" | "create";
    @Output() closing: EventEmitter<any> = new EventEmitter();
    private isNameUnique: Boolean = true;
    public canModify: Boolean = false;
    private imagedObjectCategories: Enum[] = [];
    private sexes: Enum[] = [];
    private subjectTypes: Enum[] = [];
    public studies: IdNameObject[];
    private HemisphericDominances: Enum[] = [];
    private isBirthDateValid: boolean = true;
    private selectedBirthDateNormal: IMyDate;
    private isAlreadyAnonymized: boolean;
    private hashLength: number = 14;
    private firstName: string;
    private lastName: string;
    private items: any[] = [];

    private myDatePickerOptions: IMyOptions = {
        dateFormat: 'dd/mm/yyyy',
        height: '20px',
        width: '160px'
    };

    constructor(private route: ActivatedRoute, private router: Router,
        private subjectService: SubjectService,
        private studyService: StudyService,
        private fb: FormBuilder,
        private location: Location, private keycloakService: KeycloakService) {

    }

    ngOnInit(): void {
        if (this.modeFromImport) { this.mode = this.modeFromImport; }
        this.firstName = "";
        this.lastName = "";
        this.getSubject();
        this.getImagedObjectCategories();
        if (this.mode == 'create') {
            this.subject.imagedObjectCategory = ImagedObjectCategory.LIVING_HUMAN_BEING;
        }
        this.getSexes();
        this.getHemisphericDominances();
        this.getsubjectTypes();
        this.getStudies();
        this.buildForm();
        if (this.keycloakService.isUserAdmin() || this.keycloakService.isUserExpert()) {
            this.canModify = true;
        }
    }

    getSubject(): void {
        this.route.queryParams
            .switchMap((queryParams: Params) => {
                let subjectId = queryParams['id'];
                if (!this.modeFromImport) {
                    let mode = queryParams['mode'];
                    if (mode) {
                        this.mode = mode;
                    }
                }
                if (subjectId && this.mode !== 'create') {
                    // view or edit mode
                    this.subjectId = subjectId;
                    return this.subjectService.getSubject(subjectId);
                } else {
                    // create mode
                    return Observable.of<Subject>();
                }
            })
            .subscribe((subject: Subject) => {
                this.subject = subject;
                this.getDateToDatePicker(this.subject);
            });
    }

    getImagedObjectCategories(): void {
        var imObjCat = Object.keys(ImagedObjectCategory);
        for (var i = 0; i < imObjCat.length; i = i + 2) {
            var newEnum: Enum = new Enum();
            newEnum.key = imObjCat[i];
            newEnum.value = ImagedObjectCategory[imObjCat[i]];
            this.imagedObjectCategories.push(newEnum);
        }
    }

    getSexes(): void {
        var sex = Object.keys(Sex);
        for (var i = 0; i < sex.length; i = i + 2) {
            var newEnum: Enum = new Enum();
            newEnum.key = sex[i];
            newEnum.value = Sex[sex[i]];
            this.sexes.push(newEnum);
        }
    }

    getsubjectTypes(): void {
        var type = Object.keys(SubjectType);
        for (var i = 0; i < type.length; i = i + 2) {
            var newEnum: Enum = new Enum();
            newEnum.key = type[i];
            newEnum.value = SubjectType[type[i]];
            this.subjectTypes.push(newEnum);
        }
    }

    getStudies(): void {
        this.studyService
            .getStudiesNames()
            .then(studies => {
                this.studies = studies;
            })
            .catch((error) => {
                // TODO: display error
                console.log("error getting study list!");
            });
    }

    getHemisphericDominances(): void {
        var hemisphericDominance = Object.keys(HemisphericDominance);
        for (var i = 0; i < hemisphericDominance.length; i = i + 1) {
            var newEnum: Enum = new Enum();
            newEnum.key = hemisphericDominance[i];
            newEnum.value = HemisphericDominance[hemisphericDominance[i]];
            this.HemisphericDominances.push(newEnum);
        }
    }

    buildForm(): void {
        this.subjectForm = this.fb.group({
            'imagedObjectCategory': [this.subject.imagedObjectCategory],
            'isAlreadyAnonymized': new FormControl('No'),
            'name': [this.subject.name, [Validators.required, Validators.minLength(2), Validators.maxLength(200)]],
            'firstName': [],
            'lastName': [],
            'birthDate': [this.subject.birthDate],
            'sex': [this.subject.sex],
            'manualHemisphericDominance': [this.subject.manualHemisphericDominance],
            'languageHemisphericDominance': [this.subject.languageHemisphericDominance],
            'personalComments': [],
            'listOfStudies': [],
        });

        this.subjectForm.valueChanges
            .subscribe(data => this.onValueChanged(data));
        this.onValueChanged(); // (re)set validation messages now

        this.subjectForm.get('imagedObjectCategory').valueChanges.subscribe(val => {
            this.isAlreadyAnonymized = false;
          });
    }


    get users(): FormArray {
        return this.subjectForm.get('subjectStudyList') as FormArray;
    }
    addUserField() {
        this.users.push(new FormControl());
    }
    deleteUserField(index: number) {
        this.users.removeAt(index);
    }
    onValueChanged(data?: any) {
        if (!this.subjectForm) { return; }
        const form = this.subjectForm;
        for (const field in this.formErrors) {
            // clear previous error message (if any)
            this.formErrors[field] = '';
            const control = form.get(field);
            if (control && control.dirty && !control.valid) {
                for (const key in control.errors) {
                    this.formErrors[field] += key;
                }
            }
        }
    }

    formErrors = {
        'name': ''
    };

    submit(): void {
        this.subject = this.subjectForm.value;
        this.setDateFromDatePicker();
    }

    back(): void {
        this.location.back();
    }

    edit(): void {
        this.router.navigate(['/subject'], { queryParams: { id: this.subjectId, mode: "edit" } });
    }

    create(): void {
        this.submit();
        this.setSubjectIdentifier();
        this.setSubjectBirthDateToFirstOfJanuary();
        this.subjectService.create(this.subject)
            .subscribe((subject) => {
                this.back();
            }, (err: string) => {
                this.manageRequestErrors(err);
            });
    }

    update(): void {
        this.submit();
        this.subjectService.update(this.subjectId, this.subject)
            .subscribe((subject) => {
                this.back();
            }, (err: string) => {
                this.manageRequestErrors(err);
            });
    }

    private manageRequestErrors(err: string): void {
        if (err.indexOf("name should be unique") != -1) {
            this.isNameUnique = false;
        }
    }

    resetNameErrorMsg(): void {
        this.isNameUnique = true;
    }


    getDateToDatePicker(subject: Subject): void {
        if (subject) {
            if (subject.birthDate && !isNaN(new Date(subject.birthDate).getTime())) {
                let birthDate: Date = new Date(subject.birthDate);
                this.selectedBirthDateNormal = {
                    year: birthDate.getFullYear(), month: birthDate.getMonth() + 1,
                    day: birthDate.getDate()
                };;
            }
        }
    }

    onBirthDateChanged(event: IMyDateModel) {
        if (event.formatted !== '') {
            this.selectedBirthDateNormal = event.date;
        }
    }

    onBirthDateFieldChanged(event: IMyInputFieldChanged) {
        if (event.value !== '') {
            if (!event.valid) {
                this.isBirthDateValid = false;
            } else {
                this.isBirthDateValid = true;
            }
        } else {
            this.isBirthDateValid = true;
            setTimeout((): void => this.selectedBirthDateNormal = null);
        }
    }

    setDateFromDatePicker(): void {
        if (this.selectedBirthDateNormal) {
            this.subject.birthDate = new Date(this.selectedBirthDateNormal.year, this.selectedBirthDateNormal.month - 1,
                this.selectedBirthDateNormal.day);
        } else {
            this.subject.birthDate = null;
        }
    }


    setSubjectIdentifier(): void {
        if ((this.subject.imagedObjectCategory.toString() == this.imagedObjectCategories[1].key || this.subject.imagedObjectCategory.toString() == this.imagedObjectCategories[2].key) && !this.isAlreadyAnonymized) {
            var hash = this.firstName + this.lastName + this.subject.birthDate;
            this.subject.identifier = this.getHash(hash, this.hashLength);
        }
        else {
            var hash = this.subject.name + this.subject.birthDate;
            this.subject.identifier = this.getHash(hash, this.hashLength);
        }
    }


    getHash(stringToBeHashed: string, hashLength: number): string {

        var hash = shajs('sha').update(stringToBeHashed).digest('hex');
        var hex = "";
        hex = hash.substring(0, hashLength);

        return hex;
    }



    setSubjectBirthDateToFirstOfJanuary(): void {
        var newDate: Date = new Date(this.subject.birthDate.getFullYear(), 0, 1);
        this.subject.birthDate = newDate;
    }

    onStudySelectChange(studyId) {
        var newSubjectStudy: SubjectStudy = new SubjectStudy();
        newSubjectStudy.physicallyInvolved = false;
        newSubjectStudy.studyId = studyId;

        if (this.subject.subjectStudyList != null)
            this.subject.subjectStudyList.push(newSubjectStudy);
        else {
            var newsubjectStudyList: SubjectStudy[] = new Array();
            newsubjectStudyList.push(newSubjectStudy);
            this.subject.subjectStudyList = newsubjectStudyList;
        }

        // I want to do something here for new selectedDevice, but what I
        // got here is always last selection, not the one I just select.
    }

}