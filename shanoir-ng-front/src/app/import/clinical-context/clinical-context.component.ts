import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AcquisitionEquipment } from '../../acquisition-equipments/shared/acquisition-equipment.model';
import { BreadcrumbsService, Step } from '../../breadcrumbs/breadcrumbs.service';
import { Center } from '../../centers/shared/center.model';
import { CenterService } from '../../centers/shared/center.service';
import { Examination } from '../../examinations/shared/examination.model';
import { ExaminationService } from '../../examinations/shared/examination.service';
import { SubjectExamination } from '../../examinations/shared/subject-examination.model';
import { NiftiConverter } from '../../niftiConverters/nifti.converter.model';
import { NiftiConverterService } from '../../niftiConverters/nifti.converter.service';
import { slideDown } from '../../shared/animations/animations';
import { Entity } from '../../shared/components/entity/entity.abstract';
import { IdNameObject } from '../../shared/models/id-name-object.model';
import { MsgBoxService } from '../../shared/msg-box/msg-box.service';
import { StudyCenter } from '../../studies/shared/study-center.model';
import { Study } from '../../studies/shared/study.model';
import { StudyService } from '../../studies/shared/study.service';
import { ImagedObjectCategory } from '../../subjects/shared/imaged-object-category.enum';
import { SubjectStudy } from '../../subjects/shared/subject-study.model';
import { Subject } from '../../subjects/shared/subject.model';
import { SubjectWithSubjectStudy } from '../../subjects/shared/subject.with.subject-study.model';
import { PatientDicom, EquipmentDicom } from '../dicom-data.model';
import { ContextData, ImportDataService } from '../import.data-service';

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
            private centerService: CenterService,
            private niftiConverterService: NiftiConverterService,
            private examinationService: ExaminationService,
            private router: Router,
            private breadcrumbsService: BreadcrumbsService,
            private importDataService: ImportDataService,
            private msgBoxService: MsgBoxService) {

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
            let center = this.importDataService.contextBackup.center;
            let acquisitionEquipment = this.importDataService.contextBackup.acquisitionEquipment;
            let subject = this.importDataService.contextBackup.subject;
            let examination = this.importDataService.contextBackup.examination;
            let niftiConverter = this.importDataService.contextBackup.niftiConverter;
            if (study) {
                this.contextData.study = study;
                this.onSelectStudy();
            }
            if (center) {
                this.contextData.center = center;
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
            .findStudiesForImport() 
            .then(studies => {
                /* For the moment, we import only zip files with the same equipment, 
                   That's why the calculation is only based on the equipment of the first series of the first study
                */
                if (studies) this.calculateCompatibility(studies, this.patient.studies[0].series[0].equipment);
                let hasOneCompatible: boolean = studies.filter(study => study.compatible).length == 1;
                if (hasOneCompatible) {
                    this.contextData.study = studies.filter(study => study.compatible)[0];
                    this.onSelectStudy();
                }
                });
            }
            
            
    private calculateCompatibility(studies: Study[], equipment: EquipmentDicom): void {
        for (let study of studies) {
            for (let studyCenter of study.studyCenterList) {
                this.centerService.get(studyCenter.center.id).then(
                    center => {if (center) {
                        for (let acqEpt of center.acquisitionEquipments) {
                            if (acqEpt.serialNumber === this.patient.studies[0].series[0].equipment.deviceSerialNumber
                                && acqEpt.manufacturerModel.name === this.patient.studies[0].series[0].equipment.manufacturerModelName
                                && acqEpt.manufacturerModel.manufacturer.name === this.patient.studies[0].series[0].equipment.manufacturer) {
                                    acqEpt.compatible = true;
                                    center.compatible = true;
                                    study.compatible = true;
                            }
                        } studyCenter.center = center;
                    }
                })
            } this.studies.push(study);
        }
    }

    private onSelectStudy(): void {
        this.centers = this.acquisitionEquipments = this.subjects = this.examinations = [];
        if (this.contextData.study.id && this.contextData.study.studyCenterList) {
            let hasOneCompatible: boolean = this.contextData.study.studyCenterList.filter(studyCenter => studyCenter.center.compatible).length == 1;
            if (hasOneCompatible) {
                this.contextData.center = this.contextData.study.studyCenterList.filter(studyCenter => studyCenter.center.compatible)[0].center;
                this.onSelectCenter();
            }
            for (let studyCenter of this.contextData.study.studyCenterList) {
                this.centers.push(studyCenter.center);
            }
        }
        this.onContextChange();
    }

    private onSelectCenter(): void {
        this.acquisitionEquipments = this.subjects = this.examinations = [];
        if (this.contextData.center.id && this.contextData.center.acquisitionEquipments) {
            let hasOneCompatible: boolean = this.contextData.center.acquisitionEquipments.filter(acqEqt => acqEqt.compatible).length == 1;
            if (hasOneCompatible) {
                this.contextData.acquisitionEquipment = this.contextData.center.acquisitionEquipments.filter(acqEqt => acqEqt.compatible)[0];
                this.onSelectAcquisitonEquipment();
            }
            this.acquisitionEquipments = this.contextData.center.acquisitionEquipments;
        }
        this.onContextChange();
    }

    private onSelectAcquisitonEquipment(): void {
        this.subjects = this.examinations = [];
        if (this.contextData.acquisitionEquipment.id) {
            this.studyService
                .findSubjectsByStudyId(this.contextData.study.id)
                .then(subjects => this.subjects = subjects);
        }
        this.onContextChange();
    }

    private onSelectSubject(): void {
        this.examinations = []; 
        if (this.contextData.subject.id) {
            this.examinationService
            .findExaminationsBySubjectAndStudy(this.contextData.subject.id, this.contextData.study.id)
            .then(examinations => this.examinations = examinations);
        }
        this.onContextChange();
    }
    
    private onSelectExam(): void {
        this.niftiConverters = [];
        if (this.contextData.examination.id) {
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
            this.breadcrumbsService.currentStep.entity = this.getPrefilledCenter();
            currentStep.waitFor(this.breadcrumbsService.currentStep, false).subscribe(entity => {
                this.importDataService.contextBackup.center = (entity as Center);
            });
        });
    }

    private getPrefilledCenter(): Center {
        let studyCenter = new StudyCenter();
        studyCenter.study = this.contextData.study;
        let newCenter = new Center();
        newCenter.studyCenterList = [studyCenter];
        return newCenter;
    }

    // private subjectToSubjectWithSubjectStudy(subject: Subject): SubjectWithSubjectStudy {
    //     if (!subject) return;
    //     let subjectWithSubjectStudy = new SubjectWithSubjectStudy();
    //     subjectWithSubjectStudy.id = subject.id;
    //     subjectWithSubjectStudy.name = subject.name;
    //     subjectWithSubjectStudy.identifier = subject.identifier;
    //     subjectWithSubjectStudy.subjectStudy = subject.subjectStudyList[0];
    //     return subjectWithSubjectStudy;
    // }
    
    // private centerToStudyCenter(center: Center): StudyCenter {
    //     if (!center) return;
    //     let studyCenter = new StudyCenter();
    //     console.log("center.studyCenterList.studyId: " + center.studyCenterList[0].study.id)
    //     studyCenter.center = new IdNameObject(center.id, center.name);
    //     studyCenter.study.id = this.contextData.study.id;
    //     console.log("before - this.contextData.study.studyCenterList: " + this.contextData.study.studyCenterList[0].center.name)
    //     // this.contextData.study.studyCenterList.push(studyCenter);
    //     console.log("centerToStudyCenter - studyId : " + studyCenter.study.id);
    //     return studyCenter;
    // }

    private openCreateAcqEqt() {
        let currentStep: Step = this.breadcrumbsService.currentStep;
        this.router.navigate(['/acquisition-equipment/create']).then(success => {
            this.breadcrumbsService.currentStep.entity = this.getPrefilledAcqEqt();
            // this.msgBoxService.log('info', 
            //     'Your archive contains this data: ' + this.patient.studies[0].series[0].equipment.manufacturer
            //     + ' - ' + this.patient.studies[0].series[0].equipment.manufacturerModelName
            //     + ' - ' + this.patient.studies[0].series[0].equipment.deviceSerialNumber, 100000);
            currentStep.waitFor(this.breadcrumbsService.currentStep, false).subscribe(entity => {
                this.importDataService.contextBackup.acquisitionEquipment = (entity as AcquisitionEquipment);
            });
        });
    }

    private getPrefilledAcqEqt(): AcquisitionEquipment {
        let acqEpt = new AcquisitionEquipment();
        acqEpt.center = this.contextData.center;
        acqEpt.serialNumber = this.patient.studies[0].series[0].equipment.deviceSerialNumber;
        return acqEpt;
    }

    private openCreateSubject = () => {
        let importStep: Step = this.breadcrumbsService.currentStep;
        this.router.navigate(['/subject/create']).then(success => {
            this.breadcrumbsService.currentStep.entity = this.getPrefilledSubject();
            this.breadcrumbsService.currentStep.data.firstName = this.computeNameFromDicomTag(this.patient.patientName)[1];
            this.breadcrumbsService.currentStep.data.lastName = this.computeNameFromDicomTag(this.patient.patientName)[2];
            importStep.waitFor(this.breadcrumbsService.currentStep, false).subscribe(entity => {
                this.importDataService.contextBackup.subject = this.subjectToSubjectWithSubjectStudy(entity as Subject);
            });
        });
    }

    private getPrefilledSubject(): Subject {
        let subjectStudy = new SubjectStudy();
        subjectStudy.study = this.contextData.study;
        subjectStudy.physicallyInvolved = false;
        let newSubject = new Subject();
        newSubject.imagedObjectCategory = ImagedObjectCategory.LIVING_HUMAN_BEING;
        newSubject.birthDate = this.patient.patientBirthDate;
        newSubject.sex = this.patient.patientSex; 
        newSubject.subjectStudyList = [subjectStudy];
        return newSubject;
    }

    /**
     * Try to compute patient first name and last name from dicom tags. 
     * eg. TOM^HANKS -> return TOM as first name and HANKS as last name
     */
    private computeNameFromDicomTag (patientName: string): string[] {
        let names: string[] = [];
        if (patientName) {
            names = patientName.split("\\^");
            if (names === null || names.length !== 2) {
                names.push(patientName);
                names.push(patientName);
            }
        }
        return names;
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
        newExam.centerId = this.contextData.center.id;
        newExam.centerName = this.contextData.center.name;
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
        window.open('center/details/' + this.contextData.center.id, '_blank');
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
            context.study.id != undefined && context.study.id != null
            && context.center.id != undefined && context.center.id != null
            && context.acquisitionEquipment.id != undefined && context.acquisitionEquipment.id != null
            && context.subject.id != undefined && context.subject.id != null
            && context.examination.id != undefined && context.examination.id != null
            && context.niftiConverter.id != undefined && context.niftiConverter.id != null
            );
    }

    private next() {
        this.router.navigate(['imports/finish']);
    }

    private compareEntities(e1: Entity, e2: Entity) : boolean {
        return e1 && e2 && e1.id === e2.id;
    }
}