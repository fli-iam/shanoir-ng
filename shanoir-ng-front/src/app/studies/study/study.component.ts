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
import { Component, ElementRef, ViewChild } from '@angular/core';
import { AbstractControl, FormGroup, ValidationErrors, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';

import { BidsElement } from '../../bids/model/bidsElement.model';
import { Center } from '../../centers/shared/center.model';
import { CenterService } from '../../centers/shared/center.service';
import { slideDown } from '../../shared/animations/animations';
import { EntityComponent } from '../../shared/components/entity/entity.component.abstract';
import { BrowserPaging } from '../../shared/components/table/browser-paging.model';
import { FilterablePageable, Page } from '../../shared/components/table/pageable.model';
import { TableComponent } from '../../shared/components/table/table.component';
import { DatepickerComponent } from '../../shared/date-picker/date-picker.component';
import { KeycloakService } from '../../shared/keycloak/keycloak.service';
import { IdName } from '../../shared/models/id-name.model';
import { Option } from '../../shared/select/select.component';
import { SubjectService } from '../../subjects/shared/subject.service';
import { DatasetNode, StudyNode } from '../../tree/tree.model';
import { User } from '../../users/shared/user.model';
import { UserService } from '../../users/shared/user.service';
import { capitalsAndUnderscoresToDisplayable } from '../../utils/app.utils';
import { StudyCenter } from '../shared/study-center.model';
import { StudyUserRight } from '../shared/study-user-right.enum';
import { StudyUser } from '../shared/study-user.model';
import { Study } from '../shared/study.model';
import { StudyService } from '../shared/study.service';



@Component({
    selector: 'study-detail',
    templateUrl: 'study.component.html',
    styleUrls: ['study.component.css'],
    animations: [slideDown]
})

export class StudyComponent extends EntityComponent<Study> {
    
    @ViewChild('memberTable') table: TableComponent;
    @ViewChild('input') private fileInput: ElementRef;

    private subjects: IdName[];
    private selectedCenter: IdName;
    
    private browserPaging: BrowserPaging<StudyUser>;
    private columnDefs: any[];
    private users: User[] = [];
    
    private studyUsersPromise: Promise<any>;
    private freshlyAddedMe: boolean = false;
    private studyUserBackup: StudyUser[] = [];
    protected protocolFile: File;

    protected selectedDatasetIds: number[];

    centerOptions: Option<IdName>[];
    userOptions: Option<User>[];
    studyStatusOptions: Option<string>[] = [
        new Option<string>('IN_PROGRESS', 'In Progress'),
        new Option<string>('FINISHED', 'Finished')
    ];

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
        this.getSubjects();

        this.createColumnDefs();
        this.studyUsersPromise = studyPromise.then(study => {
            this.browserPaging = new BrowserPaging(study.studyUserList, this.columnDefs);
        });

        Promise.all([
            studyPromise,
            this.fetchUsers()
        ]).then(([study, users]) => {
            Study.completeMembers(study, users);
            this.studyUserBackup = study.studyUserList ? study.studyUserList.map(a => Object.assign(new StudyUser, a)) : [];
            if (study.studyUserList) {
                study.studyUserList.forEach(studyUser => {
                    let option = this.userOptions.find(userOpt => userOpt.value.id == studyUser.user.id);
                    if (option) option.disabled = true;
                });
            }
        });
        
        Promise.all([
            studyPromise,
            this.getCenters()
        ]).then(([study, centers]) => {
            this.onMonoMultiChange();
        });
        return studyPromise.then(() => null);
    }

    async initCreate(): Promise<void> {
        this.study = this.newStudy();
        this.getCenters();
        this.selectedCenter = null;
        this.protocolFile = null;
        this.getSubjects();

        this.createColumnDefs();
        this.studyUsersPromise = Promise.resolve().then(() => {
            this.browserPaging = new BrowserPaging(this.study.studyUserList, this.columnDefs);
        });

        this.fetchUsers().then(users => {
            // Add the connected user by default
            let connectedUser: User = users.find(user => this.isMe(user));
            this.addUser(connectedUser, [StudyUserRight.CAN_SEE_ALL, StudyUserRight.CAN_DOWNLOAD, StudyUserRight.CAN_IMPORT, StudyUserRight.CAN_ADMINISTRATE]);
        });
        return Promise.resolve();
    }

    private fetchUsers(): Promise<User[]> {
        return this.userService.getAll().then(users => {
            this.users = users;
            this.userOptions = [];
            if (users) {
                users.forEach(user => this.userOptions.push(new Option<User>(user, user.lastName + ' ' + user.firstName)));
            }
            return users;
        });
    }

    buildForm(): FormGroup {
        let formGroup = this.formBuilder.group({
            'name': [this.study.name, [Validators.required, Validators.minLength(2), Validators.maxLength(200), this.registerOnSubmitValidator('unique', 'name')]],
            'startDate': [this.study.startDate, [DatepickerComponent.validator]],
            'endDate': [this.study.endDate, [DatepickerComponent.validator, this.dateOrdervalidator]],
            'studyStatus': [this.study.studyStatus, [Validators.required]],
            'withExamination': [this.study.withExamination],
            'clinical': [this.study.clinical],
            'visibleByDefault': [this.study.visibleByDefault],
            'downloadableByDefault': [this.study.downloadableByDefault],
            'monoCenter': [{value: this.study.monoCenter, disabled: this.study.studyCenterList && this.study.studyCenterList.length > 1}, [Validators.required]],
            'studyCenterList': [this.selectedCenter, [this.validateCenter]],
            'subjectStudyList': [this.study.subjectStudyList],
            'protocolFile': []
        });
        return formGroup;
    }

    private dateOrdervalidator = (control: AbstractControl): ValidationErrors | null => {
        if (this.study.startDate && this.study.endDate && this.study.startDate >= this.study.endDate) {
            return { order: true}
        }
        return null;
    }

    public async hasEditRight(): Promise<boolean> {
        if (this.keycloakService.isUserAdmin()) return true;
        if (!this.study.studyUserList) return false;
        let studyUser: StudyUser = this.study.studyUserList.filter(su => su.userId == KeycloakService.auth.userId)[0];
        if (!studyUser) return false;
        return studyUser.studyUserRights && studyUser.studyUserRights.includes(StudyUserRight.CAN_ADMINISTRATE);
    }

    public async hasDeleteRight(): Promise<boolean> {
        if (this.keycloakService.isUserAdmin()) return true;
        if (!this.study.studyUserList) return false;
        let studyUser: StudyUser = this.study.studyUserList.filter(su => su.userId == KeycloakService.auth.userId)[0];
        if (!studyUser) return false;
        return studyUser.studyUserRights && studyUser.studyUserRights.includes(StudyUserRight.CAN_ADMINISTRATE);
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

    private getCenters() {
        return this.centerService
            .getCentersNames()
            .then(centers => {
                this.centerOptions = [];
                if (centers) {
                    centers.forEach(center => {
                        this.centerOptions.push(new Option<IdName>(center, center.name));
                    });
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
    
    /** Center section management  **/
    private onMonoMultiChange() {
        if (this.study.monoCenter && this.study.studyCenterList.length == 1) {
            let option = this.centerOptions.find(option => option.value.id == this.study.studyCenterList[0].center.id);
            if (option) this.selectedCenter = option.value; 
        }
    }

    private goToCenter(id: number) {
        this.router.navigate(['/center/details/' + id]);
    }

    private onCenterAdd(): void {
        if (!this.selectedCenter) return;
        let studyCenter: StudyCenter = new StudyCenter();
        studyCenter.center = new Center();
        studyCenter.center.id = this.selectedCenter.id;
        studyCenter.center.name = this.selectedCenter.name;
        this.study.studyCenterList.push(studyCenter);

        let option = this.centerOptions.find(option => option.value.id == this.selectedCenter.id);
        if (option) option.disabled = true;

        this.form.get('studyCenterList').markAsDirty();
        this.form.get('studyCenterList').updateValueAndValidity();
    }

    private onCenterChange(center: IdName): void {
        this.selectedCenter = center;
        if (this.study.monoCenter) {
            this.study.studyCenterList = []
            this.onCenterAdd();
        }
    }

    private validateCenter = (control: AbstractControl): ValidationErrors | null => {
        if (!this.study.studyCenterList || this.study.studyCenterList.length == 0) {
            return { noCenter: true}
        }
        return null;
    }

    private removeCenterFromStudy(centerId: number): void {
        if (!this.study.studyCenterList || this.study.studyCenterList.length < 2) return;
        this.study.studyCenterList = this.study.studyCenterList.filter(item => item.center.id !== centerId);
        if (this.study.studyCenterList.length < 2) {
            this.study.monoCenter = true;
            this.onMonoMultiChange();
        }
        let option = this.centerOptions.find(option => option.value.id == centerId);
        if (option) option.disabled = false;
        this.form.get('studyCenterList').markAsDirty();
        this.form.get('studyCenterList').updateValueAndValidity();
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

    isMe(user: User): boolean {
        return user.id == KeycloakService.auth.userId;
    }

    disableEdit(studyUser: StudyUser): boolean {
        return !this.freshlyAddedMe && studyUser.userId == KeycloakService.auth.userId;
    }
        
    private createColumnDefs() {
        this.columnDefs = [
            { headerName: 'Username', field: 'userName' },
            { headerName: 'First Name', field: 'user.firstName' },
            { headerName: 'Last Name', field: 'user.lastName' },
            { headerName: 'Email', field: 'user.email', width: '200%' },
            { headerName: 'Role', field: 'user.role.displayName', width: '80px', defaultSortCol: true },
            { headerName: 'Can see all', type: 'boolean', editable: false, width: '54px', suppressSorting: true,
                //onEdit: (su: StudyUser, value: boolean) => this.onEditRight(StudyUserRight.CAN_SEE_ALL, su, value),
                cellRenderer: (params: any) => params.data.studyUserRights.includes(StudyUserRight.CAN_SEE_ALL)},
            { headerName: 'Can download', type: 'boolean', editable: true, width: '54px', suppressSorting: true, 
                onEdit: (su: StudyUser, value: boolean) => this.onEditRight(StudyUserRight.CAN_DOWNLOAD, su, value),
                cellRenderer: (params: any) => params.data.studyUserRights.includes(StudyUserRight.CAN_DOWNLOAD)},
            { headerName: 'Can import', type: 'boolean', editable: true, width: '54px', suppressSorting: true, 
                onEdit: (su: StudyUser, value: boolean) => this.onEditRight(StudyUserRight.CAN_IMPORT, su, value),
                cellRenderer: (params: any) => params.data.studyUserRights.includes(StudyUserRight.CAN_IMPORT)},
            { headerName: 'Can admin', type: 'boolean',  suppressSorting: true, editable: (su: StudyUser) => su.user && su.user.role.displayName != 'User', width: '54px', 
                onEdit: (su: StudyUser, value: boolean) => this.onEditRight(StudyUserRight.CAN_ADMINISTRATE, su, value),
                cellRenderer: (params: any) => params.data.studyUserRights.includes(StudyUserRight.CAN_ADMINISTRATE), },
            { headerName: 'Received Import Mail', type: 'boolean', field: 'receiveNewImportReport', editable: true, width: '54px' },
            { headerName: 'Received Anonymization Mail', type: 'boolean', field: 'receiveAnonymizationReport', editable: true, width: '54px' },
            { headerName: '', type: 'button', awesome: 'fa-trash', action: this.removeStudyUser }
        ];
    }

    /**
     * On select/unselect given right for the given study user 
     */
    private onEditRight(right: StudyUserRight, su: StudyUser, selected: boolean) {
        if (!su.studyUserRights.includes(right) && selected) {
            su.studyUserRights.push(right);
        }
        else if (su.studyUserRights.includes(right) && !selected) {
            const index = su.studyUserRights.indexOf(right, 0);
            if (index > -1) su.studyUserRights.splice(index, 1);
        }
    }

    private onUserAdd(selectedUser: User) {
        if (!selectedUser) {
            return;
        }
        if (this.study.studyUserList.filter(user => user.userId == selectedUser.id).length > 0){
            return;   
        }
        if (this.isMe(selectedUser)) {
            this.freshlyAddedMe = true;
        }
        this.addUser(selectedUser);
    }

    private addUser(selectedUser: User, rights: StudyUserRight[] = [StudyUserRight.CAN_SEE_ALL]) {
        if (this.userOptions) {
            let option = this.userOptions.find(opt => opt.value.id == selectedUser.id);
            if (option) option.disabled = true;
        }

        let backedUpStudyUser: StudyUser = this.studyUserBackup.filter(su => su.userId == selectedUser.id)[0];
        if (backedUpStudyUser) {
            this.study.studyUserList.push(backedUpStudyUser);
        } else {
            let studyUser: StudyUser = new StudyUser();
            studyUser.userId = selectedUser.id;
            studyUser.userName = selectedUser.username;
            studyUser.receiveAnonymizationReport = false;
            studyUser.receiveNewImportReport = false;
            studyUser.studyUserRights = rights;
            studyUser.completeMember(this.users);
            this.study.studyUserList.push(studyUser);
        }
        this.browserPaging.setItems(this.study.studyUserList);
        this.table.refresh();
        this.form.get('subjectStudyList').markAsDirty();
        this.form.updateValueAndValidity();
    }

    private removeStudyUser = (item: StudyUser) => {
        const index: number = this.study.studyUserList.indexOf(item);
        if (index !== -1) {
            this.study.studyUserList.splice(index, 1);
        }
        this.browserPaging.setItems(this.study.studyUserList);
        this.table.refresh();
        this.form.get('subjectStudyList').markAsDirty();
        this.form.updateValueAndValidity();
        StudyUser.completeMember(item, this.users);
        if (this.userOptions) {
            let option = this.userOptions.find(opt => opt.value.id == item.user.id);
            if (option) option.disabled = false;
        }
    }

    private onStudyUserEdit() {
        this.form.get('subjectStudyList').markAsDirty();
        this.form.updateValueAndValidity();
    }

    private studyStatusStr(studyStatus: string) {
        return capitalsAndUnderscoresToDisplayable(studyStatus);
    }

    private click() {
        this.fileInput.nativeElement.click();
    }

    protected deleteFile(file: any) {
        if (this.mode == 'create') { 
            this.study.protocolFilePaths = [];
            this.protocolFile = null;
        } else if (this.mode == 'edit') {
            // TODO: API call
            this.studyService.deleteFile(this.study.id);
            this.study.protocolFilePaths = [];
            this.protocolFile = null;           
        }
    }

    protected downloadFile() {
        this.studyService.downloadFile(this.study.protocolFilePaths[0], this.study.id);
    }

    private attachNewFile(event: any) {
        this.protocolFile = event.target.files[0];
        if (this.protocolFile.name.indexOf(".pdf", this.protocolFile.name.length - ".pdf".length) == -1) {
            this.msgBoxService.log("error", "Only PDF files are accepted");
            this.protocolFile = null;
        } else {
            this.study.protocolFilePaths = [this.protocolFile.name];
        }
        this.form.updateValueAndValidity();
    }

    protected save(): Promise<void> {
        let prom = super.save().then(result => {
            // Once the study is saved, save associated file if changed
            if (this.protocolFile) {
                this.studyService.uploadFile(this.protocolFile, this.entity.id).toPromise().then(result => (console.log("file saved sucessfuly")));
            }
        });
        return prom;
    }

    getFileName(element): string {
        return element.split('\\').pop().split('/').pop();
    }

    onTreeSelectedChange(study: StudyNode) {
        let dsIds: number [] = [];
        if (study.subjects && study.subjects != 'UNLOADED') {
            study.subjects.forEach(subj => {
                if (subj.examinations && subj.examinations != 'UNLOADED') {
                    subj.examinations.forEach(exam => {
                        if (exam.datasetAcquisitions && exam.datasetAcquisitions != 'UNLOADED') {
                            exam.datasetAcquisitions.forEach(dsAcq => {
                                dsIds = dsIds.concat(this.searchSelectedInDatasetNodes(dsAcq.datasets));
                            });
                        }
                    });
                }
            });
        }
        this.selectedDatasetIds = dsIds;
    }

    private searchSelectedInDatasetNodes(dsNodes: DatasetNode[] | 'UNLOADED'): number[] {
        if (dsNodes && dsNodes != 'UNLOADED') {
            return dsNodes.map(ds => {
                // get selected dataset from this nodes
                let idsFound: number[] = ds.selected ? [ds.id] : [];
                // get selected datasets from this node's processings datasets
                if (ds.processings && ds.processings != 'UNLOADED') {
                    let foundInProc: number[] = ds.processings
                            .map(proc => this.searchSelectedInDatasetNodes(proc.datasets))
                            .reduce((allFromProc, oneProc) => allFromProc.concat(oneProc), []);
                        idsFound = idsFound.concat(foundInProc);
                }
                return idsFound;
            }).reduce((allFromDs, thisDs) => {
                return allFromDs.concat(thisDs);
            }, []);
        } else return [];
    }

    onStudyNodeInit(studyNode: StudyNode) {
        studyNode.open = true;
        this.breadcrumbsService.currentStep.data.studyNode = studyNode;
    }
}
