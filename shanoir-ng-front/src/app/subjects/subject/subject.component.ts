import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { Location } from '@angular/common';
import { ActivatedRoute, Router, Params } from '@angular/router';
import { Observable } from 'rxjs/Observable';
import { FormGroup, FormBuilder, Validators, FormControl, FormArray } from '@angular/forms';
import { IMyDate, IMyDateModel, IMyInputFieldChanged, IMyOptions } from 'mydatepicker';

import { KeycloakService } from "../../shared/keycloak/keycloak.service";
import { Subject } from '../shared/subject.model';
import { SubjectService } from '../shared/subject.service';
import { ImagedObjectCategory } from '../shared/imaged-object-category.enum';
import { ImagesUrlUtil } from '../../shared/utils/images-url.util';
import * as shajs from 'sha.js';
import { StudyService } from '../../studies/shared/study.service';
import { IdNameObject } from '../../shared/models/id-name-object.model';
import { SubjectType } from '../shared/subject-type.enum';
import { SubjectStudy } from '../shared/subject-study.model';
import { ModalService } from '../../shared/components/modal/modal.service';
import { slideDown, preventInitialChildAnimations} from '../../shared/animations/animations';

@Component({
    selector: 'subject-detail',
    templateUrl: 'subject.component.html',
    styleUrls: ['subject.component.css'],
    animations: [slideDown, preventInitialChildAnimations]
})

export class SubjectComponent implements OnInit {

    private deleteIconPath: string = ImagesUrlUtil.DELETE_ICON_PATH;
    private subject: Subject = new Subject();
    public subjectForm: FormGroup;
    public subjectStudyForm: FormGroup;
    private subjectId: number;
    public mode: "view" | "edit" | "create";
    @Input() modeFromImport: "view" | "edit" | "create";
    @Output() closing: EventEmitter<any> = new EventEmitter();
    private isNameUnique: Boolean = true;
    public canModify: Boolean = false;
    public studies: IdNameObject[];
    private isBirthDateValid: boolean = true;
    private selectedBirthDateNormal: IMyDate;
    private isAlreadyAnonymized: boolean;
    private hashLength: number = 14;
    private firstName: string = "";
    private lastName: string = "";

    private init: boolean = false;

    private myDatePickerOptions: IMyOptions = {
        dateFormat: 'dd/mm/yyyy',
        height: '20px',
        width: '160px'
    };

    constructor(private route: ActivatedRoute, private router: Router,
        private subjectService: SubjectService,
        private studyService: StudyService,
        private fb: FormBuilder,
        private location: Location, private keycloakService: KeycloakService,
        private modalService: ModalService) {
    }

