import { Component, OnInit, Input, Output, EventEmitter, OnChanges, SimpleChanges } from '@angular/core';
import { Location } from '@angular/common';
import { ActivatedRoute, Router, Params } from '@angular/router';
import { Observable } from 'rxjs/Observable';
import { FormGroup, FormBuilder, Validators, FormControl, FormArray, ValidationErrors } from '@angular/forms';

import * as AppUtils from '../../utils/app.utils';
import * as shajs from 'sha.js';
import { ImagedObjectCategory } from '../shared/imaged-object-category.enum';
import { IdNameObject } from '../../shared/models/id-name-object.model';
import { ImagesUrlUtil } from '../../shared/utils/images-url.util';
import { KeycloakService } from "../../shared/keycloak/keycloak.service";
import { MsgBoxService } from '../../shared/msg-box/msg-box.service';
import { slideDown, preventInitialChildAnimations} from '../../shared/animations/animations';
import { Study } from '../../studies/shared/study.model';
import { Subject } from '../shared/subject.model';
import { SubjectService } from '../shared/subject.service';
import { SubjectStudy } from '../shared/subject-study.model';
import { StudyService } from '../../studies/shared/study.service';

@Component({
    selector: 'subject-detail',
    templateUrl: 'subject.component.html',
    styleUrls: ['subject.component.css'],
    animations: [slideDown, preventInitialChildAnimations]
})

export class SubjectComponent implements OnInit, OnChanges {
    
    private readonly ImagesUrlUtil = ImagesUrlUtil;
    private readonly ImagedObjectCategory = ImagedObjectCategory;
    private readonly HASH_LENGTH: number = 14;
    
    @Input() mode: "view" | "edit" | "create";
    @Input() preFillData: Subject;
    @Output() closing: EventEmitter<any> = new EventEmitter();
    
    private subject: Subject;
    private subjectForm: FormGroup;
    private canModify: Boolean = false;
    private firstName: string = "";
    private lastName: string = "";
    private studies: IdNameObject[];
    private subjectStudyList: SubjectStudy[] = [];
    private isBirthDateValid: boolean = true;
    private isAlreadyAnonymized: boolean;
    private init: boolean = false;
    private hasNameUniqueError: boolean = false;

    constructor(private route: ActivatedRoute, private router: Router,
        private subjectService: SubjectService,
        private studyService: StudyService,
        private fb: FormBuilder,
        private location: Location, 
        private keycloakService: KeycloakService,
        private msgService: MsgBoxService) {
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
                this.initPrefillData();
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
    
    initPrefillData() {
        if (this.preFillData && this.subject) {
            if (this.preFillData) {
                this.computeNameFromDicomTag(this.preFillData.name);
                this.subject.sex = this.preFillData.sex;
                this.subject.birthDate = new Date(this.preFillData.birthDate);
            }
            if (this.preFillData.subjectStudyList && this.preFillData.subjectStudyList.length > 0) {
                this.subjectStudyList = this.preFillData.subjectStudyList;
                this.studies = [];
                for (let subjectStudy of this.preFillData.subjectStudyList) {
                    this.studies.push(new IdNameObject(subjectStudy.study.id, subjectStudy.study.name));
                }
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
            })
            .catch((error) => {
                // TODO: display error
                console.error("error getting study list!");
            });
    }

    ngOnChanges(changes: SimpleChanges) {
        if (changes['preFillData']) this.initPrefillData();
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
    }

    back(subject?: Subject): void {
        if (this.closing.observers.length > 0) {
            this.closing.emit(subject);
            this.subject = new Subject();
            this.subject.imagedObjectCategory = ImagedObjectCategory.LIVING_HUMAN_BEING;
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
        // Anonymization only for human subject
        if (this.humanSelected()) {
            this.setSubjectBirthDateToFirstOfJanuary();
        }
        this.subjectService.create(this.subject)
            .subscribe((subject: Subject) => {
                this.msgService.log('info', 'Subject successfully created');
                this.back(subject);
            }, (error: any) => {
                this.manageRequestErrors(error);
            });
        }

    update(): void {
        this.updateModel();
        this.subjectService.update(this.subject.id, this.subject)
            .subscribe((subject) => {
                this.msgService.log('info', 'Subject successfully updated');
                this.back();
            }, (error: any) => {
                this.manageRequestErrors(error);
        });
    }

    private manageRequestErrors(error: any): void {
        this.hasNameUniqueError = AppUtils.hasUniqueError(error, 'name');
    }

    generateSubjectIdentifier(): void {
        let hash;
        if (this.humanSelected() && !this.isAlreadyAnonymized) {
            hash = this.firstName + this.lastName + this.subject.birthDate;
        }
        else {
            hash = this.subject.name + this.subject.birthDate;
        }
        this.subject.identifier = this.getHash(hash);
    }

    getHash(stringToBeHashed: string): string {
        let hash = shajs('sha').update(stringToBeHashed).digest('hex');
        let hex = "";
        hex = hash.substring(0, this.HASH_LENGTH);
        return hex;
    }

    setSubjectBirthDateToFirstOfJanuary(): void {
        let newDate: Date = new Date(this.subject.birthDate.getFullYear(), 0, 1);
        this.subject.birthDate = newDate;
    }

    private onChangeSubjectStudyList(subjectStudyList: SubjectStudy[]) {
        this.subjectStudyList = subjectStudyList;
    }

    /**
     * Try to compute patient first name and last name from dicom tags. 
     * eg. TOM^HANKS -> return TOM as first name and HANKS as last name
     */
    computeNameFromDicomTag (patientName: string): void {
        if (patientName) {
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
}