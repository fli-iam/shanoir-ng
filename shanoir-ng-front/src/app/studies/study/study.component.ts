import { Component, OnInit } from '@angular/core';
import { Location } from '@angular/common';
import { ActivatedRoute, Router, Params } from '@angular/router';
import { Observable } from 'rxjs/Observable';
import { FormGroup, FormBuilder, Validators } from '@angular/forms';

import * as AppUtils from '../../utils/app.utils';
import { Center } from '../../centers/shared/center.model';
import { CenterService } from '../../centers/shared/center.service';
import { Enum } from "../../shared/utils/enum";
import { KeycloakService } from "../../shared/keycloak/keycloak.service";
import { ImagesUrlUtil } from '../../shared/utils/images-url.util';
import { Study } from '../shared/study.model';
import { StudyCenter } from '../shared/study-center.model';
import { StudyService } from '../shared/study.service';
import { StudyStatus } from "../shared/study-status.enum";
import { Timepoint } from '../shared/timepoint.model';
import { slideDown } from '../../shared/animations/animations';
import { SubjectStudy } from '../../subjects/shared/subject-study.model';
import { SubjectService } from '../../subjects/shared/subject.service';
import { IdNameObject } from '../../shared/models/id-name-object.model';
import { MsgBoxService } from '../../shared/msg-box/msg-box.service';
import { StudyUser } from '../shared/study-user.model';
import { FilterablePageable, Page } from '../../shared/components/table/pageable.model';
import { BrowserPaging } from '../../shared/components/table/browser-paging.model';

@Component({
    selector: 'study-detail',
    templateUrl: 'study.component.html',
    styleUrls: ['study.component.css'],
    animations: [slideDown]
})

export class StudyComponent implements OnInit {

    private addIconPath: string = ImagesUrlUtil.ADD_ICON_PATH;
    private addDisabledIconPath: string = ImagesUrlUtil.ADD_DISABLED_ICON_PATH;
    public canModify: Boolean = false;
    private centers: Center[];
    private deleteIconPath: string = ImagesUrlUtil.DELETE_ICON_PATH;
    private editIconPath: string = ImagesUrlUtil.EDIT_ICON_PATH;
    private isEndDateValid: boolean = true;
    private isNameUnique: Boolean = true;
    private isStartDateValid: boolean = true;
    private loaderImagePath: string = ImagesUrlUtil.LOADER_IMAGE_PATH;
    private loading: boolean = true;
    public mode: "view" | "edit" | "create";
    private selectedCenter: Center;
    public study: Study = new Study();
    public studyForm: FormGroup;
    private studyId: number;
    private studyStatusEnumValue: string;
    private studyStatuses: Enum[] = [];
    private subjectStudyList: SubjectStudy[] = [];
    private subjects: IdNameObject[];
    private hasNameUniqueError: boolean = false;

    private studyUsersPromise: Promise<void>;
    private browserPaging: BrowserPaging<StudyUser>;
    private columnDefs: any[];
    private customActionDefs: any[];

    formErrors = {
        'name': '',
        'studyStatus': '',
        'studyCenterList': ''
    };

    constructor(private route: ActivatedRoute, private router: Router,
        private centerService: CenterService, private studyService: StudyService, private fb: FormBuilder,
        private location: Location, private keycloakService: KeycloakService, private subjectService: SubjectService,
        private msgService: MsgBoxService) {

    }

    ngOnInit(): void {
        this.getEnum();
        this.getCenters();
        this.getSubjects();
        this.getStudy();
        this.buildForm();
        if (this.keycloakService.isUserAdmin() || this.keycloakService.isUserExpert()) {
            this.canModify = true; 
        }
    }

    addCenterToStudy(): void {
        let studyCenter: StudyCenter = new StudyCenter();
        studyCenter.center = this.selectedCenter;
        this.study.studyCenterList.push(studyCenter);
    }

    back(): void {
        this.location.back();
    }

    buildForm(): void {
        this.studyForm = this.fb.group({
            'name': [this.study.name, [Validators.required, Validators.minLength(2), Validators.maxLength(200)]],
            'startDate': [this.study.startDate],
            'endDate': [this.study.endDate],
            'studyStatus': [this.study.studyStatus, [Validators.required]],
            'withExamination': [this.study.withExamination],
            'clinical': [this.study.clinical, [Validators.required]],
            'visibleByDefault': [this.study.visibleByDefault],
            'downloadableByDefault': [this.study.downloadableByDefault],
            'monoCenter': [this.study.monoCenter, [Validators.required]],
            'studyCenterList': [this.study.studyCenterList]
        });
        this.studyForm.valueChanges
            .subscribe(data => this.onValueChanged(data));
        this.onValueChanged();
    }

    edit(): void {
        this.mode = 'edit';
        this.createColumnDefs();
        this.studyUsersPromise = this.getMembers(this.study.id);
    }

    editTimepoint(timepoint: Timepoint): void {  
        // TODO
    }

    enableAddIcon(): boolean {
        if ((this.study.monoCenter && !this.studyCenterListEmpty())
            || (this.selectedCenter && this.isCenterAlreadyLinked(this.selectedCenter.id))) {
            return false;
        }
        return true;
    }

    getCenters(): void {
        this.centerService
            .getCentersNames()
            .then(centers => {
                this.centers = centers;
                if (centers) {
                    this.selectedCenter = centers[0];
                }
            })
            .catch((error) => {
                // TODO: display error
                console.log("error getting center list!");
            });
    }

