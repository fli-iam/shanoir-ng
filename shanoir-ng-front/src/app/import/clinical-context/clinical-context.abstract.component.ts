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
import { Directive, OnDestroy, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Subscription } from 'rxjs';

import { AcquisitionEquipment } from '../../acquisition-equipments/shared/acquisition-equipment.model';
import { AcquisitionEquipmentPipe } from '../../acquisition-equipments/shared/acquisition-equipment.pipe';
import { BreadcrumbsService, Step } from '../../breadcrumbs/breadcrumbs.service';
import { Center } from '../../centers/shared/center.model';
import { CenterService } from '../../centers/shared/center.service';
import { Coil } from '../../coils/shared/coil.model';
import { Examination } from '../../examinations/shared/examination.model';
import { ExaminationService } from '../../examinations/shared/examination.service';
import { SubjectExamination } from '../../examinations/shared/subject-examination.model';
import { SubjectExaminationPipe } from '../../examinations/shared/subject-examination.pipe';
import { ConsoleService } from '../../shared/console/console.service';
import { KeycloakService } from '../../shared/keycloak/keycloak.service';
import { ShanoirError } from '../../shared/models/error.model';
import { Option } from '../../shared/select/select.component';
import { StudyCenter } from '../../studies/shared/study-center.model';
import { StudyRightsService } from '../../studies/shared/study-rights.service';
import { StudyUserRight } from '../../studies/shared/study-user-right.enum';
import { Study } from '../../studies/shared/study.model';
import { StudyService } from '../../studies/shared/study.service';
import { StudyCard } from '../../study-cards/shared/study-card.model';
import { StudyCardService } from '../../study-cards/shared/study-card.service';
import { Subject } from '../../subjects/shared/subject.model';
import { SubjectService } from '../../subjects/shared/subject.service';
import { SubjectWithSubjectStudy } from '../../subjects/shared/subject.with.subject-study.model';
import { ContextData, ImportDataService } from '../shared/import.data-service';
import { ImportService } from '../shared/import.service';
import {PreclinicalSubject} from "../../preclinical/animalSubject/shared/preclinicalSubject.model";

@Directive()
export abstract class AbstractClinicalContextComponent implements OnDestroy, OnInit {

    public studyOptions: Option<Study>[] = [];
    public studycardOptions: Option<StudyCard>[] = [];
    public centerOptions: Option<Center>[] = [];
    private allCenters: Center[];
    public acquisitionEquipmentOptions: Option<AcquisitionEquipment>[] = [];
    public subjects: SubjectWithSubjectStudy[] = [];
    public examinations: SubjectExamination[] = [];
    public study: Study;
    public studycard: StudyCard;
    public center: Center;
    public acquisitionEquipment: AcquisitionEquipment;
    public subject: SubjectWithSubjectStudy;
    public examination: SubjectExamination;
    public subjectNamePrefix: string;
    protected subscribtions: Subscription[] = [];
    public subjectTypes: Option<string>[] = [
        new Option<string>('HEALTHY_VOLUNTEER', 'Healthy Volunteer'),
        new Option<string>('PATIENT', 'Patient'),
        new Option<string>('PHANTOM', 'Phantom')
    ];
    public useStudyCard: boolean = true;

    public scHasCoilToUpdate: boolean;
    public isAdminOfStudy: boolean[] = [];
    public scHasDifferentModality: string;
    public modality: string;
    openSubjectStudy: boolean = false;
    loading: number = 0;
    reloading: boolean = false;
    editSubjectStudy: boolean = true;
    protected stepTs: number;

    constructor(
            public studyService: StudyService,
            public centerService: CenterService,
            public subjectService: SubjectService,
            public examinationService: ExaminationService,
            protected router: Router,
            protected breadcrumbsService: BreadcrumbsService,
            protected importDataService: ImportDataService,
            public subjectExaminationLabelPipe: SubjectExaminationPipe,
            public acqEqPipe: AcquisitionEquipmentPipe,
            public studycardService: StudyCardService,
            public studyRightsService: StudyRightsService,
            private keycloakService: KeycloakService,
            protected consoleService: ConsoleService,
            protected importService: ImportService) {

        if (this.exitCondition()) {
            this.router.navigate(['imports'], {replaceUrl: true});
            return;
        }
        breadcrumbsService.nameStep('3. Context');

        this.preConstructor();
        this.postConstructor()
    }

