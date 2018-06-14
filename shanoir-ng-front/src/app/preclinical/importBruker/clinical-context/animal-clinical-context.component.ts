import { Component, Output, EventEmitter, ViewChild, Input, OnChanges, OnInit, SimpleChanges } from '@angular/core';
import { PatientDicom } from "../../../import/dicom-data.model";
import { ModalComponent } from '../../../shared/components/modal/modal.component';
import { Study } from '../../../studies/shared/study.model';
import { StudyService } from '../../../studies/shared/study.service';
import { StudyCard } from '../../../study-cards/shared/study-card.model';
import { ExaminationService } from '../../../examinations/shared/examination.service';
import { Subject } from '../../../subjects/shared/subject.model';
import { SubjectService } from "../../../subjects/shared/subject.service";
import { SubjectWithSubjectStudy } from '../../../subjects/shared/subject.with.subject-study.model';
import { SubjectExamination } from '../../../examinations/shared/subject-examination.model';
import { IdNameObject } from '../../../shared/models/id-name-object.model';
import { SubjectStudy } from '../../../subjects/shared/subject-study.model';
import { Router } from '@angular/router';
import { AbstractImportStepComponent } from '../../../import/import-step.abstract';
import { slideDown } from '../../../shared/animations/animations';
import { Mode } from "../../shared/mode/mode.model";
import { Modes } from "../../shared/mode/mode.enum";
import { AnimalSubject } from '../../animalSubject/shared/animalSubject.model';
import { AnimalSubjectService } from '../../animalSubject/shared/animalSubject.service';
import { Examination } from '../../../examinations/shared/examination.model';

export class ContextData {
    constructor(
        public study: Study,
        public studycard: StudyCard,
        public subject: SubjectWithSubjectStudy,
        public examination: SubjectExamination
    ) {};
}

@Component({
    selector: 'animal-clinical-context',
    templateUrl: 'animal-clinical-context.component.html',
    styleUrls: ['../../../import/clinical-context/clinical-context.component.css', '../../../import/import.step.css'],
    animations: [slideDown]
})
export class AnimalClinicalContextComponent extends AbstractImportStepComponent implements OnChanges, OnInit {
    
    @Input() examinationComment: string;
    @Input() patient: PatientDicom;
    @Output() contextChange = new EventEmitter<ContextData>();
    
    @ViewChild('subjectCreationModal') subjectCreationModal: ModalComponent;
    @ViewChild('examinationCreationModal') examinationCreationModal: ModalComponent;

    private studycardMissingError: Boolean;
    private studycardNotCompatibleError: Boolean;

    private studies: Study[];
    private study: Study;
    private studycards: StudyCard[];
    private studycard: StudyCard;
    private subjects: SubjectWithSubjectStudy[]; 
    private subject: SubjectWithSubjectStudy;
    private subjectFromImport: Subject = new Subject();
    private animalSubject: AnimalSubject = new AnimalSubject();
    private examinations: SubjectExamination[];
    private examination: SubjectExamination;
    public niftiConverter: IdNameObject;
    private mode: Mode = new Mode();
    private examinationFromImport: Examination = new Examination();
    
    constructor(
        private studyService: StudyService,
        private examinationService: ExaminationService,
        private subjectService: SubjectService,
        private animalSubjectService: AnimalSubjectService,
        private router: Router,
    ) {
        super();
    }

	ngOnInit(): void {
	 	this.mode.createMode();
	 	this.mode.setModeFromParameter("create");
	}
	
	
    ngOnChanges(changes: SimpleChanges) {
        if(changes['patient'] && changes['patient'].currentValue) {
            this.studycard = null;
            this.subject = null;
            this.examination = null;
            this.onContextChange();
            this.fetchStudies();
        }
    }

    private changeExamComment (editedLabel: string): void {
        this.examinationComment = editedLabel;
    }

