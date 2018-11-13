import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { ManufacturerModel } from '../../acquisition-equipments/shared/manufacturer-model.model';
import { AcquisitionEquipment } from '../../acquisition-equipments/shared/acquisition-equipment.model';
import { BreadcrumbsService, Step } from '../../breadcrumbs/breadcrumbs.service';
import { Center } from '../../centers/shared/center.model';
import { Examination } from '../../examinations/shared/examination.model';
import { ExaminationService } from '../../examinations/shared/examination.service';
import { SubjectExamination } from '../../examinations/shared/subject-examination.model';
import { NiftiConverter } from '../../niftiConverters/nifti.converter.model';
import { NiftiConverterService } from '../../niftiConverters/nifti.converter.service';
import { slideDown } from '../../shared/animations/animations';
import { Entity } from '../../shared/components/entity/entity.abstract';
import { Study } from '../../studies/shared/study.model';
import { StudyService } from '../../studies/shared/study.service';
import { SubjectStudy } from '../../subjects/shared/subject-study.model';
import { Subject } from '../../subjects/shared/subject.model';
import { SubjectWithSubjectStudy } from '../../subjects/shared/subject.with.subject-study.model';
import { PatientDicom } from '../dicom-data.model';
import { ContextData, ImportDataService } from '../import.data-service';
import { StudyCenter } from 'src/app/studies/shared/study-center.model';

@Component({
    selector: 'clinical-context',
    templateUrl: 'clinical-context.component.html',
    styleUrls: ['clinical-context.component.css', '../import.step.css'],
    animations: [slideDown]
})
export class ClinicalContextComponent {
    
    patient: PatientDicom;
    private contextData: ContextData = new ContextData(null);
    private studies: Study[] = [];
    private centers: Center[] = [];
    private acquisitionEquipments: AcquisitionEquipment[] = [];
    private subjects: SubjectWithSubjectStudy[] = [];
    private examinations: SubjectExamination[] = [];
    private niftiConverters: NiftiConverter[] = [];
    
    constructor(
            private studyService: StudyService,
            private niftiConverterService: NiftiConverterService,
            private examinationService: ExaminationService,
            private router: Router,
            private breadcrumbsService: BreadcrumbsService,
            private importDataService: ImportDataService) {

        if (!importDataService.patients || !importDataService.patients[0]) {
            this.router.navigate(['imports'], {replaceUrl: true});
            return;
        }
        breadcrumbsService.nameStep('3. Context');
        this.setPatient(importDataService.patients[0]);
        this.reloadSavedData();
    }

    private reloadSavedData() {
        if (this.importDataService.contextBackup) {
            let study = this.importDataService.contextBackup.study;
            let center = this.importDataService.contextBackup.studyCenter;
            let acquisitionEquipment = this.importDataService.contextBackup.acquisitionEquipment;
            let subject = this.importDataService.contextBackup.subject;
            let examination = this.importDataService.contextBackup.examination;
            let niftiConverter = this.importDataService.contextBackup.niftiConverter;
            if (study) {
                this.contextData.study = study;
                this.onSelectStudy();
            }
            if (center) {
                this.contextData.studyCenter = center;
                this.onSelectCenter();
            }
            if (acquisitionEquipment) {
                this.contextData.acquisitionEquipment = acquisitionEquipment;
                this.onSelectAcquisitonEquipment();
            }
            if (subject) {
                this.contextData.subject = subject;
                this.onSelectSubject();
            }
            if (examination) {
                this.contextData.examination = examination;
                this.onSelectExam();
            }
            if (niftiConverter) {
                this.contextData.niftiConverter = niftiConverter;
                this.onContextChange();
            }
        }
    }

    setPatient(patient: PatientDicom) {
        this.patient = patient;
        this.fetchStudies();
    }

    private fetchStudies(): void {
        this.studyService
            .findStudiesByUserAndEquipment(this.patient.studies[0].series[0].equipment) // For the moment, we import only zip files with the same equipment
            .then(studies => {
                let hasOneCompatible: boolean = studies.filter(study => study.compatible).length == 1;
                if (hasOneCompatible) {
                    this.contextData.study = studies.filter(study => study.compatible)[0];
                    this.onSelectStudy();
                }
                this.studies = studies;
            });
    }

    private onSelectStudy(): void {
        this.centers = this.acquisitionEquipments = this.subjects = this.examinations = [];
        if (this.contextData.study && this.contextData.study.studyCenterList) {
            let hasOneCompatible: boolean = this.contextData.study.studyCenterList.filter(studyCenter => studyCenter.center.compatible).length == 1;
            if (hasOneCompatible) {
                this.contextData.studyCenter = this.contextData.study.studyCenterList.filter(studyCenter => studyCenter.center.compatible)[0];
                this.onSelectCenter();
            }
            for (let center of this.contextData.study.studyCenterList) {
                this.centers.push(center.center);
            }
        }
        this.onContextChange();
    }