    ngOnInit(): void {
        this.stepTs = this.breadcrumbsService.currentStep.timestamp;
        this.reloading = !!this.importDataService.contextBackup(this.stepTs);
        if (this.reloading) {
            this.fetchStudies(false).then(() => {
                this.reloadSavedData().finally(() => this.reloading = false);
                this.getStudyCardPolicy(this.study).then(policy => {
                    this.study.studyCardPolicy = policy;
                })
            }).catch(error => {
                throw new ShanoirError({error: {message: 'the study list failed loading', details: error}});
            });
        } else {
            this.fetchStudies(true).then( () => {
                this.getStudyCardPolicy(this.study).then(policy => {
                    this.study.studyCardPolicy = policy;
                })
            });
        }
    }

    protected exitCondition(): boolean {
        return false;
    }

    public preConstructor() {};

    public postConstructor() {};

    protected reloadSavedData(): Promise<void> {
        this.reloading = true;
        let promises: Promise<any>[] = [];
        let study = this.importDataService.contextBackup(this.stepTs).study;
        let studyCard = this.importDataService.contextBackup(this.stepTs).studyCard;
        let center = this.importDataService.contextBackup(this.stepTs).center;
        let acquisitionEquipment = this.importDataService.contextBackup(this.stepTs).acquisitionEquipment;
        let subject = this.importDataService.contextBackup(this.stepTs).subject;
        let examination = this.importDataService.contextBackup(this.stepTs).examination;
        this.study = study;
        let useStudyCard = this.importDataService.contextBackup(this.stepTs).useStudyCard;

        let studyOption = this.studyOptions.find(s => s.value.id == study.id);
        if (studyOption) {
            this.study = studyOption.value; // in case it has been modified by an on-the-fly equipment creation
        }
        promises.push(this.onSelectStudy().then(() => {
            if (this.useStudyCard != useStudyCard) {
                this.useStudyCard = useStudyCard;
                this.onToggleUseStudyCard();
            } else if (useStudyCard && studyCard){
                promises.push(this.onSelectStudyCard());
            } else {
                this.restoreCenter(center, acquisitionEquipment);
            }
            if (subject) {
                this.subject = subject;
                promises.push(this.onSelectSubject().then(() => {
                    if (examination) {
                        this.examination = examination;
                        return this.onSelectExam();
                    }
                }));
            }
        }));
        return Promise.all(promises).then();
    }

    private restoreEquipment(acquisitionEquipment: AcquisitionEquipment) {
        if (acquisitionEquipment) {
            this.acquisitionEquipment = acquisitionEquipment;
        }
    }

    private restoreCenter(center: Center, acquisitionEquipment: AcquisitionEquipment) {
        if (center) {
            this.center = center;
            let centerOption = this.centerOptions.find(c => c.value.id == center.id);
            if (centerOption) {
                this.center = centerOption.value;
            }
            return this.onSelectCenter().then(() => this.restoreEquipment(acquisitionEquipment));
        } else {
            this.restoreEquipment(acquisitionEquipment);
        }
    }

    fetchStudies(selectDefault: boolean = true): Promise<void> {
        return this.completeStudyCenters()
            /* For the moment, we import only zip files with the same equipment,
            That's why the calculation is only based on the equipment of the first series of the first study */
            .then(() => {
                if (selectDefault) {
                    let compatibleFounded = this.studyOptions.find(study => study.compatible);
                    if (compatibleFounded) {
                        this.study = compatibleFounded.value;
                        return this.onSelectStudy();
                    }
                }
            });
    }

    private completeStudyCenters(): Promise<void> {
        return Promise.all([this.studyService.getAll(), this.centerService.getAll()])
            .then(([allStudies, allCenters]) => {
                this.studyOptions = [];
                this.allCenters = allCenters;
                for (let study of allStudies) {
                    let studyOption: Option<Study> = new Option(study, study.name);
                    studyOption.compatible = false;
                    if (study.studyCenterList) {
                        for (let studyCenter of study.studyCenterList) {
                            let center: Center = allCenters.find(center => center.id === studyCenter.center.id);
                            if (center) {
                                studyCenter.center = center;
                                studyOption.compatible = true;
                            }
                        }
                        this.studyOptions.push(studyOption);
                        // update the selected study as well
                        if (this.study && this.study.id == study.id) {
                            this.study.studyCenterList = study.studyCenterList;
                        }
                    }
                }
            });
    }

