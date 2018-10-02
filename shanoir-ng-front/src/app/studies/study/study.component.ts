import { Component, ViewChild } from '@angular/core';
import { FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';

import { Center } from '../../centers/shared/center.model';
import { CenterService } from '../../centers/shared/center.service';
import { slideDown } from '../../shared/animations/animations';
import { EntityComponent } from '../../shared/components/entity/entity.component.abstract';
import { BrowserPaging } from '../../shared/components/table/browser-paging.model';
import { FilterablePageable, Page } from '../../shared/components/table/pageable.model';
import { TableComponent } from '../../shared/components/table/table.component';
import { IdNameObject } from '../../shared/models/id-name-object.model';
import { SubjectService } from '../../subjects/shared/subject.service';
import { User } from '../../users/shared/user.model';
import { UserService } from '../../users/shared/user.service';
import { StudyCenter } from '../shared/study-center.model';
import { StudyUserType } from '../shared/study-user-type.enum';
import { StudyUser } from '../shared/study-user.model';
import { Study } from '../shared/study.model';
import { StudyService } from '../shared/study.service';
import { ShanoirError } from '../../shared/models/error.model';

declare type Mode = 'create' | 'edit' | 'view';

@Component({
    selector: 'study-detail',
    templateUrl: 'study.component.html',
    styleUrls: ['study.component.css'],
    animations: [slideDown]
})

export class StudyComponent extends EntityComponent<Study> {
    
    @ViewChild('memberTable') table: TableComponent;

    private centers: Center[];
    private subjects: IdNameObject[];
    private selectedCenter: Center;
    private isNameUniqueError: boolean = false;
    
    private browserPaging: BrowserPaging<StudyUser>;
    private columnDefs: any[];
    private users: User[] = [];
    
    private studyUsersPromise: Promise<any>;

    constructor(
            private route: ActivatedRoute, 
            private centerService: CenterService, 
            private studyService: StudyService, 
            private subjectService: SubjectService,
            private userService: UserService) {

        super(route, 'study');
    }

    public get study(): Study { return this.entity; }
    public set study(study: Study) { this.entity = study; }

    initView(): Promise<void> {
        return this.studyService.get(this.id).then(study => {this.study = study}); 
    }

    initEdit(): Promise<void> {
        let studyPromise: Promise<Study> = this.studyService.get(this.id).then(study => this.study = study);
        this.getCenters();
        this.getSubjects();

        this.createColumnDefs();
        this.studyUsersPromise = studyPromise.then(study => {
            this.browserPaging = new BrowserPaging(study.studyUserList, this.columnDefs);
        });

        Promise.all([
            studyPromise,
            this.userService.getUsers().then(users => this.users = users)
        ]).then(([study, users]) => {
            Study.completeMembers(study, users);
        })
        return studyPromise.then(() => null);
    }

    initCreate(): Promise<void> {
        this.study = this.newStudy();
        this.getCenters();
        this.getSubjects();

        this.createColumnDefs();
        this.studyUsersPromise = Promise.resolve().then(() => {
            this.browserPaging = new BrowserPaging(this.study.studyUserList, this.columnDefs);
        });

        this.userService.getUsers().then(users => this.users = users);
        return Promise.resolve();
    }

    buildForm(): FormGroup {
        return this.formBuilder.group({
            'name': [this.study.name, [Validators.required, Validators.minLength(2), Validators.maxLength(200)]],
            'startDate': [this.study.startDate],
            'endDate': [this.study.endDate],
            'studyStatus': [this.study.studyStatus, [Validators.required]],
            'withExamination': [this.study.withExamination],
            'clinical': [this.study.clinical, [Validators.required]],
            'visibleByDefault': [this.study.visibleByDefault],
            'downloadableByDefault': [this.study.downloadableByDefault],
            'monoCenter': [{value: this.study.monoCenter, disabled: this.study.studyCenterList && this.study.studyCenterList.length > 1}, [Validators.required]],
            'studyCenterList': [this.study.studyCenterList]
        });
    }

    private newStudy(): Study {
        let study: Study = new Study();
        study.clinical = false;
        study.monoCenter = true;
        study.studyCenterList = [];
        study.timepoints = [];
        study.withExamination = true;
        return study;
    }

    private getCenters(): void {
        this.centerService
        .getCentersNames()
        .then(centers => {
            this.centers = centers;
            if (centers) {
                this.selectedCenter = centers[0];
            }
        });
    }
        
    private getSubjects(): void {
        this.subjectService
            .getSubjectsNames()
            .then(subjects => {
                this.subjects = subjects;
        });
    }

    private manageSaveErrors() {
        this.subscribtions.push(
            this.onSave.subscribe(response => {
                if (response && response instanceof ShanoirError && response.code == 422) {
                    this.isNameUniqueError = response.hasFieldError('name', 'unique'); 
                }
            })
        );
    }

    private get disableMono(): boolean {
        return this.study.studyCenterList && this.study.studyCenterList.length > 1;
    }

    /** Center section management  **/

    private onCenterAdd(): void {
        let studyCenter: StudyCenter = new StudyCenter();
        studyCenter.center = this.selectedCenter;
        this.study.studyCenterList.push(studyCenter);
    }

    private onCenterChange(): void {
        if (this.study.monoCenter) {
            this.study.studyCenterList = []
            this.onCenterAdd();
        }
    }

    private removeCenterFromStudy(centerId: number): void {
        this.study.studyCenterList = this.study.studyCenterList.filter(item => item.center.id !== centerId);
        if (this.study.studyCenterList.length < 2) {
            this.study.monoCenter = true;
        }
    }
    
    private enableAddIcon(): boolean {
        return this.selectedCenter && !this.isCenterAlreadyLinked(this.selectedCenter.id)
            && (!this.study.monoCenter || !this.study.studyCenterList || this.study.studyCenterList.length == 0);
    }    

    private isCenterAlreadyLinked(centerId: number): boolean {
        if (!this.study.studyCenterList) return false;
        for (let studyCenter of this.study.studyCenterList) {
            if (centerId == studyCenter.center.id) {
                return true;
            }
        }
        return false;
    }
    
    
    /** StudyUser management **/

    getPage(pageable: FilterablePageable): Promise<Page<StudyUser>> {
        return new Promise((resolve) => {
            this.studyUsersPromise.then(() => {
                resolve(this.browserPaging.getPage(pageable));
            });
        });
    }
        
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
            { headerName: "First Name", field: "user.firstName" },
            { headerName: "Last Name", field: "user.lastName" },
            { headerName: "Email", field: "user.email", width: "200%" },
            { headerName: "Role", field: "user.role.displayName", width: "63px" },
            { headerName: "Role/Position*", field: "studyUserType", editable: true, possibleValues: allStudyUserTypes, width: "300%"},
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


        
    // removeTimepoint(timepoint: Timepoint): void {
    //     const index: number = this.study.timepoints.indexOf(timepoint);
    //     if (index !== -1) {
    //         this.study.timepoints.splice(index, 1);
    //     }
    // }

}