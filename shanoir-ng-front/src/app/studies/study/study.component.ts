import { Location } from '@angular/common';
import { AfterViewInit, Component, OnInit, ViewChild } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';

import { Center } from '../../centers/shared/center.model';
import { CenterService } from '../../centers/shared/center.service';
import { slideDown } from '../../shared/animations/animations';
import { FooterState } from '../../shared/components/form-footer/footer-state.model';
import { BrowserPaging } from '../../shared/components/table/browser-paging.model';
import { FilterablePageable, Page } from '../../shared/components/table/pageable.model';
import { TableComponent } from '../../shared/components/table/table.component';
import { KeycloakService } from '../../shared/keycloak/keycloak.service';
import { IdNameObject } from '../../shared/models/id-name-object.model';
import { MsgBoxService } from '../../shared/msg-box/msg-box.service';
import { Enum } from '../../shared/utils/enum';
import { SubjectStudy } from '../../subjects/shared/subject-study.model';
import { SubjectService } from '../../subjects/shared/subject.service';
import { User } from '../../users/shared/user.model';
import { UserService } from '../../users/shared/user.service';
import * as AppUtils from '../../utils/app.utils';
import { StudyCenter } from '../shared/study-center.model';
import { StudyStatus } from '../shared/study-status.enum';
import { StudyUserType } from '../shared/study-user-type.enum';
import { StudyUser } from '../shared/study-user.model';
import { Study } from '../shared/study.model';
import { StudyService } from '../shared/study.service';
import { Timepoint } from '../shared/timepoint.model';

declare type Mode = 'create' | 'edit' | 'view';

@Component({
    selector: 'study-detail',
    templateUrl: 'study.component.html',
    styleUrls: ['study.component.css'],
    animations: [slideDown]
})

export class StudyComponent implements OnInit, AfterViewInit {
    
    private id: number;
    private mode: Mode;
    private centers: Center[];
    private isEndDateValid: boolean = true;
    private isNameUnique: Boolean = true;
    private isStartDateValid: boolean = true;
    private selectedCenter: Center;
    private study: Study = new Study();
    public studyForm: FormGroup;
    private studyStatuses: Enum[] = [];
    private subjects: IdNameObject[];
    private hasNameUniqueError: boolean = false;
    private browserPaging: BrowserPaging<StudyUser>;
    private columnDefs: any[];
    private users: User[] = [];
    private footerState: FooterState;
    
    private studyUsersPromise: Promise<any>;
    private usersPromise: Promise<User[]>;
    private studyPromise: Promise<Study>;

    @ViewChild('memberTable') table: TableComponent;
    
    formErrors = {
        'name': '',
        'studyStatus': '',
        'studyCenterList': ''
    };
    
    constructor(
            private route: ActivatedRoute, 
            private router: Router,
            private centerService: CenterService, 
            private studyService: StudyService, 
            private fb: FormBuilder,
            private location: Location, 
            private keycloakService: KeycloakService, 
            private subjectService: SubjectService,
            private msgService: MsgBoxService, 
            private userService: UserService) {

        this.mode = this.route.snapshot.data['mode'];
        this.id = +this.route.snapshot.params['id'];
     }
        
    ngOnInit(): void {
        this.getEnum();
        this.getCenters();
        this.getSubjects();
        this.usersPromise = this.userService.getUsers();
        this.createColumnDefs();
        this.studyPromise = this.fetchStudy();
        this.studyUsersPromise = this.studyPromise.then(study => {
            this.browserPaging = new BrowserPaging(study.studyUserList, this.columnDefs);
        });
        this.usersPromise.then(users => this.users = users);
        this.buildForm(new Study());
        this.footerState = new FooterState(this.mode, this.keycloakService.isUserAdminOrExpert());
    }

    ngAfterViewInit() {
        this.studyPromise.then(study => this.buildForm(study));
    }
    
