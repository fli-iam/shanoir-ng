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
import { Subject } from '../../subjects/shared/subject.model';
import { DatasetNode, StudyNode } from '../../tree/tree.model';
import { User } from '../../users/shared/user.model';
import { UserService } from '../../users/shared/user.service';
import { capitalsAndUnderscoresToDisplayable } from '../../utils/app.utils';
import { StudyCenter } from '../shared/study-center.model';
import { StudyUserRight } from '../shared/study-user-right.enum';
import { StudyUser } from '../shared/study-user.model';
import { Study } from '../shared/study.model';
import { StudyService } from '../shared/study.service';
import { SubjectStudy } from '../../subjects/shared/subject-study.model';
import { EntityService } from 'src/app/shared/components/entity/entity.abstract.service';
import { StudyRightsService } from '../../studies/shared/study-rights.service';
import { LoadingBarComponent } from '../../shared/components/loading-bar/loading-bar.component';

@Component({
    selector: 'study-detail',
    templateUrl: 'study.component.html',
    styleUrls: ['study.component.css'],
    animations: [slideDown]
})

export class StudyComponent extends EntityComponent<Study> {

    @ViewChild('DUAprogressBar') DUAprogressBar: LoadingBarComponent;
    @ViewChild('PFprogressBar') PFprogressBar: LoadingBarComponent;
    @ViewChild('memberTable', { static: false }) table: TableComponent;
    @ViewChild('input', { static: false }) private fileInput: ElementRef;
    @ViewChild('duaInput', { static: false }) private duaFileInput: ElementRef;

    subjects: IdName[];
    selectedCenter: IdName;
    users: User[] = [];
    
    protected protocolFiles: File[];
    protected dataUserAgreement: File;

    public selectedDatasetIds: number[];
    protected hasDownloadRight: boolean;
    
    public openPrefix: boolean = false;

    centerOptions: Option<IdName>[];
    studyStatusOptions: Option<string>[] = [
        new Option<string>('IN_PROGRESS', 'In Progress'),
        new Option<string>('FINISHED', 'Finished')
    ];

    constructor(
            private route: ActivatedRoute, 
            private centerService: CenterService, 
            private studyService: StudyService, 
            private subjectService: SubjectService,
            private userService: UserService,  
            private studyRightsService: StudyRightsService) {

        super(route, 'study');
    }

    public get study(): Study { return this.entity; }
    public set study(study: Study) { this.entity = study; }

    getService(): EntityService<Study> {
        return this.studyService;
    }

    initView(): Promise<void> {
        this.studyRightsService.getMyRightsForStudy(this.id).then(rights => {
            this.hasDownloadRight = this.keycloakService.isUserAdmin() || rights.includes(StudyUserRight.CAN_DOWNLOAD);
        })
        let studyPromise: Promise<Study> = this.studyService.get(this.id).then(study => {
            this.study = study;
            this.study.subjectStudyList = this.study.subjectStudyList.sort(
                function(a: SubjectStudy, b:SubjectStudy) {
                    let aname = a.subjectStudyIdentifier ? a.subjectStudyIdentifier : a.subject.name;
                    let bname = b.subjectStudyIdentifier ? b.subjectStudyIdentifier : b.subject.name;
                    return aname.localeCompare(bname);
                });
            return study;
        });
        Promise.all([
            studyPromise,
            this.fetchUsers()
        ]).then(([study, users]) => {
            Study.completeMembers(study, users);
        });
        return studyPromise.then(() => null);
    }