    private onSelectCenter(): void {
        this.acquisitionEquipments = this.subjects = this.examinations = [];
        if (this.contextData.studyCenter.center && this.contextData.studyCenter.center.acquisitionEquipments) {
            let hasOneCompatible: boolean = this.contextData.studyCenter.center.acquisitionEquipments.filter(acqEqt => acqEqt.compatible).length == 1;
            if (hasOneCompatible) {
                this.contextData.acquisitionEquipment = this.contextData.studyCenter.center.acquisitionEquipments.filter(acqEqt => acqEqt.compatible)[0];
                this.onSelectAcquisitonEquipment();
            }
            this.acquisitionEquipments = this.contextData.studyCenter.center.acquisitionEquipments;
        }
        this.onContextChange();
    }

    private onSelectAcquisitonEquipment(): void {
        this.subjects = this.examinations = [];
        if (this.contextData.acquisitionEquipment) {
            this.studyService
                .findSubjectsByStudyId(this.contextData.study.id)
                .then(subjects => this.subjects = subjects);
        }
        this.onContextChange();
    }

    private onSelectSubject(): void {
        this.examinations = [];
        if (this.contextData.subject) {
            this.examinationService
            .findExaminationsBySubjectAndStudy(this.contextData.subject.id, this.contextData.study.id)
            .then(examinations => this.examinations = examinations);
        }
        this.onContextChange();
    }
    
    private onSelectExam(): void {
        this.niftiConverters = [];
        if (this.contextData.examination) {
            this.niftiConverterService.getAll().then(niftiConverters => this.niftiConverters = niftiConverters);
        }
        this.onContextChange();
    }

    private onContextChange() {
        this.importDataService.contextBackup = this.getContext();
        if (this.valid) {
            this.importDataService.contextData = this.getContext();
        }
    }
    
    private getContext(): ContextData {
        return new ContextData(this.contextData);
    }

    private openCreateCenter = () => {
        let currentStep: Step = this.breadcrumbsService.currentStep;
        this.router.navigate(['/center/create']).then(success => {
            this.breadcrumbsService.currentStep.entity = this.getprefilledCenter();
            currentStep.waitFor(this.breadcrumbsService.currentStep, false).subscribe(entity => {
                this.importDataService.contextBackup.studyCenter.center = (entity as Center);
            });
        });
    }

    private getprefilledCenter(): Center {
        let newCenter = new Center();
        newCenter.name = this.patient.studies[0].series[0].institution.institutionName;
        newCenter.street = this.patient.studies[0].series[0].institution.institutionAddress;
        return newCenter;
    }

    private openCreateAcqEqt() {
        let currentStep: Step = this.breadcrumbsService.currentStep;
        this.router.navigate(['/acquisition-equipment/create']).then(success => {
            this.breadcrumbsService.currentStep.entity = this.getprefilledAcqEqt();
            currentStep.waitFor(this.breadcrumbsService.currentStep, false).subscribe(entity => {
                this.importDataService.contextBackup.acquisitionEquipment = (entity as AcquisitionEquipment);
            });
        });
    }

    private getprefilledAcqEqt(): AcquisitionEquipment {
        let acqEpt = new AcquisitionEquipment();
        acqEpt.center = this.contextData.studyCenter.center;
        acqEpt.serialNumber = this.patient.studies[0].series[0].equipment.deviceSerialNumber;
        // let manufModel = new ManufacturerModel();
        // manufModel.name = this.patient.studies[0].series[0].equipment.manufacturerModelName;
        // manufModel.manufacturer.name = this.patient.studies[0].series[0].equipment.manufacturer;
        // acqEpt.manufacturerModel = manufModel;
        return acqEpt;
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
        subjectStudy.study = this.contextData.study;
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
        newExam.studyId = this.contextData.study.id;
        newExam.studyName = this.contextData.study.name;
        newExam.centerId = this.contextData.studyCenter.id;
        newExam.centerName = this.contextData.studyCenter.center.name;
        newExam.subjectId = this.contextData.subject.id;
        newExam.subjectName = this.contextData.subject.name;
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
        window.open('study/details/' + this.contextData.study.id, '_blank');
    }

    private showCenterDetails() {
        window.open('center/details/' + this.contextData.studyCenter.id, '_blank');
    }

    private showAcquistionEquipmentDetails() {
        window.open('acquisition-equipment/details/' + this.contextData.acquisitionEquipment.id, '_blank');
    }

    private showSubjectDetails() {
        window.open('subject/details/' + this.contextData.subject.id, '_blank');
    }

    private showExaminationDetails() {
        window.open('examination/details/' + this.contextData.examination.id, '_blank');
    }

    get valid(): boolean {
        let context = this.getContext();
        return (
            context.study != undefined && context.study != null
            && context.studyCenter != undefined && context.studyCenter != null
            && context.acquisitionEquipment != undefined && context.acquisitionEquipment != null
            && context.niftiConverter != undefined && context.niftiConverter != null
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
}