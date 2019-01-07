import { Component, HostListener } from '@angular/core';
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
import { IdNameObject } from '../../shared/models/id-name-object.model';
import { StudyCenter } from '../../studies/shared/study-center.model';
import { Study } from '../../studies/shared/study.model';
import { StudyService } from '../../studies/shared/study.service';
import { ImagedObjectCategory } from '../../subjects/shared/imaged-object-category.enum';
import { SubjectStudy } from '../../subjects/shared/subject-study.model';
import { Subject } from '../../subjects/shared/subject.model';
import { SubjectWithSubjectStudy } from '../../subjects/shared/subject.with.subject-study.model';
import { EquipmentDicom, PatientDicom } from '../dicom-data.model';
import { ContextData, ImportDataService } from '../import.data-service';

@Component({
    selector: 'clinical-context',
    templateUrl: 'clinical-context.component.html',
    styleUrls: ['clinical-context.component.css', '../import.step.css'],
    animations: [slideDown]
})
export class ClinicalContextComponent{
    
    patient: PatientDicom;
    private studies: Study[] = [];
    private centers: Center[] = [];
    private acquisitionEquipments: AcquisitionEquipment[] = [];
    private subjects: SubjectWithSubjectStudy[] = [];
    private examinations: SubjectExamination[] = [];
    private niftiConverters: NiftiConverter[] = [];
    private _study: Study;
    private _center: Center;
    private _acquisitionEquipment: AcquisitionEquipment;
    private _subject: SubjectWithSubjectStudy;
    private _examination: SubjectExamination;
    private niftiConverter: NiftiConverter;
    
    constructor(
            private studyService: StudyService,
            private centerService: CenterService,
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
        this.setPatient(this.importDataService.patients[0]).then(() => this.reloadSavedData());
    }

    private reloadSavedData() {
        if (this.importDataService.contextBackup) {
            let study = this.importDataService.contextBackup.study;
            let center = this.importDataService.contextBackup.center;
            let acquisitionEquipment = this.importDataService.contextBackup.acquisitionEquipment;
            let subject = this.importDataService.contextBackup.subject;
            let examination = this.importDataService.contextBackup.examination;
            let niftiConverter = this.importDataService.contextBackup.niftiConverter;
            if (study) this.study = study;
            if (center) this.center = center;
            if (acquisitionEquipment) this.acquisitionEquipment = acquisitionEquipment;
            if (subject) this.subject = subject;
            if (examination) this.examination = examination;
            if (niftiConverter) this.niftiConverter = niftiConverter;
            this.onContextChange();
        }
    }

    setPatient(patient: PatientDicom): Promise<void> {
        this.patient = patient;
        return this.completeStudies(this.patient.studies[0].series[0].equipment)
            /* For the moment, we import only zip files with the same equipment, 
            That's why the calculation is only based on the equipment of the first series of the first study */
            .then(() => {
                if (this.studies) {
                    let hasOneCompatible: boolean = this.studies.filter(study => study.compatible).length == 1;
                    if (hasOneCompatible) {
                        this.study = this.studies.filter(study => study.compatible)[0];
                    }
                } else {
                    this.study = null;
                }
            })
    }

    private completeStudies(equipment: EquipmentDicom): Promise<void> {
        let completeStudyPromises: Promise<void>[] = [];
        completeStudyPromises.push(Promise.all([this.studyService.findStudiesForImport(), this.centerService.getAll()])
            .then(([allStudies, allCenters]) => {
                if (allStudies) {
                    for (let study of allStudies) {
                        for (let studyCenter of study.studyCenterList) {
                            let center = allCenters.find(center => center.id === studyCenter.center.id);
                            if (center) {
                                let compatibleAcqEqts = center.acquisitionEquipments.filter(acqEqt => acqEqt.serialNumber === equipment.deviceSerialNumber
                                    && acqEqt.manufacturerModel.name === equipment.manufacturerModelName
                                    && acqEqt.manufacturerModel.manufacturer.name === equipment.manufacturer);
                                for (let compatibleAcqEqt of compatibleAcqEqts) {
                                    compatibleAcqEqt.compatible = true;
                                    center.compatible = true;
                                    study.compatible = true;
                                }
                                studyCenter.center = center;
                            }
                        } 
                        this.studies.push(study);
                    }
                }
            }));
        return Promise.all(completeStudyPromises).then(() => {});
    }

    private get study(): Study {
        return this._study;
    }
    
    private get center(): Center {
        return this._center;
    }
    
    private get acquisitionEquipment(): AcquisitionEquipment {
        return this._acquisitionEquipment;
    }
    
    private get subject(): SubjectWithSubjectStudy {
        return this._subject;
    }
    
    private get examination(): SubjectExamination {
        return this._examination;
    }
    
    private set study(study: Study) {
        this._study = study;
        this.centers = this.acquisitionEquipments = this.subjects = this.examinations = [];
        this._center = this.acquisitionEquipment = this.subject = this.examination = null;
        if (this.study.id && this.study.studyCenterList) {
            let hasOneCompatible: boolean = this.study.studyCenterList.filter(studyCenter => studyCenter.center.compatible).length == 1;
            if (hasOneCompatible) {
                this.center = this.study.studyCenterList.filter(studyCenter => studyCenter.center.compatible)[0].center;
            }
            for (let studyCenter of this.study.studyCenterList) {
                this.centers.push(studyCenter.center);
            }
        }
    }

    private set center(center: Center) {
        this._center = center;
        this.acquisitionEquipments = this.subjects = this.examinations = [];
        this._acquisitionEquipment = this.subject = this.examination = null;
        if (this.center && this.center.acquisitionEquipments) {
            let hasOneCompatible: boolean = this.center.acquisitionEquipments.filter(acqEqt => acqEqt.compatible).length == 1;
            if (hasOneCompatible) {
                this.acquisitionEquipment = this.center.acquisitionEquipments.filter(acqEqt => acqEqt.compatible)[0];
            }
            this.acquisitionEquipments = this.center.acquisitionEquipments;
        }
    }