    initEdit(): Promise<void> {
        let studyPromise: Promise<Study> = this.studyService.get(this.id).then(study => this.study = study);
        this.getSubjects();
        
        this.protocolFiles = [];

        Promise.all([
            studyPromise,
            this.fetchUsers()
        ]).then(([study, users]) => {
            Study.completeMembers(study, users);
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
        this.protocolFiles = [];
        this.dataUserAgreement = null;
        this.getSubjects();

        this.fetchUsers().then(users => {
            // Add the connected user by default
            let connectedUser: User = users.find(user => this.isMe(user));
            this.addMe(connectedUser, [StudyUserRight.CAN_SEE_ALL, StudyUserRight.CAN_DOWNLOAD, StudyUserRight.CAN_IMPORT, StudyUserRight.CAN_ADMINISTRATE]);
        });
        return Promise.resolve();
    }

    private fetchUsers(): Promise<User[]> {
        return this.userService.getAll().then(users => {
            this.users = users;
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
            'studyCenterList': [this.study.studyCenterList, [this.validateCenter]],
            'subjectStudyList': [this.study.subjectStudyList],
            'tags': [this.study.tags],
            'challenge': [this.study.challenge],
            'protocolFile': [],
            'dataUserAgreement': [],
            'studyUserList': [this.study.studyUserList]
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
        study.tags = [];
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
                this.subjects = subjects.sort(function(a:Subject, b:Subject){
                    return a.name.localeCompare(b.name);
                });
        });
    }
    
    /** Center section management  **/
    onMonoMultiChange() {
        if (this.study.monoCenter && this.study.studyCenterList.length >= 1) {
            this.study.studyCenterList = [this.study.studyCenterList[0]];
            let option = this.centerOptions.find(option => option.value.id == this.study.studyCenterList[0].center.id);
            if (option) this.selectedCenter = option.value; 
        }
    }

    goToCenter(id: number) {
        this.router.navigate(['/center/details/' + id]);
    }

    onCenterAdd(): void {
        if (this.selectedCenter) {
            let studyCenter: StudyCenter = new StudyCenter();
            studyCenter.center = new Center();
            studyCenter.center.id = this.selectedCenter.id;
            studyCenter.center.name = this.selectedCenter.name;
            this.study.studyCenterList.push(studyCenter);

            this.centerOptions.forEach(option => option.disabled = this.study.studyCenterList.findIndex(studyCenter => studyCenter.center.id == option.value.id) != -1);
        }
        this.form.get('studyCenterList').markAsDirty();
        this.form.get('studyCenterList').updateValueAndValidity();
    }

    onCenterChange(center: IdName): void {
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

    removeCenterFromStudy(centerId: number): void {
        if (!this.study.studyCenterList || this.study.studyCenterList.length < 2) return;
        this.study.studyCenterList = this.study.studyCenterList.filter(item => item.center.id !== centerId);
        if (this.study.studyCenterList.length < 2) {
            this.study.monoCenter = true;
            this.onMonoMultiChange();
        }
        this.centerOptions.forEach(option => option.disabled = this.study.studyCenterList.findIndex(studyCenter => studyCenter.center.id == option.value.id) != -1);
        this.form.get('studyCenterList').markAsDirty();
        this.form.get('studyCenterList').updateValueAndValidity();
    }
    
    enableAddIcon(): boolean {
        return this.selectedCenter && !this.isCenterAlreadyLinked(this.selectedCenter.id)
            && (!this.study.monoCenter || !this.study.studyCenterList || this.study.studyCenterList.length == 0);
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

    isMe(user: User): boolean {
        return user.id == KeycloakService.auth.userId;
    }

    private addMe(selectedUser: User, rights: StudyUserRight[] = [StudyUserRight.CAN_SEE_ALL]) {
        let studyUser: StudyUser = new StudyUser();
        studyUser.userId = selectedUser.id;
        studyUser.userName = selectedUser.username;
        studyUser.receiveStudyUserReport = false;
        studyUser.receiveNewImportReport = false;
        studyUser.studyUserRights = rights;
        studyUser.user = selectedUser;
        this.study.studyUserList.unshift(studyUser);
        this.study.studyUserList = this.study.studyUserList;
    }

    studyStatusStr(studyStatus: string) {
        return capitalsAndUnderscoresToDisplayable(studyStatus);
    }

    public click() {
        this.fileInput.nativeElement.click();
    }
    
    public duaClick() {
        this.duaFileInput.nativeElement.click();
    }

    public deleteFileOk(file: any) {
        this.study.protocolFilePaths = this.study.protocolFilePaths.filter(fileToKeep => fileToKeep != file);
        this.protocolFiles = this.protocolFiles.filter(fileToKeep => fileToKeep.name != file);
        this.form.markAsDirty();
        this.form.updateValueAndValidity();
    }
    
    deleteFile(file: any): void {
        this.openDeleteConfirmDialogFile(file)
    }

    openDeleteConfirmDialogFile = (file: string) => {
        this.confirmDialogService
            .confirm(
                'Deleting ' + file, 
                'Are you sure you want to delete the file ' + file + ' ?'
            ).then(res => {
                if (res) {
                   this.deleteFileOk(file);
                }
            })
    }
    
    public setFile() {
        this.fileInput.nativeElement.click();
    }
    
    public setDuaFile() {
        this.duaFileInput.nativeElement.click();
    }

    public downloadFile(file) {
        this.studyService.downloadFile(file, this.study.id, 'protocol-file', this.PFprogressBar);
    }

    public attachNewFile(event: any) {
        let fileToAdd = event.target.files[0];
        this.protocolFiles.push(fileToAdd);
        this.study.protocolFilePaths.push(fileToAdd.name);
        this.form.markAsDirty();
        this.form.updateValueAndValidity();
    }
    
    public deleteDataUserAgreement() {
        if (this.mode == 'create') { 
            this.study.dataUserAgreementPaths = [];
            this.dataUserAgreement = null;
        } else if (this.mode == 'edit') {
            this.studyService.deleteFile(this.study.id, 'dua');
            this.study.dataUserAgreementPaths = [];
            this.dataUserAgreement = null;           
        }
    }

    public downloadDataUserAgreement() {
        this.studyService.downloadFile(this.study.dataUserAgreementPaths[0], this.study.id, 'dua', this.DUAprogressBar);
    }

    public attachDataUserAgreement(event: any) {
        this.dataUserAgreement = event.target.files[0];
        if (this.dataUserAgreement.name.indexOf(".pdf", this.dataUserAgreement.name.length - ".pdf".length) == -1) {
            this.msgBoxService.log("error", "Only .pdf files are accepted");
            this.dataUserAgreement = null;
        } else if (this.dataUserAgreement.size > 50000000) {
            this.msgBoxService.log("error", "File must be less than 50Mb.");
            this.dataUserAgreement = null;
        } else {
            this.study.dataUserAgreementPaths = ['DUA-' + this.dataUserAgreement.name];
        }
        this.form.updateValueAndValidity();
    }

    save(): Promise<Study> {
        return super.save().then(result => {
            // Once the study is saved, save associated file if changed
            if (this.protocolFiles.length > 0) {
                for (let file of this.protocolFiles) {
                    this.studyService.uploadFile(file, this.entity.id, 'protocol-file').toPromise();
                }
            }
            if (this.dataUserAgreement) {
                this.studyService.uploadFile(this.dataUserAgreement, this.entity.id, 'dua').toPromise()
                .catch(error => {
                    this.dataUserAgreement = null;
                });
            }
            return result;
        }).then(study => {
            if (this.mode == 'create') {
               this.confirmDialogService.confirm('Create a Study Card', 
                'A study card is necessary in order to import datasets in this new study. Do you want to create a study card now ?')
                .then(userChoice => {
                    if (userChoice) {
                        this.router.navigate(['/study-card/create', {studyId: study.id}]);
                    }
                });
            }
            return study;
        });
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
   
    public hasDownloadRights(): boolean {
        return this.keycloakService.isUserAdmin() || this.hasDownloadRight;
    }

    onTagListChange() {
        this.study.tags = [].concat(this.study.tags); // hack : force change detection
    }
}