    getSubjects(): void {
        this.subjectService
            .getSubjectsNames()
            .then(subjects => {
                this.subjects = subjects;
            })
            .catch((error) => {
                // TODO: display error
                console.log("error getting subjects list!");
            });
    }

    getEnum(): void {
        var types = Object.keys(StudyStatus);
        for (var i = 0; i < types.length; i = i + 2) {
            var newEnum: Enum = new Enum();
            newEnum.key = types[i];
            newEnum.value = StudyStatus[types[i]];
            this.studyStatuses.push(newEnum);
        }
    }

    getStudy(): void {
        this.route.queryParams
            .switchMap((queryParams: Params) => {
                let studyId = queryParams['id'];
                let mode = queryParams['mode'];
                if (mode) {
                    this.mode = mode;
                }
                if (studyId) {
                    // view or edit mode
                    this.studyId = studyId;
                    this.loading = true;
                    return this.studyService.getStudy(studyId);
                } else {
                    // create mode
                    this.initializeStudyData();
                    return Observable.of<Study>();
                }
            })
            .subscribe((study: Study) => {
                this.study = study;
                this.studyStatusEnumValue = StudyStatus[this.study.studyStatus];
                this.loading = false;
            }, (error: any) => {
                this.loading = false;
            });
    }

    initializeStudyData(): void {
        this.study.clinical = false;
        this.study.monoCenter = true;
        this.study.studyCenterList = [];
        this.study.timepoints = [];
        this.study.withExamination = true;
    }

    isCenterAlreadyLinked(centerId: number): boolean {
        if (!this.study.studyCenterList) return false;
        for (let studyCenter of this.study.studyCenterList) {
            if (centerId == studyCenter.center.id) {
                return true;
            }
        }
        return false;
    }

    onValueChanged(data?: any) {
        if (!this.studyForm) { return; }
        const form = this.studyForm;
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

    removeCenterFromStudy(centerId: number): void {
        this.study.studyCenterList = this.study.studyCenterList.filter(item => item.center.id !== centerId);
        if (this.study.studyCenterList.length < 2) {
            this.study.monoCenter = true;
        }
    }

    removeTimepoint(timepoint: Timepoint): void {
        const index: number = this.study.timepoints.indexOf(timepoint);
        if (index !== -1) {
            this.study.timepoints.splice(index, 1);
        }
    }

    studyCenterListEmpty(): boolean {
        if (this.study.studyCenterList && this.study.studyCenterList.length > 0) {
            return false;
        }
        return true;
    }

    studyCenterListHasMultipleElements(): boolean {
        return this.study.studyCenterList.length > 1;
    }

    private manageRequestErrors(error: any): void {
        this.hasNameUniqueError = AppUtils.hasUniqueError(error, 'name');
    }

    submit(): void {
        let studyCenterListBackup: StudyCenter[] = this.study.studyCenterList;
        this.study = this.studyForm.value;
        this.study.studyCenterList = studyCenterListBackup;
        this.study.subjectStudyList = this.subjectStudyList;
    }

    create(): void {
        this.submit();
        this.studyService.create(this.study)
            .subscribe((study: Study) => {
                this.back();
                this.msgService.log('info', 'Study successfully created');
            }, (error: any) => {
                this.manageRequestErrors(error);
            });
    }

    update(): void {
        this.submit();
        this.studyService.update(this.studyId, this.study)
            .subscribe((study: Study) => {
                this.back();
                this.msgService.log('info', 'Study successfully updated');
            }, (error: any) => {
                this.manageRequestErrors(error);
            });
    }

    private onChangeSubjectStudyList(subjectStudyList: SubjectStudy[]) {
        this.subjectStudyList = subjectStudyList;
    }


    private getMembers(studyId: number): Promise<void> {
        return this.studyService.findMembers(this.studyId)
            .then((studyUserList: StudyUser[]) => {
                this.browserPaging = new BrowserPaging(studyUserList, this.columnDefs);
            });
    }

    getPage(pageable: FilterablePageable): Promise<Page<StudyUser>> {
        return new Promise((resolve) => {
            this.studyUsersPromise.then(() => {
                resolve(this.browserPaging.getPage(pageable));
            });
        });
    }

    // Grid columns definition
    private createColumnDefs() {
        this.columnDefs = [
            {headerName: "Username", field: "userName", width: "100%"},
            // {headerName: "First Name", field: ""},
            // {headerName: "Last Name", field: ""},
            // {headerName: "Email", field: "", width: "200%"},
            // {headerName: "Role", field: "", width: "63px"},
            {headerName: "Role/Position", field: "studyUserType", width: "100%"},
            {headerName: "Receive Import Mail", field: "receiveNewImportReport", type: "boolean", width: "100%"},
            {headerName: "Receive Anonymization Mail", field: "receiveAnonymizationReport", type: "boolean", width: "100%"},
            {headerName: "Delete", type: "button", img: ImagesUrlUtil.DELETE_ICON_PATH, target : "/user", getParams: function(item: any): Object {
                return {id: item.id};
            }}
        ];
        this.customActionDefs = [
            {title: "Add member", img: ImagesUrlUtil.ADD_ICON_PATH, target: "../user"},
        ];
    }
}