    /* Please return undefined if you don't know */
    acqEqCompatible(acquisitionEquipment: AcquisitionEquipment): boolean | undefined {
        return undefined;
    }

    centerCompatible(center: Center): boolean | undefined {
        return undefined;
    }

    private computeIsAdminOfStudy(studyId: number) {
        if (this.study && this.isAdminOfStudy[studyId] == undefined) {
            if (this.keycloakService.isUserAdmin) {
                this.isAdminOfStudy[studyId] = true;
            } else {
                this.hasAdminRightOn(this.study).then((result) => this.isAdminOfStudy[studyId] = result);
            }
        }
    }

    private getStudyCardOptions(study: Study): Promise<Option<StudyCard>[]> {
        let studyEquipments: AcquisitionEquipment[] = [];
        if (!study) return Promise.resolve([]);
        /* find equipments for this study - needed for checking studycards compatibilities */
        study.studyCenterList.forEach(studyCenter => {
            studyCenter.center.acquisitionEquipments.forEach(eq => {
                if (studyEquipments.findIndex(se => se.id == eq.id) == -1) studyEquipments.push(eq);
            });
        });
        /* build the studycards options and set their compatibilies */
        return this.centerService.getCentersByStudyId(study.id).then(centers => {
            let accessibleCenterIds = centers.map(center => center.id);
            return this.studycardService.getAllForStudy(study.id).then(studyCards => {
                if (!studyCards) studyCards = [];

                return studyCards.filter(studyCard => {
                    return accessibleCenterIds.includes(studyCard.acquisitionEquipment.center.id);
                }).map(studyCard => {
                    let opt = new Option(studyCard, studyCard.name);
                    let scEq = studyCard.acquisitionEquipment ? studyEquipments.find(se => se.id == studyCard.acquisitionEquipment.id) : null;
                    opt.compatible = this.acqEqCompatible(scEq);
                    return opt;
                });
            });
        });
    }

    private selectDefaultStudyCard(options: Option<StudyCard>[]): Promise<void> {
        let founded = options?.find(option => option.compatible)?.value;
        if (founded) {
            this.studycard = founded;
            return this.onSelectStudyCard();
        } else if (options?.length > 0) {
            this.studycard = options[0].value;
            return this.onSelectStudyCard();
        }
    }

    private getCenterOptions(study: Study): Promise<Option<Center>[]> {
        if (study && study.id && study.studyCenterList) {
            return this.centerService.getCentersByStudyId(study.id).then(centers => {
                return centers.map(center => {
                    let centerOption = new Option<Center>(center, center.name);
                    if (!this.useStudyCard) {
                        centerOption.compatible = center && this.centerCompatible(center);
                    }
                    return centerOption;
                });
            });
        } else {
            return Promise.resolve([]);
        }
    }

    private getStudyCardPolicy(study: Study): Promise<string> {
        if (study && study.id) {
            return this.studyService.get(study.id).then(study => {
                if (study.studyCardPolicy == 'MANDATORY') {
                    this.useStudyCard = true;
                } else {
                    this.useStudyCard = false;
                }
                return study.studyCardPolicy;
            })
        } else {
            return Promise.resolve('MANDATORY');
        }
    }

    private selectDefaultCenter(options: Option<Center>[]): Promise<void> {
        let founded = options?.find(option => option.compatible)?.value;
        if (founded) {
            this.center = founded;
            return this.onSelectCenter();
        } else if (options?.length == 1) {
            this.center = options[0].value;
            return this.onSelectCenter();
        }
    }

    protected getSubjectList(studyId: number): Promise<SubjectWithSubjectStudy[]> {
        this.openSubjectStudy = false;
        if (!studyId) {
            return Promise.resolve([]);
        } else {
            return this.studyService.findSubjectsByStudyId(studyId);
        }
    }

    /**
     * auto-select center, equipment from studycard
     */
    private selectDataFromStudyCard(studyCard: StudyCard, studyCenterList: StudyCenter[]) {
        if (studyCard && studyCenterList) {
            this.acquisitionEquipment = null;
            let eqFound: AcquisitionEquipment;
            if (this.useStudyCard) {
                this.center = this.studycard.acquisitionEquipment.center;
                eqFound = this.studycard.acquisitionEquipment;
            } else {
                let scFound: StudyCenter = studyCenterList?.find(sc => {
                    eqFound = sc.center.acquisitionEquipments.find(eq => eq.id == this.studycard.acquisitionEquipment.id);
                    return (!!eqFound);
                })
                this.center = scFound ? scFound.center : this.studycard?.acquisitionEquipment?.center;
            }
            return this.onSelectCenter().then(() => {
                this.acquisitionEquipment = eqFound;
            });
        }
    }

