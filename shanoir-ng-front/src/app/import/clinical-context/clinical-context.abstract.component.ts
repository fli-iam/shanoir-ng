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
import {Directive, HostListener, OnDestroy, OnInit} from '@angular/core';
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
    public subjects: Subject[] = [];
    public examinations: SubjectExamination[] = [];
    public study: Study;
    public studycard: StudyCard;
    public center: Center;
    public acquisitionEquipment: AcquisitionEquipment;
    public subject: Subject;
    public examination: SubjectExamination;
    public subjectNamePrefix: string;
    protected subscriptions: Subscription[] = [];

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

    protected reloadSavedData(): Promise<void> {
        this.reloading = true;
        const promises: Promise<any>[] = [];
        const study = this.importDataService.contextBackup(this.stepTs).study;
        const studyCard = this.importDataService.contextBackup(this.stepTs).studyCard;
        const center = this.importDataService.contextBackup(this.stepTs).center;
        const acquisitionEquipment = this.importDataService.contextBackup(this.stepTs).acquisitionEquipment;
        const subject = this.importDataService.contextBackup(this.stepTs).subject;
        const examination = this.importDataService.contextBackup(this.stepTs).examination;
        this.study = study;
        const useStudyCard = this.importDataService.contextBackup(this.stepTs).useStudyCard;

        const studyOption = this.studyOptions.find(s => s.value.id == study.id);
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
            const centerOption = this.centerOptions.find(c => c.value.id == center.id);
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
                    const compatibleFounded = this.studyOptions.find(study => study.compatible);
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
                for (const study of allStudies) {
                    const studyOption: Option<Study> = new Option(study, study.name);
                    studyOption.compatible = false;
                    if (study.studyCenterList) {
                        for (const studyCenter of study.studyCenterList) {
                            const center: Center = allCenters.find(center => center.id === studyCenter.center.id);
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
    acqEqCompatible(_acquisitionEquipment: AcquisitionEquipment): boolean | undefined {
        return undefined;
    }

    centerCompatible(_center: Center): boolean | undefined {
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
        const studyEquipments: AcquisitionEquipment[] = [];
        if (!study) return Promise.resolve([]);
        /* find equipments for this study - needed for checking studycards compatibilities */
        study.studyCenterList.forEach(studyCenter => {
            studyCenter.center.acquisitionEquipments.forEach(eq => {
                if (studyEquipments.findIndex(se => se.id == eq.id) == -1) studyEquipments.push(eq);
            });
        });
        /* build the studycards options and set their compatibilies */
        return this.centerService.getCentersByStudyId(study.id).then(centers => {
            const accessibleCenterIds = centers.map(center => center.id);
            return this.studycardService.getAllForStudy(study.id).then(studyCards => {
                if (!studyCards) studyCards = [];

                studyCards?.sort((a, b) => a.name?.trim().localeCompare(b.name.trim()));

                return studyCards.filter(studyCard => {
                    return accessibleCenterIds.includes(studyCard.acquisitionEquipment.center.id);
                }).map(studyCard => {
                    const opt = new Option(studyCard, studyCard.name);
                    const scEq = studyCard.acquisitionEquipment ? studyEquipments.find(se => se.id == studyCard.acquisitionEquipment.id) : null;
                    opt.compatible = this.acqEqCompatible(scEq);
                    return opt;
                });
            });
        });
    }

    private selectDefaultStudyCard(options: Option<StudyCard>[]): Promise<void> {
        const founded = options?.find(option => option.compatible)?.value;
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
                centers.sort((a, b) => a.name?.trim().localeCompare(b.name.trim()));
                return centers.map(center => {
                    const centerOption = new Option<Center>(center, center.name);
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
        const founded = options?.find(option => option.compatible)?.value;
        if (founded) {
            this.center = founded;
            return this.onSelectCenter();
        } else if (options?.length == 1) {
            this.center = options[0].value;
            return this.onSelectCenter();
        }
    }

    protected getSubjectList(studyId: number): Promise<Subject[]> {
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
                const scFound: StudyCenter = studyCenterList?.find(sc => {
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
        center?.acquisitionEquipments?.sort((a, b) => a.manufacturerModel?.manufacturer?.name?.trim().localeCompare(b.manufacturerModel?.manufacturer?.name.trim()));
        return center?.acquisitionEquipments?.map(acqEq => {
            const option = new Option<AcquisitionEquipment>(acqEq, this.acqEqPipe.transform(acqEq));
            option.compatible = this.acqEqCompatible(acqEq);
            return option;
        });
    }

    private selectDefaultEquipment(options: Option<AcquisitionEquipment>[]) {
        let newValue: AcquisitionEquipment | null = null;

        if (options?.length == 1) {
            newValue = options[0].value;
        }
        const founded = options?.find(option => option.compatible)?.value;
        if (founded) {
            newValue = founded;
        }
        Promise.resolve().then(() => {
            this.acquisitionEquipment = newValue;
        });
    }

    public onSelectStudy(): Promise<void> {
        if (this.study) {
            this.loading++;
            this.computeIsAdminOfStudy(this.study?.id);

            this.useStudyCard = this.study.studyCardPolicy == "MANDATORY" ? true : false;

            this.studycard = this.center = this.acquisitionEquipment = this.subject = this.examination = null;
            let studycardsOrCentersPromise: Promise<void>;
            if (this.useStudyCard && (this.modality == 'MR' || this.modality == 'bruker')) {
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
            const subjectsPromise: Promise<void> = this.getSubjectList(this.study?.id).then(subjects => {
                this.subjects = subjects ? subjects : [];
                this.subjects?.sort((a, b) => a.name?.trim().localeCompare(b.name.trim()));
            });
            this.studyService.getTagsFromStudyId(this.study?.id).then(tags => {
                this.study.tags = tags ? tags : [];
            });
            return Promise.all([studycardsOrCentersPromise, subjectsPromise]).finally(() => this.loading--)
                .then(() => this.onContextChange());
        }
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
            const studycardOpt = this.studycardOptions.find(sco => sco.compatible == true);
            if (studycardOpt) {
                this.studycard = studycardOpt.value;
                this.onSelectStudyCard();
            }
        }
        this.importDataService.contextBackup(this.stepTs).useStudyCard = this.useStudyCard;
    }

    public onSelectCenter(): Promise<any> {
        if (this.center) {
            this.loading++;
            this.subjectNamePrefix = this.study.studyCenterList.find(studyCenter => studyCenter.center.id === this.center.id)?.subjectNamePrefix;
            this.openSubjectStudy = false;

            this.acquisitionEquipmentOptions = this.getEquipmentOptions(this.center);
            this.selectDefaultEquipment(this.acquisitionEquipmentOptions);
            this.loading--;
            this.onContextChange();
            return Promise.resolve();
        }
    }

    public onSelectSubject(): Promise<any> {
        this.loading++;
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

    protected abstract fillCreateSubjectStep();

    protected abstract fillCreateExaminationStep();

    protected abstract fillCreateAcqEqStep();

    public openCreateCenter = () => {
        const currentStep: Step = this.breadcrumbsService.currentStep;
        this.router.navigate(['/center/create']).then(() => {

            this.breadcrumbsService.currentStep.addPrefilled("entity", this.getPrefilledCenter());
            this.subscriptions.push(
                currentStep.waitFor(this.breadcrumbsService.currentStep, false).subscribe(entity => {
                    this.importDataService.contextBackup(this.stepTs).center = this.updateStudyCenter(entity as Center);
                })
            );
        });
    }

    private getPrefilledCenter(): Center {
        const studyCenter = new StudyCenter();
        studyCenter.study = this.study;
        const newCenter = new Center();
        newCenter.studyCenterList = [studyCenter];
        if (this.importedCenterDataStr != null) {
            newCenter.name = this.importedCenterDataStr.split(' - ')[0] != "null" ? this.importedCenterDataStr.split(' - ')[0] : "";
            newCenter.street = this.importedCenterDataStr.split(' - ')[1] != "null" ? this.importedCenterDataStr.split(' - ')[1] : "";
        }
        this.breadcrumbsService.currentStep.addPrefilled("entity", newCenter);
        return newCenter;
    }

    private updateStudyCenter(center: Center): Center {
        if (!center) return;
        const studyCenter: StudyCenter = center.studyCenterList[0];
        if (studyCenter) this.study.studyCenterList.push(studyCenter);
        return center;
    }

    public openCreateAcqEqt() {
        const currentStep: Step = this.breadcrumbsService.currentStep;
        this.router.navigate(['/acquisition-equipment/create'], { state: { fromImport: this.importedEquipmentDataStr } }).then(() => {
            this.fillCreateAcqEqStep();
            this.breadcrumbsService.currentStep.addPrefilled('center', this.center);
            this.subscriptions.push(
                currentStep.waitFor(this.breadcrumbsService.currentStep, false).subscribe(entity => {
                    this.importDataService.contextBackup(this.stepTs).acquisitionEquipment = (entity as AcquisitionEquipment);
                })
            );
        });
    }

    protected getCreateSubjectRoute(): string {
        return '/subject/create';
    }

    protected getCreateExamRoute(): string {
        return '/examination/create';
    }

    public openCreateSubject = () => {
        const importStep: Step = this.breadcrumbsService.currentStep;
        const createSubjectRoute: string = this.getCreateSubjectRoute();
        this.router.navigate([createSubjectRoute]).then(() => {
            this.fillCreateSubjectStep();
            this.subscriptions.push(
                importStep.waitFor(this.breadcrumbsService.currentStep, false).subscribe(entity => {
                    let sub: Subject;
                    if (entity instanceof Subject) {
                        sub = entity;
                    } else if (entity instanceof PreclinicalSubject) {
                        sub = entity.subject;
                    }
                    this.importDataService.contextBackup(this.stepTs).subject = sub;
                })
            );
        })
    }

    public openCreateExam = () => {
        const currentStep: Step = this.breadcrumbsService.currentStep;
        const createExamRoute: string = this.getCreateExamRoute();
        this.router.navigate([createExamRoute]).then(() => {
            this.fillCreateExaminationStep();
            this.subscriptions.push(
                currentStep.waitFor(this.breadcrumbsService.currentStep, false).subscribe(entity => {
                    this.importDataService.contextBackup(this.stepTs).examination = this.examToSubjectExam(entity as Examination);
                })
            );
        });
    }



    private examToSubjectExam(examination: Examination): SubjectExamination {
        if (!examination) return;
        // Add the new created exam to the select box and select it
        const subjectExam = new SubjectExamination();
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
        return;
    }

    get importedEquipmentDataStr(): string {
        return;
    }

    get valid(): boolean {
        const context = this.getContext();
        return (
            !!context.study
            && !!context.center
            && !!context.acquisitionEquipment
            && !!context.subject
            && !!context.examination
        );
    }

    @HostListener('document:keypress', ['$event']) onKeydownHandler(event: KeyboardEvent) {
        if (event.key == '²') {
            const context = this.getContext();
            console.log('context', context);
        }
    }

    abstract getNextUrl(): string;

    public next() {
        this.startImportJob();
    }

    startImportJob(): void {
        this.importData(this.stepTs)
            .then(() => {
                this.importDataService.reset();
                setTimeout(() => {
                    this.consoleService.log('info', 'Import successfully started for subject "' + this.subject.name + '" in study "' + this.study.name + '"');
                }, 0);
                this.router.navigate([this.getNextUrl()]);
            }).catch(error => {
            throw error;
        });
    }

    abstract importData(timestamp: number): Promise<any>;

    private hasCoilToUpdate(studycard: StudyCard): boolean {
        if (!studycard) return false;
        for (const rule of studycard.rules) {
            for (const ass of rule.assignments) {
                if (ass.field?.endsWith('_COIL') && !(ass.value instanceof Coil)) {
                    return true;
                }
            }
        }
        return false;
    }

    private hasDifferentModality(studycard: StudyCard): any {
        if (!studycard) return false;
        for (const rule of studycard.rules) {
            for (const ass of rule.assignments) {
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
        const currentStep: Step = this.breadcrumbsService.currentStep;
        this.router.navigate(['/study-card/edit/' + studycard.id]).then(() => {
            this.subscriptions.push(
                currentStep.waitFor(this.breadcrumbsService.currentStep, true).subscribe(entity => {
                    this.importDataService.contextBackup(this.stepTs).studyCard = entity as StudyCard;
                })
            );
        });
    }

    public createStudyCard() {
        const currentStep: Step = this.breadcrumbsService.currentStep;
        this.router.navigate(['/study-card/create', {studyId: this.study.id}]).then(() => {
            this.subscriptions.push(
                currentStep.waitFor(this.breadcrumbsService.currentStep, true).subscribe(entity => {
                    this.importDataService.contextBackup(this.stepTs).studyCard = entity as StudyCard;
                })
            );
        });
    }


    ngOnDestroy() {
        for(const subscribtion of this.subscriptions) {
            subscribtion.unsubscribe();
        }
    }
}

