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
import { AbstractControl, UntypedFormGroup, ValidationErrors, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';

import { KeyValue } from "@angular/common";
import { TaskState } from 'src/app/async-tasks/task.model';
import { EntityService } from 'src/app/shared/components/entity/entity.abstract.service';
import { MassDownloadService } from 'src/app/shared/mass-download/mass-download.service';
import { AccessRequest } from 'src/app/users/access-request/access-request.model';
import { AccessRequestService } from 'src/app/users/access-request/access-request.service';
import { ExecutionDataService } from 'src/app/vip/execution.data-service';
import { Center } from '../../centers/shared/center.model';
import { CenterService } from '../../centers/shared/center.service';
import { DatasetExpressionFormat } from "../../enum/dataset-expression-format.enum";
import { slideDown } from '../../shared/animations/animations';
import { EntityComponent } from '../../shared/components/entity/entity.component.abstract';
import { TableComponent } from '../../shared/components/table/table.component';
import { DatepickerComponent } from '../../shared/date-picker/date-picker.component';
import { KeycloakService } from '../../shared/keycloak/keycloak.service';
import { IdName } from '../../shared/models/id-name.model';
import { Profile } from "../../shared/models/profile.model";
import { Option } from '../../shared/select/select.component';
import { StudyRightsService } from '../../studies/shared/study-rights.service';
import { StudyCardService } from '../../study-cards/shared/study-card.service';
import { SubjectStudy } from '../../subjects/shared/subject-study.model';
import { Subject } from '../../subjects/shared/subject.model';
import { SubjectService } from '../../subjects/shared/subject.service';
import { User } from '../../users/shared/user.model';
import { UserService } from '../../users/shared/user.service';
import { capitalsAndUnderscoresToDisplayable } from '../../utils/app.utils';
import { SuperPromise } from "../../utils/super-promise";
import { StudyCenter } from '../shared/study-center.model';
import { StudyUserRight } from '../shared/study-user-right.enum';
import { StudyUser } from '../shared/study-user.model';
import { Study } from '../shared/study.model';
import { StudyService } from '../shared/study.service';
import { Selection } from './tree.service';
import { Tag } from 'src/app/tags/tag.model';

@Component({
    selector: 'study-detail',
    templateUrl: 'study.component.html',
    styleUrls: ['study.component.css'],
    animations: [slideDown],
    standalone: false
})

export class StudyComponent extends EntityComponent<Study> {
    @ViewChild('memberTable', { static: false }) table: TableComponent;
    @ViewChild('input', { static: false }) private fileInput: ElementRef;
    @ViewChild('duaInput', { static: false }) private duaFileInput: ElementRef;
    protected pdfDownloadState: TaskState = new TaskState();
    protected duaDownloadState: TaskState = new TaskState();
    protected studyDownloadState: TaskState = new TaskState();
    protected downloadState: TaskState = new TaskState();

    subjects: IdName[];
    selectedCenter: IdName;
    users: User[] = [];
    uploading: boolean = false;
    protected protocolFiles: File[];
    protected dataUserAgreement: File;
    openHistory: SuperPromise<void> = new SuperPromise<void>();
    public selectedDatasetIds: number[];
    protected hasDownloadRight: boolean;
    accessRequests: AccessRequest[];
    isStudyAdmin: boolean;
    subjectTagsInUse: Tag[] = [];

    public openPrefix: boolean = false;

    centerOptions: Option<IdName>[];
    profileOptions: Option<Profile>[];
    studyStatusOptions: Option<string>[] = [
        new Option<string>('IN_PROGRESS', 'In Progress'),
        new Option<string>('FINISHED', 'Finished')
    ];

    valueDescOrder = (a: KeyValue<String, number>, b: KeyValue<String, number>): number => {
        return b.value - a.value;
    };

    public keycloakService: KeycloakService;

    constructor(
            private route: ActivatedRoute,
            private centerService: CenterService,
            private studyService: StudyService,
            private subjectService: SubjectService,
            private userService: UserService,
            private studyRightsService: StudyRightsService,
            private studyCardService: StudyCardService,
            private accessRequestService: AccessRequestService,
            protected downloadService: MassDownloadService) {
        super(route, 'study');
        this.activeTab = 'general';
    }

    public set activeTab(param : string) {
        super.activeTab = param;
        if(this.activeTab == "history") {
            this.openHistory.resolve();
        }
    }

    public get activeTab():string {
        return super.activeTab;
    }

    public get study(): Study { return this.entity; }

    public set study(study: Study) {
        this.entity = study;
    }

    public set entity(study: Study) {
        super.entity = study;
        this.updateSubjectTagsInUse();
    }

    public get entity(): Study {
        return super.entity;
    }

    getService(): EntityService<Study> {
        return this.studyService;
    }

    loadHistory() {
        this.openHistory.resolve();
    }

    protected getTreeSelection: () => Selection = () => {
        return Selection.fromStudy(this.study);
    }

    fetchEntity: () => Promise<Study> = () => {
        return this.idPromise.then(() => this.studyService.get(this.id, null));
    }

    initView(): Promise<void> {
        this.studyRightsService.getMyRightsForStudy(this.id).then(rights => {
            this.hasDownloadRight = this.keycloakService.isUserAdmin() 
                || (this.keycloakService.isUserExpert() && rights.includes(StudyUserRight.CAN_DOWNLOAD));
        })

        this.setLabeledSizes(this.study);

        if (this.study.profile == null) {
            let pro = new Profile();
            pro.profileName = "Profile Neurinfo";
            this.study.profile = pro;
        }
        this.study.subjectStudyList = this.study.subjectStudyList.sort(
            function(a: SubjectStudy, b:SubjectStudy) {
                let aname = a.subjectStudyIdentifier ? a.subjectStudyIdentifier : a.subject.name;
                let bname = b.subjectStudyIdentifier ? b.subjectStudyIdentifier : b.subject.name;
                return aname.localeCompare(bname);
            });

        this.hasStudyAdminRight().then(val => this.isStudyAdmin = val);

        if (this.keycloakService.isUserAdmin()) {
            this.accessRequestService.findByStudy(this.id).then(accessReqs => {
                this.accessRequests = accessReqs;
            });
        }
        return this.fetchUsers().then(users => {Study.completeMembers(this.study, users);});
    }

    initEdit(): Promise<void> {

        if (this.study.profile == null) {
            let profile = new Profile();
            profile.profileName = "Profile Neurinfo";
            this.study.profile = profile;
        }

        this.hasStudyAdminRight().then(val => this.isStudyAdmin = val);

        this.getAllSubjects();

        this.protocolFiles = [];

        this.fetchUsers().then(users => {
            Study.completeMembers(this.study, users);
        });
        if (this.keycloakService.isUserAdmin()) {
            this.accessRequestService.findByStudy(this.id).then(accessReqs => {
                this.accessRequests = accessReqs;
            });
        }
        this.getCenters().then(centers => {
            let option = this.centerOptions.find(option => option.value.id == this.study.studyCenterList[0].center.id);
            if (option) this.selectedCenter = option.value;
            this.centerOptions.forEach(option => option.disabled = this.study.studyCenterList.findIndex(studyCenter => studyCenter.center.id == option.value.id) != -1);
        });
        return Promise.resolve();
    }

    async initCreate(): Promise<void> {
        this.study = this.newStudy();
        this.isStudyAdmin = true;
        this.getCenters();
        this.getProfiles();
        this.selectedCenter = null;
        this.protocolFiles = [];
        this.dataUserAgreement = null;
        this.getAllSubjects();

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

    buildForm(): UntypedFormGroup {
        let formGroup = this.formBuilder.group({
            'name': [this.study.name, [Validators.required, Validators.minLength(2), Validators.maxLength(200), this.registerOnSubmitValidator('unique', 'name')]],
            'startDate': [this.study.startDate, [DatepickerComponent.validator]],
            'endDate': [this.study.endDate, [DatepickerComponent.validator, this.dateOrdervalidator]],
            'studyStatus': [this.study.studyStatus, [Validators.required]],
            'profile': [this.study.profile, [Validators.required]],
            'withExamination': [this.study.withExamination],
            'studyCardPolicy': [this.study.studyCardPolicy],
            'clinical': [this.study.clinical],
            'description': [this.study.description],
            'license': [this.study.license],
            'visibleByDefault': [this.study.visibleByDefault],
            'downloadableByDefault': [this.study.downloadableByDefault],
            'selectedCenter': [this.selectedCenter, [Validators.required]],
            'studyCenterList': [{value: this.study.studyCenterList}, [this.validateCenter]],
            'subjectStudyList': [this.study.subjectStudyList],
            'tags': [this.study.tags],
            'studyTags': [this.study.studyTags],
            'challenge': [this.study.challenge],
            'protocolFile': [],
            'dataUserAgreement': [],
            'studyUserList': [this.study.studyUserList]
        });

        return formGroup;
    }
    private setLabeledSizes(study: Study): Promise<void> {
        let waitUploads: Promise<void> = this.studyService.fileUploads.has(study.id)
            ? this.studyService.fileUploads.get(study.id)
            : Promise.resolve();

        this.uploading = true;
        return waitUploads.then(() => {
            return this.studyService.getStudyDetailedStorageVolume(study.id).then(dto => {
                let datasetSizes = dto;
                study.totalSize = datasetSizes.total
                let sizesByLabel = new Map<String, number>()
                for (let sizeByFormat of datasetSizes.volumeByFormat) {
                    if(sizeByFormat.size > 0){
                        sizesByLabel.set(DatasetExpressionFormat.getLabel(sizeByFormat.format), sizeByFormat.size);
                    }
                }
                if (datasetSizes.extraDataSize > 0){
                    sizesByLabel.set("Other files (DUA, protocol...)", datasetSizes.extraDataSize);
                }
                study.detailedSizes = sizesByLabel;
            });
        }).finally(() => {
            this.uploading = false;
        });
    }

    private dateOrdervalidator = (control: AbstractControl): ValidationErrors | null => {
        if (this.study.startDate && this.study.endDate && this.study.startDate >= this.study.endDate) {
            return { order: true}
        }
        return null;
    }

    public async hasStudyAdminRight(): Promise<boolean> {
        if (this.keycloakService.isUserAdmin()) return true;
        if (!this.study?.studyUserList) return false;
        let studyUser: StudyUser = this.study.studyUserList.filter(su => su.userId == KeycloakService.auth.userId)[0];
        if (!studyUser) return false;
        return studyUser.studyUserRights && studyUser.studyUserRights.includes(StudyUserRight.CAN_ADMINISTRATE);
    }

    public async hasEditRight(): Promise<boolean> {
        return this.hasStudyAdminRight();
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
        study.studyCenterList = [];
        study.tags = [];
        study.studyTags = [];
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

    private getProfiles() {
      return this.studyService
        .getStudiesProfiles()
        .then(profiles => {
          this.profileOptions = [];
          if (profiles) {
            profiles.forEach(profile => {
              this.profileOptions.push(new Option<Profile>(profile, profile.profileName));
            });
          }
        });
    }

    private getAllSubjects(): void {
        this.subjectService
            .getAllSubjectsNames()
            .then(subjects => {
                this.subjects = subjects?.sort(function(a:Subject, b:Subject){
                    return a.name.localeCompare(b.name);
                });
            });
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
            this.study.studyCenterList = [...this.study.studyCenterList];
            this.centerOptions.forEach(option => option.disabled = this.study.studyCenterList.findIndex(studyCenter => studyCenter.center.id == option.value.id) != -1);
        }
        this.form.get('studyCenterList').markAsDirty();
        this.form.get('studyCenterList').updateValueAndValidity();
    }

    onPrefixChange() {
        this.form.get('studyCenterList').markAsDirty();
        this.form.get('studyCenterList').updateValueAndValidity();
    }

    private validateCenter = (control: AbstractControl): ValidationErrors | null => {
        if (!Array.isArray(this.study.studyCenterList) || this.study.studyCenterList.length == 0) {
            return { noCenter: true}
        }
        return null;
    }

    removeCenterFromStudy(centerId: number): void {
        if (!this.study.studyCenterList) return;
        this.study.studyCenterList = this.study.studyCenterList.filter(item => item.center.id !== centerId);
        this.centerOptions.forEach(option => option.disabled = this.study.studyCenterList.findIndex(studyCenter => studyCenter.center.id == option.value.id) != -1);
        this.form.get('studyCenterList').markAsDirty();
        this.form.get('studyCenterList').updateValueAndValidity();
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
        this.studyService.downloadProtocolFile(file, this.study.id, this.pdfDownloadState);
    }


    public builFileUrl(file): string {
        return this.studyService.buildProtocolFileUrl(file, this.study.id);
    }

    downloadAll() {
        this.downloadService.downloadAllByStudyId(this.study?.id, this.study.totalSize, this.downloadState);
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
        this.studyService.downloadDuaFile(this.study.dataUserAgreementPaths[0], this.study.id, this.duaDownloadState);
    }

    public attachDataUserAgreement(event: any) {
        this.dataUserAgreement = event.target.files[0];
        if (this.dataUserAgreement.name.indexOf(".pdf", this.dataUserAgreement.name.length - ".pdf".length) == -1) {
            this.consoleService.log("error", "Attaching DUA to study \"" + this.study.name + "\" : Only .pdf files are accepted");
            this.dataUserAgreement = null;
        } else if (this.dataUserAgreement.size > 50000000) {
            this.consoleService.log("error", "Attaching DUA to study \"" + this.study.name + "\" : File must be less than 50Mb.");
            this.dataUserAgreement = null;
        } else {
            this.study.dataUserAgreementPaths = ['DUA-' + this.dataUserAgreement.name];
        }
        this.form.markAsDirty();
        this.form.updateValueAndValidity();
    }

    save(): Promise<Study> {
        return super.save(() => {
            let uploads: Promise<void>[] = [];
            // Once the study is saved, save associated file if changed
            if (this.protocolFiles.length > 0) {
                for (let file of this.protocolFiles) {
                    uploads.push(this.studyService.uploadFile(file, this.entity.id, 'protocol-file'));
                }
            }
            if (this.dataUserAgreement) {
                uploads.push(this.studyService.uploadFile(this.dataUserAgreement, this.entity.id, 'dua')
                    .catch(error => {
                        this.dataUserAgreement = null;
                    }));
            }
            return Promise.all(uploads).then();
        }).then(study => {
            if(!study){
                if(this.saveError){
                    this.consoleService.log('warn', this.saveError.message);
                }
                return;
            }
            if (study.studyCardPolicy == 'MANDATORY') {
                this.studyCardService.getAllForStudy(study.id).then(studyCards => {
                    if (!studyCards || studyCards.length == 0) {
                        this.confirmDialogService.confirm('Create a Study Card',
                            'A study card is necessary in order to import datasets in this new study. Do you want to create a study card now ?')
                            .then(userChoice => {
                                if (userChoice) {
                                    this.router.navigate(['/study-card/create', {studyId: study.id}]);
                                }
                            });
                    }
                })
            }
            return study;
        });
    }

    getFileName(element): string {
        return element.split('\\').pop().split('/').pop();
    }

    onTagListChange() {
        // hack : force change detection
        this.study.tags = [].concat(this.study.tags);
        // hack : force change detection for the subject-study tag list
        this.study.subjectStudyList.forEach(subjStu => {
            subjStu.study.tags = this.study.tags;
        });
        this.study.subjectStudyList = [].concat(this.study.subjectStudyList);

        this.updateSubjectTagsInUse();
    }

    onStudyTagListChange() {
        // hack : force change detection
        this.study.studyTags = [].concat(this.study.studyTags);
        // hack : force change detection for the subject-study tag list
        this.study.subjectStudyList.forEach(subjStu => {
            subjStu.study.studyTags = this.study.studyTags;
        });
        this.study.subjectStudyList = [].concat(this.study.subjectStudyList);
    }

    goToAccessRequest(accessRequest : AccessRequest) {
        this.router.navigate(["/access-request/details/" + accessRequest.id]);
    }

    reloadSubjectStudies() {
        setTimeout(() => {
            this.studyService.get(this.id).then(study => {
                this.study.subjectStudyList = study.subjectStudyList;
            });
        }, 1000);
    }

    studyCardPolicyStr() {
        return capitalsAndUnderscoresToDisplayable(this.study.studyCardPolicy);
    }

    private updateSubjectTagsInUse() {
        let tags: Tag[] = [];
        this.study.subjectStudyList.forEach(ss => {
            tags = tags.concat(ss.tags);
        });
        this.subjectTagsInUse = tags;
    }
}