    private getEquipmentOptions(center: Center): Option<AcquisitionEquipment>[] {
        return center?.acquisitionEquipments?.map(acqEq => {
            let option = new Option<AcquisitionEquipment>(acqEq, this.acqEqPipe.transform(acqEq));
            option.compatible = this.acqEqCompatible(acqEq);
            return option;
        });
    }

    private selectDefaultEquipment(options: Option<AcquisitionEquipment>[]) {
        let founded = options?.find(option => option.compatible)?.value;
        if (founded) {
            this.acquisitionEquipment = founded;
        }
    }

    public onSelectStudy(): Promise<void> {
        this.loading++;
        this.computeIsAdminOfStudy(this.study?.id);
        
        this.useStudyCard = this.study.studyCardPolicy == "MANDATORY" ? true : false;

        this.studycard = this.center = this.acquisitionEquipment = this.subject = this.examination = null;
        let studycardsOrCentersPromise: Promise<void>;
        if (this.useStudyCard) {
            studycardsOrCentersPromise = this.getStudyCardOptions(this.study).then(options => {
                this.studycardOptions = options;
                return this.selectDefaultStudyCard(options);
            });
        } else {
            studycardsOrCentersPromise = this.getCenterOptions(this.study).then(options => {
                this.centerOptions = options;
                return this.selectDefaultCenter(options);
            });
            this.getEquipmentOptions(this.center);
        }
        let subjectsPromise: Promise<void> = this.getSubjectList(this.study?.id).then(subjects => {
            this.subjects = subjects ? subjects : [];
        });
        let tagsPromise: Promise<void> = this.studyService.getTagsFromStudyId(this.study?.id).then(tags => {
            this.study.tags = tags ? tags : [];
        });
        return Promise.all([studycardsOrCentersPromise, subjectsPromise]).finally(() => this.loading--)
            .then(() => this.onContextChange());
    }

    public onSelectStudyCard(): Promise<any> {
        if (this.studycard) {
            this.loading++;
            this.center = this.acquisitionEquipment = null;
            this.scHasCoilToUpdate = this.hasCoilToUpdate(this.studycard);
            this.scHasDifferentModality = this.hasDifferentModality(this.studycard);
            return this.selectDataFromStudyCard(this.studycard, this.study?.studyCenterList)
              .finally(() => this.loading--)
              .then(() => this.onContextChange());
        }
    }

    onClearStudyCard() {
        this.studycard = null;
        // this.useStudyCard = true;
    }

    onToggleUseStudyCard() {
        if (!this.useStudyCard) this.studycard = null;
        else {
            let studycardOpt = this.studycardOptions.find(sco => sco.compatible == true);
            if (studycardOpt) {
                this.studycard = studycardOpt.value;
                this.onSelectStudyCard();
            }
        }
        this.importDataService.contextBackup(this.stepTs).useStudyCard = this.useStudyCard;
    }

    public onSelectCenter(): Promise<any> {
        this.loading++;
        this.acquisitionEquipment = null;
        if (this.center) {
            this.subjectNamePrefix = this.study.studyCenterList.find(studyCenter => studyCenter.center.id === this.center.id)?.subjectNamePrefix;;
        }
        this.openSubjectStudy = false;

        this.acquisitionEquipmentOptions = this.getEquipmentOptions(this.center);
        this.selectDefaultEquipment(this.acquisitionEquipmentOptions);
        this.loading--;
        this.onContextChange();
        return Promise.resolve();
    }

    public onSelectSubject(): Promise<any> {
        this.loading++;
        this.examination = null;
        if (this.subject && !this.subject.subjectStudy) this.subject = null;

        this.examinations = [];
        if (this.subject) {
            return this.examinationService
                    .findExaminationsBySubjectAndStudy(this.subject.id, this.study.id)
                    .finally(() => this.loading--)
                    .then(examinations => {
                        this.examinations = examinations;
                        this.onContextChange();
                    });
        } else {
            this.loading--;
            this.openSubjectStudy = false;
            return Promise.resolve();
        }
    }

    public onSelectExam(): void {
        this.onContextChange();
    }

    public onContextChange() {
        this.importDataService.setContextBackup(this.stepTs, this.getContext());
        if (this.valid) {
            this.importDataService.contextData = this.getContext();
        }
    }