    addCenterToStudy(): void {
        let studyCenter: StudyCenter = new StudyCenter();
        studyCenter.center = this.selectedCenter;
        this.study.studyCenterList.push(studyCenter);
    }
    
    back(): void {
        this.location.back();
    }
    
    buildForm(study: Study): void {
        this.studyForm = this.fb.group({
            'name': [study.name, [Validators.required, Validators.minLength(2), Validators.maxLength(200)]],
            'startDate': [study.startDate],
            'endDate': [study.endDate],
            'studyStatus': [study.studyStatus, [Validators.required]],
            'withExamination': [study.withExamination],
            'clinical': [study.clinical, [Validators.required]],
            'visibleByDefault': [study.visibleByDefault],
            'downloadableByDefault': [study.downloadableByDefault],
            'monoCenter': [study.monoCenter, [Validators.required]],
            'studyCenterList': [study.studyCenterList]
        });
        this.studyForm.valueChanges.subscribe(data => this.onValueChanged(data));
        this.onValueChanged();
        this.studyForm.statusChanges.subscribe(status => this.footerState.valid = status == 'VALID');
    }
    
    edit(): void {
        this.router.navigate(['/study/edit/'+this.study.id]); 
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

    private fetchStudy(): Promise<Study> {
        if (this.mode == 'create') {
            return new Promise(resolve => {
                this.study = new Study();
                resolve(this.study); 
            });
        } else {
            return Promise.all([
                this.studyService.getStudy(this.id),
                this.usersPromise
            ]).then(([study, users]) => {
                Study.completeMembers(study, users);
                this.study = study;
                return this.study;
            })
        }
    }
        
    newStudy(): Study {
        let study: Study = new Study();
        study.clinical = false;
        study.monoCenter = true;
        study.studyCenterList = [];
        study.timepoints = [];
        study.withExamination = true;
        return study;
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
        this.studyService.update(this.id, this.study)
            .subscribe((study: Study) => {
                this.back();
                this.msgService.log('info', 'Study successfully updated');
            }, (error: any) => {
                this.manageRequestErrors(error);
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
        const allStudyUserTypes: any[] = [
            { value: StudyUserType.NOT_SEE_DOWNLOAD, label: "Cannot see or download datasets" },
            { value: StudyUserType.RESPONSIBLE, label: "Is responsible for the research study" },
            { value: StudyUserType.SEE_DOWNLOAD, label: "Can see and download datasets" },
            { value: StudyUserType.SEE_DOWNLOAD_IMPORT, label: "Can see, download and import datasets" },
            { value: StudyUserType.SEE_DOWNLOAD_IMPORT_MODIFY, label: "Can see, download, import datasets and modify the study parameters" },
        ];

        this.columnDefs = [
            { headerName: "Username", field: "userName" },
            { headerName: "First Name", field: "firstName" },
            { headerName: "Last Name", field: "lastName" },
            { headerName: "Email", field: "email", width: "200%" },
            { headerName: "Role", field: "role.displayName", width: "63px" },
            { headerName: "Role/Position*", field: "studyUserType", editable: true, possibleValues: StudyUserType.all(), width: "300%" },
            { headerName: "Received Import Mail", field: "receiveNewImportReport", editable: true },
            { headerName: "Received Anonymization Mail", field: "receiveAnonymizationReport", editable: true },
            { headerName: "", type: "button", awesome: "fa-trash", action: this.removeStudyUser }
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
        studyUser.completeMember(this.users);
        this.study.studyUserList.push(studyUser);
        this.browserPaging.setItems(this.study.studyUserList);
        this.table.refresh();
    }

    private removeStudyUser = (item: StudyUser) => {
        const index: number = this.study.studyUserList.indexOf(item);
        item.user.selected = false;
        if (index !== -1) {
            this.study.studyUserList.splice(index, 1);
        }
        this.browserPaging.setItems(this.study.studyUserList);
        this.table.refresh();
    }
}