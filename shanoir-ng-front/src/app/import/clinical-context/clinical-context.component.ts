import { Component, HostListener, ViewChild } from '@angular/core';
import { Router } from '@angular/router';
import { Entity } from 'src/app/shared/components/entity/entity.abstract';

import { BreadcrumbsService, Step } from '../../breadcrumbs/breadcrumbs.service';
import { Examination } from '../../examinations/shared/examination.model';
import { ExaminationService } from '../../examinations/shared/examination.service';
import { SubjectExamination } from '../../examinations/shared/subject-examination.model';
import { slideDown } from '../../shared/animations/animations';
import { ModalComponent } from '../../shared/components/modal/modal.component';
import { IdNameObject } from '../../shared/models/id-name-object.model';
import { Study } from '../../studies/shared/study.model';
import { StudyService } from '../../studies/shared/study.service';
import { StudyCard } from '../../study-cards/shared/study-card.model';
import { SubjectStudy } from '../../subjects/shared/subject-study.model';
import { Subject } from '../../subjects/shared/subject.model';
import { SubjectWithSubjectStudy } from '../../subjects/shared/subject.with.subject-study.model';
import { PatientDicom } from '../dicom-data.model';
import { ContextData, ImportDataService } from '../import.data-service';

@Component({
    selector: 'clinical-context',
    templateUrl: 'clinical-context.component.html',
    styleUrls: ['clinical-context.component.css', '../import.step.css'],
    animations: [slideDown]
})
export class ClinicalContextComponent {
    
    patient: PatientDicom;
    //@Output() contextChange = new EventEmitter<ContextData>();
    
    @ViewChild('subjectCreationModal') subjectCreationModal: ModalComponent;
    @ViewChild('examCreationModal') examCreationModal: ModalComponent;

    private studycardMissingError: Boolean;
    private studycardNotCompatibleError: Boolean;

    private studies: Study[];
    private study: Study;
    private studycards: StudyCard[];
    private studycard: StudyCard;
    private subjects: SubjectWithSubjectStudy[]; 
    private subject: SubjectWithSubjectStudy;
    private examinations: SubjectExamination[];
    private examination: SubjectExamination;
    public niftiConverter: IdNameObject;
    
    constructor(
            private studyService: StudyService,
            private examinationService: ExaminationService,
            private router: Router,
            private breadcrumbsService: BreadcrumbsService,
            private importDataService: ImportDataService) {

        if (!importDataService.patients || !importDataService.patients[0]) {
            this.router.navigate(['imports'], {replaceUrl: true});
            return;
        }
        breadcrumbsService.nameStep('3. Context');
        this.setPatient(this.importDataService.patients[0]).then(() => this.reloadSavedData());
    }

    private reloadSavedData() {
        if (this.importDataService.contextBackup) {
            let study = this.importDataService.contextBackup.study;
            let studycard = this.importDataService.contextBackup.studycard;
            let subject = this.importDataService.contextBackup.subject;
            let examination = this.importDataService.contextBackup.examination;
            if (study) this.onSelectStudy(study);
            if (studycard) this.onSelectStudycard(studycard);
            if (subject) this.onSelectSubject(subject);
            if (examination) this.onSelectExamination(examination);
        }
    }

    setPatient(patient: PatientDicom): Promise<void> {
        this.patient = patient;
        return this.fetchStudies();
    }

    private fetchStudies(): Promise<void> {
        return this.studyService
            .findStudiesWithStudyCardsByUserAndEquipment(this.patient.studies[0].series[0].equipment)
            .then(studies => {
                this.studies = studies;
                this.prepareStudyStudycard(studies);
            });
    }

    private prepareStudyStudycard(studies: Study[]): void {
        let compatibleStudies: Study[] = [];
        for (let study of studies) {
            if (study.compatible) {
                compatibleStudies.push(study);
            }
        }
        if (compatibleStudies.length == 1) {
            this.study = compatibleStudies[0];
            this.studycards = this.study.studyCards;
        }
    }
    
    private onSelectStudy(study): void {
        this.study = study
        this.studycards = null;
        this.studycard = null;
        this.subjects = null;
        this.subject = null;
        this.examinations = null;
        this.examination = null;
        if (this.study) {
            if (this.study.studyCards.length == 0) {
                this.studycardMissingError = true;
            } else {
                this.studycardMissingError = false;
                let compatibleStudycards: StudyCard[] = [];
                for (let studycard of this.study.studyCards) {
                    if (studycard.compatible) {
                        compatibleStudycards.push(studycard);
                    }
                }
                if (compatibleStudycards.length == 1) {
                    // autoselect studycard
                    this.studycard = compatibleStudycards[0];
                } 
                this.studycards = this.study.studyCards;
            }
        }
        this.onContextChange();
    }