    protected getContext(): any {
        return new ContextData(this.study, this.studycard, this.useStudyCard, this.center, this.acquisitionEquipment,
            this.subject, this.examination, null, null, null, null, null, null);
    }

    public openCreateCenter = () => {
        let currentStep: Step = this.breadcrumbsService.currentStep;
        this.router.navigate(['/center/create']).then(success => {
            this.breadcrumbsService.currentStep.entity = this.getPrefilledCenter();
            this.subscribtions.push(
                currentStep.waitFor(this.breadcrumbsService.currentStep, false).subscribe(entity => {
                    this.importDataService.contextBackup(this.stepTs).center = this.updateStudyCenter(entity as Center);
                })
            );
        });
    }

    private getPrefilledCenter(): Center {
        let studyCenter = new StudyCenter();
        studyCenter.study = this.study;
        let newCenter = new Center();
        newCenter.studyCenterList = [studyCenter];
        if (this.importedCenterDataStr != null) {
            newCenter.name = this.importedCenterDataStr.split(' - ')[0] != "null" ? this.importedCenterDataStr.split(' - ')[0] : "";
            newCenter.street = this.importedCenterDataStr.split(' - ')[1] != "null" ? this.importedCenterDataStr.split(' - ')[1] : "";
        }
        return newCenter;
    }

    private updateStudyCenter(center: Center): Center {
        if (!center) return;
        let studyCenter: StudyCenter = center.studyCenterList[0];
        if (studyCenter) this.study.studyCenterList.push(studyCenter);
        return center;
    }

    public openCreateAcqEqt() {
        let currentStep: Step = this.breadcrumbsService.currentStep;
        this.router.navigate(['/acquisition-equipment/create'], { state: { fromImport: this.importedEquipmentDataStr } }).then(success => {

            this.breadcrumbsService.currentStep.addPrefilled('center', this.center);

            this.fillCreateAcqEqStep(this.breadcrumbsService.currentStep);
            this.subscribtions.push(
                currentStep.waitFor(this.breadcrumbsService.currentStep, false).subscribe(entity => {
                    this.importDataService.contextBackup(this.stepTs).acquisitionEquipment = (entity as AcquisitionEquipment);
                })
            );
        });
    }

    public openCreateSubject = () => {
        let importStep: Step = this.breadcrumbsService.currentStep;
        console.log("step envoi : ", importStep);
        let createSubjectRoute: string = this.getCreateSubjectRoute();
        this.router.navigate([createSubjectRoute]).then(success => {
            this.fillCreateSubjectStep(this.breadcrumbsService.currentStep as Step);
            this.subscribtions.push(
                importStep.waitFor(this.breadcrumbsService.currentStep, false).subscribe(entity => {

                    let sub: Subject;
                    if(entity instanceof Subject){
                        sub = entity;
                    }else if(entity instanceof PreclinicalSubject){
                        sub = entity.subject;
                    }
                    this.importDataService.contextBackup(this.stepTs).subject = this.subjectToSubjectWithSubjectStudy(sub);
                })
            );
        });
    }

    protected fillCreateSubjectStep(step: Step) {}

    protected fillCreateExaminationStep(step: Step) {}

    protected fillCreateAcqEqStep(step: Step) {}

    protected getCreateSubjectRoute(): string {
        return '/subject/create';
    }

    protected getCreateExamRoute(): string {
        return '/examination/create';
    }

    public subjectToSubjectWithSubjectStudy(subject: Subject): SubjectWithSubjectStudy {
        if (!subject) return;
        let subjectWithSubjectStudy = new SubjectWithSubjectStudy();
        subjectWithSubjectStudy.id = subject.id;
        subjectWithSubjectStudy.name = subject.name;
        subjectWithSubjectStudy.identifier = subject.identifier;
        if(subject.subjectStudyList){
            subjectWithSubjectStudy.subjectStudy = subject.subjectStudyList[0];
        }

        return subjectWithSubjectStudy;
    }

    public openCreateExam = () => {
        let currentStep: Step = this.breadcrumbsService.currentStep;
        let createExamRoute: string = this.getCreateExamRoute();
        this.router.navigate([createExamRoute]).then(success => {
            this.fillCreateExaminationStep(this.breadcrumbsService.currentStep);
            this.subscribtions.push(
                currentStep.waitFor(this.breadcrumbsService.currentStep, false).subscribe(entity => {

                    this.importDataService.contextBackup(this.stepTs).examination = this.examToSubjectExam(entity as Examination);
                })
            );
        });
    }