    private set acquisitionEquipment(acqEpt: AcquisitionEquipment) {
        this._acquisitionEquipment = acqEpt;
        this.subjects = this.examinations = [];
        this.subject = this.examination = null;
        if (this.acquisitionEquipment) {
            this.studyService
                .findSubjectsByStudyId(this.study.id)
                .then(subjects => this.subjects = subjects);
        }
    }
    
    private set subject(subject: SubjectWithSubjectStudy) {
        this._subject = subject;
        this.examinations = [];
        this.examination = null;
        if (this.subject) {
            this.examinationService
            .findExaminationsBySubjectAndStudy(this.subject.id, this.study.id)
            .then(examinations => this.examinations = examinations);
        }
    }

    private set examination(exam: SubjectExamination) {
        this._examination = exam;
        this.niftiConverters = [];
        if (this.examination) {
            this.niftiConverterService.getAll().then(niftiConverters => this.niftiConverters = niftiConverters);
        }
    }

    private onSelectStudy(study: Study): void {
        this.study = study;
        this.onContextChange();
    }

    private onSelectCenter(center: Center): void {
        this.center = center;
        this.onContextChange();
    }

    private onSelectAcquisitonEquipment(acqEqt: AcquisitionEquipment): void {
        this.acquisitionEquipment = acqEqt;
        this.onContextChange();
    }

    private onSelectSubject(subject: SubjectWithSubjectStudy): void {
        this.subject = subject;
        this.onContextChange();
    }

    private onSelectExamination(examination: SubjectExamination) {
        this.examination = examination;
        this.onContextChange();
    }

    private onSelectNifti(nifti: NiftiConverter) {
        this.niftiConverter = nifti;
        this.onContextChange();
    }

    private onContextChange() {
        this.importDataService.contextBackup = this.getContext();
        if (this.valid) {
            this.importDataService.contextData = this.getContext();
        }
    }
    
    private getContext(): ContextData {
        return new ContextData(this.study, this.center, this.acquisitionEquipment,
            this.subject, this.examination, this.niftiConverter);
    }

    private openCreateCenter = () => {
        let currentStep: Step = this.breadcrumbsService.currentStep;
        this.router.navigate(['/center/create']).then(success => {
            this.breadcrumbsService.currentStep.entity = this.getPrefilledCenter();
            currentStep.waitFor(this.breadcrumbsService.currentStep, false).subscribe(entity => {
                this.importDataService.contextBackup.center = this.updateStudyCenter(entity as Center);
            });
        });
    }

    private getPrefilledCenter(): Center {
        let studyCenter = new StudyCenter();
        studyCenter.study = this.study;
        let newCenter = new Center();
        newCenter.studyCenterList = [studyCenter];
        return newCenter;
    }

    private updateStudyCenter(center: Center): Center {
        if (!center) return;
        let studyCenter: StudyCenter = center.studyCenterList[0];
        this.study.studyCenterList.push(studyCenter);
        return center;
    }

    private openCreateAcqEqt() {
        let currentStep: Step = this.breadcrumbsService.currentStep;
        this.router.navigate(['/acquisition-equipment/create']).then(success => {
            this.breadcrumbsService.currentStep.entity = this.getPrefilledAcqEqt();
            currentStep.waitFor(this.breadcrumbsService.currentStep, false).subscribe(entity => {
                this.importDataService.contextBackup.acquisitionEquipment = (entity as AcquisitionEquipment);
            });
        });
    }

    private getPrefilledAcqEqt(): AcquisitionEquipment {
        let acqEpt = new AcquisitionEquipment();
        acqEpt.center = this.center;
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
        subjectStudy.study = this.study;
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
        newExam.study = new IdNameObject(this.study.id, this.study.name);
        newExam.center = new IdNameObject(this.center.id, this.center.name);
        newExam.subject = this.subject;
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

    private get hasCompatibleCenters(): boolean {
        return this.centers.find(center => center.compatible) != undefined;
    }

    
    private get hasCompatibleEquipments(): boolean {
        return this.acquisitionEquipments.find(ae => ae.compatible) != undefined;
    }

    private showStudyDetails() {
        window.open('study/details/' + this.study.id, '_blank');
    }

    private showCenterDetails() {
        window.open('center/details/' + this.center.id, '_blank');
    }

    private showAcquistionEquipmentDetails() {
        window.open('acquisition-equipment/details/' + this.acquisitionEquipment.id, '_blank');
    }

    private showSubjectDetails() {
        window.open('subject/details/' + this.subject.id, '_blank');
    }

    private showExaminationDetails() {
        window.open('examination/details/' + this.examination.id, '_blank');
    }

    get valid(): boolean {
        let context = this.getContext();
        return (
            context.study != undefined && context.study != null
            && context.center != undefined && context.center != null
            && context.acquisitionEquipment != undefined && context.acquisitionEquipment != null
            && context.subject != undefined && context.subject != null
            && context.examination != undefined && context.examination != null
            && context.niftiConverter != undefined && context.niftiConverter != null
        );
    }

    private next() {
        this.router.navigate(['imports/finish']);
    }

    @HostListener('document:keypress', ['$event']) onKeydownHandler(event: KeyboardEvent) {
        if (event.key == 'œ') {
            console.log('study', this._study);
            console.log('center', this._center);
            console.log('acqEq', this._acquisitionEquipment);
            console.log('subject', this._subject);
            console.log('examination', this._examination);
            console.log('nifti', this.niftiConverter);
        }
    }
}