    private onSelectStudycard(studyCard: StudyCard): void {
        this.studycard = studyCard;
        if (this.studycard) {
            if (this.studycard.compatible) {
                this.studycardNotCompatibleError = false;
            } else {
                this.studycardNotCompatibleError = true;
            }
            this.niftiConverter = this.studycard.niftiConverter;
            this.studyService
                .findSubjectsByStudyId(this.study.id)
                .then(subjects => this.subjects = subjects);
        }
        this.onContextChange();
    }

    private onSelectSubject(subject: SubjectWithSubjectStudy): void {
        this.subject = subject;
        this.examinations = null;
        this.examination = null;
        if (this.subject) {
            this.examinationService
                .findExaminationsBySubjectAndStudy(this.subject.id, this.study.id)
                .then(examinations => this.examinations = examinations);
        }
        this.onContextChange();
    }

    private onSelectExamination(examination: SubjectExamination) {
        this.examination = examination;
        this.onContextChange();
    }

    private onContextChange() {
        this.importDataService.contextBackup = this.getContext();
        if (this.valid) {
            this.importDataService.contextData = this.getContext();
        }
    }
    
    private getContext(): ContextData {
        return new ContextData(this.study, this.studycard, this.subject, this.examination);
    }


    private openCreateSubject = () => {
        let currentStep: Step = this.breadcrumbsService.currentStep;
        this.router.navigate(['/subject/create']).then(success => {
            this.breadcrumbsService.currentStep.entity = this.getPrefilledSubject();
            currentStep.waitFor(this.breadcrumbsService.currentStep, false).subscribe(entity => {
                this.importDataService.contextBackup.subject = this.subjectToSubjectWithSubjectStudy(entity as Subject);
            });
        });
    }

    private getPrefilledSubject(): Subject {
        let subjectStudy = new SubjectStudy();
        subjectStudy.study = this.study;
        subjectStudy.physicallyInvolved = false;
        let newSubject = new Subject();
        newSubject.birthDate = this.patient.patientBirthDate;
        newSubject.name = this.patient.patientName;
        newSubject.sex = this.patient.patientSex; 
        newSubject.subjectStudyList = [subjectStudy];
        return newSubject;
    }
    
    private subjectToSubjectWithSubjectStudy(subject: Subject): SubjectWithSubjectStudy {
        if (!subject) return;
        let subjectWithSubjectStudy = new SubjectWithSubjectStudy();
        subjectWithSubjectStudy.id = subject.id;
        subjectWithSubjectStudy.name = subject.name;
        subjectWithSubjectStudy.identifier = subject.identifier;
        subjectWithSubjectStudy.subjectStudy = subject.subjectStudyList[0];
        return subjectWithSubjectStudy;
    }

    private openCreateExam = () => {
        let currentStep: Step = this.breadcrumbsService.currentStep;
        this.router.navigate(['/examination/create']).then(success => {
            this.breadcrumbsService.currentStep.entity = this.getPrefilledExam();
            currentStep.waitFor(this.breadcrumbsService.currentStep, false).subscribe(entity => {
                this.importDataService.contextBackup.examination = this.examToSubjectExam(entity as Examination);
            });
        });
    }

    private getPrefilledExam(): Examination {
        let newExam = new Examination();
        newExam.studyId = this.study.id;
        newExam.studyName = this.study.name;
        newExam.centerId = this.studycard.center.id;
        newExam.centerName = this.studycard.center.name;
        newExam.subjectId = this.subject.id;
        newExam.subjectName = this.subject.name;
        newExam.examinationDate = this.patient.studies[0].series[0].seriesDate;
        newExam.comment = this.patient.studies[0].studyDescription;
        return newExam;
    }
    
    private examToSubjectExam(examination: Examination): SubjectExamination {
        if (!examination) return;
        // Add the new created exam to the select box and select it
        let subjectExam = new SubjectExamination();
        subjectExam.id = examination.id;
        subjectExam.examinationDate = examination.examinationDate;
        subjectExam.comment = examination.comment;
        return subjectExam;
    }

    private showStudyDetails() {
        window.open('study/details/' + this.study.id, '_blank');
    }

    private showSubjectDetails() {
        window.open('subject/details/' + this.subject.id, '_blank');
    }

    private showStudyCardDetails() {
        window.open('studycard/details/' + this.studycard.id, '_blank');
    }

    private showExaminationDetails() {
        window.open('examination/details/' + this.examination.id, '_blank');
    }

    get valid(): boolean {
        let context = this.getContext();
        return (
            context.study != undefined && context.study != null
            && context.studycard != undefined && context.studycard != null
            && context.subject != undefined && context.subject != null
            && context.examination != undefined && context.examination != null
        );
    }

    private next() {
        this.router.navigate(['imports/finish']);
    }

    private compareEntities(e1: Entity, e2: Entity) : boolean {
        return e1 && e2 && e1.id === e2.id;
    }

    @HostListener('document:keypress', ['$event']) onKeydownHandler(event: KeyboardEvent) {
        if (event.key == 'œ') {
            console.log('study', this.study);
        }
    }
}