import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { Location } from '@angular/common';
import { ActivatedRoute, Router, Params } from '@angular/router';
import { Observable } from 'rxjs/Observable';
import { FormGroup, FormBuilder, Validators, FormControl, FormArray, ValidationErrors } from '@angular/forms';

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
    public canModify: Boolean = false;
    private firstName: string = "";
    private lastName: string = "";
    private ImagedObjectCategory = ImagedObjectCategory;

    @Output() closing: EventEmitter<any> = new EventEmitter();

    public studies: IdNameObject[];
    public studyIdNameMap: Map<number, String> = new Map<number, String>();
    public subjectStudyList: SubjectStudy[] = [];
    private isBirthDateValid: boolean = true;
    private isAlreadyAnonymized: boolean;
    private hashLength: number = 14;

    private init: boolean = false;

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
                this.subject = new Subject();
                this.subject.imagedObjectCategory = ImagedObjectCategory.LIVING_HUMAN_BEING;
                this.buildForm();
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
        this.buildForm();
    }
    
    prefillData() {
        if (this.preFillData) {
            if (this.preFillData.subject) {
                this.computeNameFromDicomTag(this.preFillData.subject.name);
                this.subject.sex = this.preFillData.subject.sex;
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
            });
    }

    loadAllStudies(): void {
        this.studyService
            .getStudiesNames()
            .then(studies => {
                this.studies = studies;
                for (let study of this.studies) {
                    this.studyIdNameMap.set(study.id, study.name);
                }
            })
            .catch((error) => {
                // TODO: display error
                console.error("error getting study list!");
            });
    }

    buildForm(): void {
        let firstNameFC, lastNameFC, birthDateFC : FormControl;
        if (this.subject.imagedObjectCategory == ImagedObjectCategory.LIVING_HUMAN_BEING) {
            firstNameFC = new FormControl(this.firstName, [Validators.required, Validators.minLength(2), Validators.maxLength(64)]);
            lastNameFC = new FormControl(this.lastName, [Validators.required, Validators.minLength(2), Validators.maxLength(64)]);
            birthDateFC = new FormControl(this.subject.birthDate, [Validators.required]);
        } else {
            firstNameFC = new FormControl(this.firstName);
            lastNameFC = new FormControl(this.lastName);
            birthDateFC = new FormControl(this.subject.birthDate);
        }

        this.subjectForm = this.fb.group({
            'imagedObjectCategory': [this.subject.imagedObjectCategory, [Validators.required]],
            'isAlreadyAnonymized': new FormControl('No'),
            'name': [this.subject.name, [Validators.required, Validators.minLength(2), Validators.maxLength(64)]],
            'firstName': firstNameFC,
            'lastName': lastNameFC,
            'birthDate': birthDateFC,
            'sex': [this.subject.sex],
            'manualHemisphericDominance': [this.subject.manualHemisphericDominance],
            'languageHemisphericDominance': [this.subject.languageHemisphericDominance],
            'personalComments': []
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

    formErrors = {
        'name': '',
        'imagedObjectCategory': '',
        'firstName': '',
        'lastName': '',
        'birthDate': ''
    }

    updateModel(): void {
        this.subject = this.subjectForm.value;
        this.subject.subjectStudyList = this.subjectStudyList;
        console.log("updateModel - length" + this.subjectStudyList.length);
        console.log("updateModel - length2 : " + this.subject.subjectStudyList.length);
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
        if (this.subject.imagedObjectCategory == ImagedObjectCategory.LIVING_HUMAN_BEING) {
            this.setSubjectBirthDateToFirstOfJanuary();
        }
        this.subjectService.create(this.subject)
        .subscribe((subject) => {
                console.log("size: " + this.subject.subjectStudyList.length);
                for (let subjectStudy of this.subject.subjectStudyList) {
                    subjectStudy.subjectId = subject.id;
                    console.log("studyId: " + subjectStudy.studyId + ", subjectId: " + subjectStudy.subjectId + ", PI:" + subjectStudy.physicallyInvolved);
                    this.subjectService.createSubjectStudy(subjectStudy);
                }
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
        const index: number = this.subjectStudyList.indexOf(subjectStudy);
        if (index !== -1) {
            this.subjectStudyList.splice(index, 1);
        }
        // this.subjectService.deleteSubjectStudy(subjectStudy.id);
    }

    onStudySelectChange(study: any) {
        var newSubjectStudy: SubjectStudy = new SubjectStudy();
        newSubjectStudy.physicallyInvolved = false;
        newSubjectStudy.studyId = study.target.value;
        
        this.subjectStudyList.push(newSubjectStudy);
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
            && (this.subject.imagedObjectCategory == ImagedObjectCategory.HUMAN_CADAVER
                || this.subject.imagedObjectCategory == ImagedObjectCategory.LIVING_HUMAN_BEING);
    }

    public subjectTypes() {
        return SubjectType.keyValues();
    }
}