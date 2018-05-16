import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { Location } from '@angular/common';
import { ActivatedRoute, Router, Params } from '@angular/router';
import { Observable } from 'rxjs/Observable';
import { FormGroup, FormBuilder, Validators, FormControl, FormArray, ValidationErrors } from '@angular/forms';
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
import { slideDown, preventInitialChildAnimations} from '../../shared/animations/animations';

@Component({
    selector: 'subject-detail',
    templateUrl: 'subject.component.html',
    styleUrls: ['subject.component.css'],
    animations: [slideDown, preventInitialChildAnimations]
})

export class SubjectComponent implements OnInit {

    private ImagesUrlUtil = ImagesUrlUtil; // Make it visible to the template
    
    @Input() mode: "view" | "edit" | "create";
    @Input() preFillData: any;
    
    private subject: Subject;
    public subjectForm: FormGroup;
    public subjectStudyForm: FormGroup;
    private formErrors: any = {};
    public canModify: Boolean = false;
    private firstName: string = "";
    private lastName: string = "";

    @Output() closing: EventEmitter<any> = new EventEmitter();

    public studies: IdNameObject[];
    private isBirthDateValid: boolean = true;
    private selectedBirthDateNormal: IMyDate;
    private isAlreadyAnonymized: boolean;
    private hashLength: number = 14;

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
        private location: Location, 
        private keycloakService: KeycloakService) {
    }

    ngOnInit(): void {
        this.chooseMode().then(this.initData.bind(this));
    }

    private initData() {
        switch(this.mode) {
            case 'create': {
                this.loadAllStudies();
                this.setSubject(new Subject());
                this.subject.imagedObjectCategory = ImagedObjectCategory.LIVING_HUMAN_BEING;
                this.prefillData();
                break;    
            }
            case 'edit': {
                this.loadAllStudies();
                this.fetchSubject();
                break;
            }
            case 'view': {
                this.fetchSubject();
                break;
            }   
        }
        this.canModify = this.keycloakService.isUserAdmin() || this.keycloakService.isUserExpert();
    }
    
    private chooseMode(): Promise<void> {
        return new Promise((resolve, reject) => {
            if (this.mode == null) {
                this.route.queryParams
                .filter(params => params.mode)
                .subscribe(params => {
                    if (!params.mode) {
                        throw new Error("a mode parameter must be set");
                    }
                    this.mode = params.mode;
                    resolve();
                });
            } else {
                resolve();
            }
        });
    }

    public setSubject(subject: Subject) {
        this.subject = subject;
        console.log('this.subject : ', this.subject);
        this.buildForm();
    }
    
    prefillData() {
        if (this.preFillData) {
            if (this.preFillData.subject) {
                this.computeNameFromDicomTag(this.preFillData.subject.name);
                this.subject.sex = this.preFillData.subject.sex;
                this.getDateToDatePicker(this.preFillData.subject);  
            }
            if (this.preFillData.study) {
                let study : IdNameObject = new IdNameObject();
                study.id = this.preFillData.study.id;
                study.name = this.preFillData.study.name;
                this.studies = [];
                this.studies.push(study);
            }
        }
    }

    fetchSubject(): void {
        if (this.mode == 'create') throw new Error ("A subject cannot be fetch in create mode");
        this.route.queryParams
            .switchMap((queryParams: Params) => {
                if (queryParams['id']) {
                    return this.subjectService.getSubject(queryParams['id']);
                } else throw new Error ("A id must be passed as a parameter in edit mode");
            })
            .subscribe((subject: Subject) => {
                this.setSubject(subject);
                this.getDateToDatePicker(this.subject);
            });
    }

    loadAllStudies(): void {
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
            'studies': []
        });
        this.subjectStudyForm = this.fb.group({
            'subjectStudyList': [this.subject.subjectStudyList]
        })

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
        for (const field in this.formErrors) {
            // clear previous error message (if any)
            this.formErrors[field] = '';
            const control = this.subjectForm.get(field);
            if (control && control.dirty && !control.valid) {
                for (const key in control.errors) {
                    this.formErrors[field] += key;
                }
            }
        }
    }

    updateModel(): void {
        this.subject = this.subjectForm.value;
        this.subject.subjectStudyList = this.subjectStudyForm.value;
        this.setDateFromDatePicker();
    }




    // No
    back(subject?: Subject): void {
        if (this.closing.observers.length > 0) {
            this.closing.emit(subject);
        } else {
            this.location.back();
        }
    }


    edit(): void {
        this.router.navigate(['/subject'], { queryParams: { id: this.subject.id, mode: "edit" } });
    }

    create(): void {
        this.updateModel();
        this.generateSubjectIdentifier();
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
        this.updateModel();
        for (let subjectStudy of this.subject.subjectStudyList) {
            if (this.subjectService.findSubjectStudyById(subjectStudy.id)) {
                this.subjectService.updateSubjectStudy(subjectStudy);
            } else {
                this.subjectService.createSubjectStudy(subjectStudy);
            }
        }
        this.subjectService.update(this.subject.id, this.subject)
            .subscribe((subject) => {
                this.back();
            }, (err: string) => {
                this.manageRequestErrors(err);
            });
    }

    private manageRequestErrors(err: string): void {
        if (err.indexOf("name should be unique") != -1) {
            this.formErrors['name'] = 'unique';
        }
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

    generateSubjectIdentifier(): void {
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

    removeSubjectStudy(subjectStudy: SubjectStudy):void {
        const index: number = this.subject.subjectStudyList.indexOf(subjectStudy);
        if (index !== -1) {
            this.subject.subjectStudyList.splice(index, 1);
        }
        this.subjectService.deleteSubjectStudy(subjectStudy.id);
    }

    onStudySelectChange(studyId: number) {
        var newSubjectStudy: SubjectStudy = new SubjectStudy();
        newSubjectStudy.physicallyInvolved = false;
        newSubjectStudy.studyId = studyId;

        if (this.subject.subjectStudyList != null)
            this.subject.subjectStudyList.push(newSubjectStudy);
        else {
            this.subject.subjectStudyList = [];
            this.subject.subjectStudyList.push(newSubjectStudy);
        } 

        // I want to do something here for new selectedDevice, but what I
        // got here is always last selection, not the one I just select.
    }

    /**
     * Try to compute patient first name and last name from dicom tags. 
     * eg. TOM^HANKS -> return TOM as first name and HANKS as last name
     */
    computeNameFromDicomTag (patientName: string): void {
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
            && (this.subject.imagedObjectCategory.toString() == 'HUMAN_CADAVER'
                || this.subject.imagedObjectCategory.toString() == 'LIVING_HUMAN_BEING');
    }

    public imagedObjectCategories() {
        return ImagedObjectCategory.keys();
    }

    public subjectTypes() {
        return SubjectType.keyValues();
    }
    
}