    private examToSubjectExam(examination: Examination): SubjectExamination {
        if (!examination) return;
        // Add the new created exam to the select box and select it
        let subjectExam = new SubjectExamination();
        subjectExam.id = examination.id;
        subjectExam.examinationDate = examination.examinationDate;
        subjectExam.comment = examination.comment;
        subjectExam.preclinical = examination.preclinical;
        return subjectExam;
    }

    public get hasCompatibleCenters(): boolean {
        return this.centerOptions?.find(center => center.compatible) != undefined;
    }

    public get hasCompatibleEquipments(): boolean {
        return this.acquisitionEquipmentOptions?.find(ae => ae.compatible) != undefined;
    }

    get importedCenterDataStr(): string {
        return null;
    }

    get importedEquipmentDataStr(): string {
        return null;
    }

    get valid(): boolean {
        let context = this.getContext();
        return (
            !!context.study
            && (!context.useStudyCard || context.studyCard)
            && !!context.center
            && !!context.acquisitionEquipment
            && !!context.subject?.subjectStudy?.subjectType
            && !!context.examination
        );
    }

    abstract getNextUrl(): string;

    public next() {
        this.startImportJob();
    }

    startImportJob(): void {
        let context = this.importDataService.contextData;
        this.subjectService
            .updateSubjectStudyValues(context.subject.subjectStudy)
            .then(() => {
                let that = this;
                this.importData(this.stepTs)
                    .then(() => {
                        this.importDataService.reset();
                        setTimeout(() => {
                            that.consoleService.log('info', 'Import successfully started for subject "' + that.subject.name + '" in study "' + that.study.name + '"');
                        }, 0);
                        this.router.navigate([this.getNextUrl()]);
                    }).catch(error => {
                        throw error;
                    });
            }).catch(error => {
                throw new Error('Could not save the subjectStudy object, the import job has been stopped. Cause : ' + error);
            });
    }

    abstract importData(timestamp: number): Promise<any>;

    private hasCoilToUpdate(studycard: StudyCard): boolean {
        if (!studycard) return false;
        for (let rule of studycard.rules) {
            for (let ass of rule.assignments) {
                if (ass.field?.endsWith('_COIL') && !(ass.value instanceof Coil)) {
                    return true;
                }
            }
        }
        return false;
    }

    private hasDifferentModality(studycard: StudyCard): any {
        if (!studycard) return false;
        for (let rule of studycard.rules) {
            for (let ass of rule.assignments) {
                if (ass.field == 'MODALITY_TYPE'
                        && this.modality && typeof ass.value == 'string' && ass.value
                        && (ass.value as string).split('_')[0] != this.modality.toUpperCase()) {
                    return (ass.value as string).split('_')[0];
                }
            }
        }
        return false;
    }

    protected hasAdminRightOn(study: Study): Promise<boolean> {
        if (!study) return Promise.resolve(false);
        else if (this.keycloakService.isUserAdmin()) return Promise.resolve(true);
        else if (!this.keycloakService.isUserExpert()) return Promise.resolve(false);
        else return this.studyRightsService.getMyRightsForStudy(study.id).then(rights => {
            return rights && rights.includes(StudyUserRight.CAN_ADMINISTRATE);
        });
    }

    public editStudyCard(studycard: StudyCard) {
        let currentStep: Step = this.breadcrumbsService.currentStep;
        this.router.navigate(['/study-card/edit/' + studycard.id]).then(success => {
            this.subscribtions.push(
                currentStep.waitFor(this.breadcrumbsService.currentStep, true).subscribe(entity => {
                    this.importDataService.contextBackup(this.stepTs).studyCard = entity as StudyCard;
                })
            );
        });
    }

    public createStudyCard() {
        let currentStep: Step = this.breadcrumbsService.currentStep;
        this.router.navigate(['/study-card/create', {studyId: this.study.id}]).then(success => {
            this.subscribtions.push(
                currentStep.waitFor(this.breadcrumbsService.currentStep, true).subscribe(entity => {
                    this.importDataService.contextBackup(this.stepTs).studyCard = entity as StudyCard;
                })
            );
        });
    }


    ngOnDestroy() {
        for(let subscribtion of this.subscribtions) {
            subscribtion.unsubscribe();
        }
    }
}