    private fetchStudies(): void {
        this.studyService
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
    
    private onSelectStudy(): void {
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

    private onSelectStudycard(): void {
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

    private onSelectSubject(): void {
        this.examinations = null;
        this.examination = null;
        if (this.subject) {
        	this.animalSubjectService
        		.findAnimalSubjectBySubjectId(this.subject.id)
        		.then(animalSubject => this.animalSubject = animalSubject)
        		.catch((error) => {});
            this.examinationService
                .findExaminationsBySubjectAndStudy(this.subject.id, this.study.id)
                .then(examinations => this.examinations = examinations);
        }
        this.onContextChange();
    }

    private onSelectExamination() {
        this.onContextChange();
    }

    private onContextChange() {
        this.updateValidity();
        if (this.getValidity()) {
            this.contextChange.emit(this.getContext());
        }
    }
    
    private getContext(): ContextData {
        return new ContextData(this.study, this.studycard, this.subject, this.examination);
    }

    private updateSubjectStudyValues() {
    	if (this.subject && this.subject.subjectStudy){
        	this.subjectService.updateSubjectStudyValues(this.subject.subjectStudy);
        }
    }


    private initializePrefillSubject(): void {
        this.mode.createMode();
        let subjectStudy = new SubjectStudy();
        subjectStudy.study = this.study;
        subjectStudy.physicallyInvolved = false;

        let newSubject = new Subject();
        if (this.patient){
        	newSubject.birthDate = this.patient.patientBirthDate;
        	newSubject.identifier = this.patient.patientName;
        	newSubject.sex = this.patient.patientSex;
        } 
        newSubject.subjectStudyList = [subjectStudy];
        this.subjectFromImport = newSubject;
    }
    
    private onCloseSubjectPopin(subject?: Subject): void {
        if (subject && subject.id) {
            // Add the subject to the select box and select it
            let subjectWithSubjectStudy = new SubjectWithSubjectStudy();
            subjectWithSubjectStudy.id = subject.id;
            subjectWithSubjectStudy.name = subject.name;
            subjectWithSubjectStudy.identifier = subject.identifier;
            if (subject.subjectStudyList && subject.subjectStudyList.length > 0){
            	subjectWithSubjectStudy.subjectStudy = subject.subjectStudyList[0];
            }
            if (this.subjects == null){
            	this.subjects = new Array<SubjectWithSubjectStudy>();
            }
            this.subjects.push(subjectWithSubjectStudy);
            this.subject = subjectWithSubjectStudy;
            this.onContextChange();
        }
        this.subjectCreationModal.hide();
    }

    private showStudyDetails() {
        window.open('study?id=' + this.study.id + '&mode=view', '_blank');
    }

    private showSubjectDetails() {
    	if (this.animalSubject.id){
        	window.open('preclinical-subject?id=' + this.animalSubject.id + '&mode=view', '_blank');
        }else{
            window.open('subject?id=' + this.subject.id + '&mode=view', '_blank');
        }
    }

    private showStudyCardDetails() {
        window.open('studycard?id=' + this.studycard.id + '&mode=view', '_blank');
    }

    private showExaminationDetails() {
        window.open('preclinical-examination?id=' + this.examination.id + '&mode=view', '_blank');
    }

    getValidity(): boolean {
        let context = this.getContext();
        return (
            context.study != undefined && context.study != null
            && context.studycard != undefined && context.studycard != null
            && context.subject != undefined && context.subject != null
            && context.examination != undefined && context.examination != null
        );
    }
    
    
    private onCloseExaminationPopin(examination?: Examination): void {
    	if (examination && examination.id) {
     		// Add the examination to the select box and select it
            let subjectExamination = new SubjectExamination();
            subjectExamination.id = examination.id;
            subjectExamination.comment = examination.comment;
            subjectExamination.examinationDate = examination.examinationDate;
            if (this.examinations == null){
            	this.examinations = new Array<SubjectExamination>();
            }
            this.examinations.push(subjectExamination);
            this.examination = subjectExamination;
            this.onContextChange();
        }
        this.examinationCreationModal.hide();
    }
    
    private initializePrefillExamination(): void{
    	this.mode.createMode();
        let examination = new Examination();
        if (this.study){
        	examination.studyId = this.study.id;
        	examination.studyName = this.study.name;
        }
        //examination.centerId = ;
        //examination.centerName = ;
       // examination.examinationDate = ;
        this.examinationFromImport = examination;
    }
}