    ngOnInit(): void {
        if (this.modeFromImport) {
            this.mode = this.modeFromImport; 
            this.modalService.objectPassedByModal
                .subscribe((subjectFromImport) => {
                    this.computeNameFromDicomTag(subjectFromImport.name);
                    this.subject.sex = subjectFromImport.sex;
                    this.getDateToDatePicker(subjectFromImport);
                    let study : IdNameObject = new IdNameObject();
                    study.id = subjectFromImport.study.id;
                    study.name = subjectFromImport.study.name;
                    this.studies = [];
                    this.studies.push(study);
                })
        }
        if (this.modeFromImport == null) {this.getStudies();}
        this.getSubject();
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
                if (subjectId) {
                    // view or edit mode
                    this.subjectId = subjectId;
                    return this.subjectService.getSubject(subjectId);
                } else {
                    // create mode
                    this.subject.imagedObjectCategory = ImagedObjectCategory.HUMAN_CADAVER;
                    return Observable.of<Subject>();
                }
            })
            .subscribe((subject: Subject) => {
                this.subject = subject;
                this.getDateToDatePicker(this.subject);
            });
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
            'studies': [],
        });

        this.subjectForm.valueChanges.subscribe(data => this.onValueChanged(data));
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
        for (let subjectStudy of this.subject.subjectStudyList) {
            this.subjectService.createSubjectStudy(subjectStudy);
        }
        this.subjectService.create(this.subject)
            .subscribe((subject) => {
                this.back();
            }, (err: string) => {
                this.manageRequestErrors(err);
            });
    }

    update(): void {
        this.submit();
        for (let subjectStudy of this.subject.subjectStudyList) {
            if (this.subjectService.findSubjectStudyById(subjectStudy.id)) {
                this.subjectService.updateSubjectStudy(subjectStudy);
            } else {
                this.subjectService.createSubjectStudy(subjectStudy);
            }
        }
        this.subjectService.update(this.subjectId, this.subject)
            .subscribe((subject) => {
                this.back();
            }, (err: string) => {
                this.manageRequestErrors(err);
            });
    }

    removeSubjectStudy(subjectStudy: SubjectStudy):void {
        const index: number = this.subject.subjectStudyList.indexOf(subjectStudy);
        if (index !== -1) {
            this.subject.subjectStudyList.splice(index, 1);
        }
        this.subjectService.deleteSubjectStudy(subjectStudy.id);
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
        if (subject && subject.birthDate) {
            let birthDate: Date = new Date(parseInt(subject.birthDate.toString()));
            this.selectedBirthDateNormal = {year: birthDate.getFullYear(), month: birthDate.getMonth() + 1, 
                day: birthDate.getDate()};
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
        if (this.humanSelected() && !this.isAlreadyAnonymized) {
            let hash = this.firstName + this.lastName + this.subject.birthDate;
            this.subject.identifier = this.getHash(hash, this.hashLength);
        }
        else {
            let hash = this.subject.name + this.subject.birthDate;
            this.subject.identifier = this.getHash(hash, this.hashLength);
        }
    }

    getHash(stringToBeHashed: string, hashLength: number): string {
        let hash = shajs('sha').update(stringToBeHashed).digest('hex');
        let hex = "";
        hex = hash.substring(0, hashLength);
        return hex;
    }

    setSubjectBirthDateToFirstOfJanuary(): void {
        var newDate: Date = new Date(this.subject.birthDate.getFullYear(), 0, 1);
        this.subject.birthDate = newDate;
    }

    onStudySelectChange(studyId: number) {
        var newSubjectStudy: SubjectStudy = new SubjectStudy();
        newSubjectStudy.physicallyInvolved = false;
        newSubjectStudy.studyId = studyId;
        this.subject.subjectStudyList.push(newSubjectStudy);

        // if (this.subject.subjectStudyList != null)
        //     this.subject.subjectStudyList.push(newSubjectStudy);
        // else {
        //     var newsubjectStudyList: SubjectStudy[] = new Array();
        //     newsubjectStudyList.push(newSubjectStudy);
        //     this.subject.subjectStudyList = newsubjectStudyList;
        // }

        // I want to do something here for new selectedDevice, but what I
        // got here is always last selection, not the one I just select.
    }

    computeNameFromDicomTag (patientName: string): void {
        /* Try to compute patient first name and last name from dicom tags. 
        eg. TOM^HANKS -> return TOM as first name and HANKS as last name */
        if (patientName !== null) {
            let names: string[] = patientName.split("\\^");
            if (names !== null && names.length == 2) {
                this.firstName = names[1];
                this.lastName = names[2];
            } else {
                this.firstName = this.lastName = patientName;
            }
        }
    }

    public humanSelected(): boolean {
        return this.subject.imagedObjectCategory != null
            && (this.subject.imagedObjectCategory == ImagedObjectCategory.HUMAN_CADAVER 
                || this.subject.imagedObjectCategory == ImagedObjectCategory.LIVING_HUMAN_BEING);
    }

    public imagedObjectCategories() {
        return ImagedObjectCategory.keyValues();
    }

    public subjectTypes() {
        return SubjectType.keyValues();
    }
}