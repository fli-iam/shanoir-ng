import { Component, OnInit, ViewChild, SimpleChanges, OnChanges } from '@angular/core';
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
import { UserService } from '../../users/shared/user.service';
import { User } from '../../users/shared/user.model';
import { StudyUserType } from '../shared/study-user-type.enum';
import { TableComponent } from '../../shared/components/table/table.component';

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
    private subjects: IdNameObject[];
    private hasNameUniqueError: boolean = false;
    private studyUsersPromise: Promise<void>;
    private browserPaging: BrowserPaging<StudyUser>;
    private columnDefs: any[];
    private users: User[] = [];
    @ViewChild('memberTable') table: TableComponent;
    
    formErrors = {
        'name': '',
        'studyStatus': '',
        'studyCenterList': ''
    };
    
    constructor(private route: ActivatedRoute, private router: Router,
        private centerService: CenterService, private studyService: StudyService, private fb: FormBuilder,
        private location: Location, private keycloakService: KeycloakService, private subjectService: SubjectService,
        private msgService: MsgBoxService, private userService: UserService) {
            
    }
        
    ngOnInit(): void {
        this.getEnum();
        this.getCenters();
        this.getSubjects();
        this.getStudy();
        this.createColumnDefs();
        this.initStudyUser();
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
        this.router.navigate(['/study'], { queryParams: { id: this.study.id, mode: "edit" }});
        this.initStudyUser();
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
        });
    }
        
    getSubjects(): void {
        this.subjectService
            .getSubjectsNames()
            .then(subjects => {
                this.subjects = subjects;
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
        return this.study.studyCenterList && this.study.studyCenterList.length > 1;
    }
    
    private manageRequestErrors(error: any): void {
        this.hasNameUniqueError = AppUtils.hasUniqueError(error, 'name');
    }
    
    submit(): void {
        let studyCenterListBackup: StudyCenter[] = this.study.studyCenterList;
        let studyUserListBackup: StudyUser[] = this.study.studyUserList;
        let subjectStudyListBackup: SubjectStudy[] = this.study.subjectStudyList;
        this.study = this.studyForm.value;
        this.study.studyCenterList = studyCenterListBackup;
        this.study.studyUserList = studyUserListBackup;
        this.study.subjectStudyList = subjectStudyListBackup;
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

    private initStudyUser() {
        this.studyUsersPromise = this.getUsers().then(() => {
            this.completeMembers();
            this.browserPaging = new BrowserPaging(this.study.studyUserList, this.columnDefs);
        });
    }

    private completeMembers() {
        for (let studyUser of this.study.studyUserList) {
            for (let user of this.users) {
                if (studyUser.userId == user.id) {
                    studyUser.email = user.email;
                    studyUser.firstName = user.firstName;
                    studyUser.lastName = user.lastName;
                    studyUser.role = user.role;
                    user.selected = true;
                }
            }
        }
    }

    getPage(pageable: FilterablePageable): Promise<Page<StudyUser>> {
        return new Promise((resolve) => {
            this.studyUsersPromise.then(() => {
                resolve(this.browserPaging.getPage(pageable));
            });
        });
    }
    
    private getUsers(): Promise<void> {
        return this.userService.getUsers().then(users => {
            if (users) {
                this.users = users;
            }
        });
    }
        
    // Grid columns definition
    private createColumnDefs() {
        this.columnDefs = [
            { headerName: "Username", field: "userName" },
            { headerName: "First Name", field: "firstName" },
            { headerName: "Last Name", field: "lastName" },
            { headerName: "Email", field: "email", width: "200%" },
            { headerName: "Role", field: "role.displayName", width: "63px" },
            { headerName: "Role/Position*", field: "studyUserType", editable: true, possibleValues: StudyUserType.all(), width: "300%" },
            { headerName: "Received Import Mail", field: "receiveNewImportReport", editable: true },
            { headerName: "Received Anonymization Mail", field: "receiveAnonymizationReport", editable: true },
            { headerName: "", type: "button", img: ImagesUrlUtil.GARBAGE_ICON_PATH, action: this.removeStudyUser }
        ];
    }

    private onUserSelect(selectedUser: User) {
        selectedUser.selected = true;
        let studyUser: StudyUser = new StudyUser();
        studyUser.userId = selectedUser.id;
        studyUser.userName = selectedUser.username;
        studyUser.receiveAnonymizationReport = false;
        studyUser.receiveNewImportReport = false;
        studyUser.studyUserType = StudyUserType.NOT_SEE_DOWNLOAD;
        for (let user of this.users) {
            if (studyUser.userId == user.id) {
                studyUser.email = user.email;  
                studyUser.firstName = user.firstName;
                studyUser.lastName = user.lastName;
                studyUser.role = user.role;
            }
        }
        this.study.studyUserList.push(studyUser);
        this.table.refresh();
    }

    private removeStudyUser = (item: StudyUser) => {
        const index: number = this.study.studyUserList.indexOf(item);
        for (let user of this.users) {
            if (item.userId == user.id) {user.selected = false;}
        }
        if (index !== -1) {
            this.study.studyUserList.splice(index, 1);
        }
        this.table.refresh();
    }
}