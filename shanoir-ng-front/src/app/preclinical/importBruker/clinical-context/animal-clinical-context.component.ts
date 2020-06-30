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

import { Component, OnDestroy } from '@angular/core';
import { Router } from '@angular/router';

import { EquipmentDicom, PatientDicom } from '../../../import/shared/dicom-data.model';
import { Study } from '../../../studies/shared/study.model';
import { StudyService } from '../../../studies/shared/study.service';
import { ExaminationService } from '../../../examinations/shared/examination.service';
import { Subject } from '../../../subjects/shared/subject.model';
import { SubjectWithSubjectStudy } from '../../../subjects/shared/subject.with.subject-study.model';
import { SubjectExamination } from '../../../examinations/shared/subject-examination.model';
import { IdName } from '../../../shared/models/id-name.model';
import { SubjectStudy } from '../../../subjects/shared/subject-study.model';
import { slideDown } from '../../../shared/animations/animations';
import { AnimalSubject } from '../../animalSubject/shared/animalSubject.model';
import { AnimalSubjectService } from '../../animalSubject/shared/animalSubject.service';
import { Examination } from '../../../examinations/shared/examination.model';
import { ImportDataService, ContextData } from '../../../import/shared/import.data-service';
import { BreadcrumbsService, Step } from '../../../breadcrumbs/breadcrumbs.service';
import { Entity } from '../../../shared/components/entity/entity.abstract';
import { PreclinicalSubject } from '../../animalSubject/shared/preclinicalSubject.model';
import { Center } from '../../../centers/shared/center.model';
import { CenterService } from '../../../centers/shared/center.service';
import { AcquisitionEquipment } from '../../../acquisition-equipments/shared/acquisition-equipment.model';
import { NiftiConverter } from '../../../niftiConverters/nifti.converter.model';
import { NiftiConverterService } from '../../../niftiConverters/nifti.converter.service';
import { StudyCenter } from '../../../studies/shared/study-center.model';
import { ImagedObjectCategory } from '../../../subjects/shared/imaged-object-category.enum';
import { Option } from '../../../shared/select/select.component';
import { AcquisitionEquipmentPipe } from '../../../acquisition-equipments/shared/acquisition-equipment.pipe';
import { SubjectExaminationPipe } from '../../../examinations/shared/subject-examination.pipe';
import { Subscription } from 'rxjs';

@Component({
    selector: 'animal-clinical-context',
    templateUrl: 'animal-clinical-context.component.html',
    styleUrls: ['../../../import/clinical-context/clinical-context.component.css', '../../../import/shared/import.step.css'],
    animations: [slideDown]
})
export class AnimalClinicalContextComponent implements OnDestroy {
    
    patient: PatientDicom;
    private studyOptions: Option<Study>[] = [];
    private centerOptions: Option<Center>[] = [];
    private allCenters: Center[];
    private acquisitionEquipmentOptions: Option<AcquisitionEquipment>[] = [];
    private subjects: SubjectWithSubjectStudy[] = [];
    private examinations: SubjectExamination[] = [];
    private niftiConverters: NiftiConverter[] = [];
    private study: Study;
    private center: Center;
    private acquisitionEquipment: AcquisitionEquipment;
    private subject: SubjectWithSubjectStudy;
    private examination: SubjectExamination;
    private niftiConverter: NiftiConverter;
    private animalSubject: AnimalSubject = new AnimalSubject();
    private subscribtions: Subscription[] = [];
    
    constructor(
            private studyService: StudyService,
            private centerService: CenterService,
            private niftiConverterService: NiftiConverterService,
            private animalSubjectService: AnimalSubjectService,
            private examinationService: ExaminationService,
            private router: Router,
            private breadcrumbsService: BreadcrumbsService,
            private importDataService: ImportDataService,
            private acqEqPipe: AcquisitionEquipmentPipe,
            public subjectExaminationLabelPipe: SubjectExaminationPipe) {

        if (!importDataService.patients || !importDataService.patients[0]) {
            this.router.navigate(['importsBruker'], {replaceUrl: true});
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
                this.study = study;
                this.onSelectStudy();
            }
            if (center) {
                this.center = center;
                this.onSelectCenter();
            }
            if (acquisitionEquipment) {
                this.acquisitionEquipment = acquisitionEquipment;
                this.onSelectAcquisitonEquipment();
            }
            if (subject) {
                this.subject = subject;
                this.onSelectSubject();
            }
            if (examination) {
                this.examination = examination;
                this.onSelectExam();
            }
            if (niftiConverter) {
                this.niftiConverter = niftiConverter;
                this.onContextChange();
            }
        }
    }
    

    setPatient(patient: PatientDicom): Promise<void> {
        this.patient = patient;
        return this.completeStudiesCompatibilities(this.patient.studies[0].series[0].equipment)
            /* For the moment, we import only zip files with the same equipment, 
            That's why the calculation is only based on the equipment of the first series of the first study */
            .then(() => {
                let compatibleFounded = this.studyOptions.find(study => study.compatible);
                if (compatibleFounded) {
                    this.study = compatibleFounded.value;
                    this.onSelectStudy();
                }
            })
    }

    private completeStudiesCompatibilities(equipment: EquipmentDicom): Promise<void> {
        return Promise.all([this.studyService.getStudyNamesAndCenters(), this.centerService.getAll()])
            .then(([allStudies, allCenters]) => {
                this.studyOptions = [];
                this.allCenters = allCenters;
                for (let study of allStudies) {
                    let studyOption: Option<Study> = new Option(study, study.name);
                    studyOption.compatible = false;
                    if (study.studyCenterList) {
                        for (let studyCenter of study.studyCenterList) {
                            let center: Center = allCenters.find(center => center.id === studyCenter.center.id)
                            if (center) {
                                if (this.centerCompatible(center)) {
                                    studyOption.compatible = true;
                                }
                                studyCenter.center = center;
                            } 
                        }
                        this.studyOptions.push(studyOption);
                    }
                }
            });
    }

    private equipmentsEquals(eq1: AcquisitionEquipment, eq2: EquipmentDicom): boolean {
        return eq1.serialNumber === eq2.deviceSerialNumber
        && eq1.manufacturerModel.name === eq2.manufacturerModelName
        && eq1.manufacturerModel.manufacturer.name === eq2.manufacturer;
    }

    public acqEqCompatible(acquisitionEquipment: AcquisitionEquipment): boolean {
        return this.equipmentsEquals(acquisitionEquipment, this.patient.studies[0].series[0].equipment);
    }
    
    public centerCompatible(center: Center): boolean {
        return center.acquisitionEquipments && center.acquisitionEquipments.find(eq => this.acqEqCompatible(eq)) != undefined;
    }
    

    private onSelectStudy(): Promise<void> {
        this.centerOptions = this.acquisitionEquipmentOptions = this.subjects = this.examinations = [];
        if (this.study && this.study.id && this.study.studyCenterList) {
            for (let studyCenter of this.study.studyCenterList) {
                let option = new Option<Center>(studyCenter.center, studyCenter.center.name);
                option.compatible = studyCenter.center && this.centerCompatible(studyCenter.center);
                if (option.compatible) {
                    this.center = option.value;
                    this.onSelectCenter();
                }
                this.centerOptions.push(option);
            }
        }
        return Promise.resolve();
    }

    private onSelectCenter(): void {
        this.acquisitionEquipment = this.subject = this.examination = null;
        this.acquisitionEquipmentOptions = this.subjects = this.examinations = [];
        if (this.center && this.center.acquisitionEquipments) {
            for (let acqEq of this.center.acquisitionEquipments) {
                let option = new Option<AcquisitionEquipment>(acqEq, this.acqEqPipe.transform(acqEq));
                option.compatible = this.acqEqCompatible(acqEq);
                if (option.compatible) {
                    this.acquisitionEquipment = option.value;
                    this.onSelectAcquisitonEquipment();
                }
                this.acquisitionEquipmentOptions.push(option);
            }
        }
    }

    private onSelectAcquisitonEquipment(): void {
        this.subjects = this.examinations = [];
        this.subject = this.examination = null;
        if (this.acquisitionEquipment) {
            this.studyService
                .findSubjectsByStudyIdPreclinical(this.study.id, true)
                .then(subjects => this.subjects = subjects);
        }
        this.onContextChange();
    }

    private onSelectSubject(): void {
        this.examinations = [];
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
    
    private onSelectExam(): void {
        this.niftiConverters = [];
        if (this.examination) {
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
        return new ContextData(this.study, null, false, this.center, this.acquisitionEquipment,
            this.subject, this.examination, this.niftiConverter, null);
    }

    private openCreateCenter = () => {
        let currentStep: Step = this.breadcrumbsService.currentStep;
        this.router.navigate(['/center/create']).then(success => {
            this.breadcrumbsService.currentStep.entity = this.getPrefilledCenter();
            this.subscribtions.push(
                currentStep.waitFor(this.breadcrumbsService.currentStep, false).subscribe(entity => {
                    this.importDataService.contextBackup.center = this.updateStudyCenter(entity as Center);
                })
            );
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
        this.router.navigate(['/preclinical-subject/create']).then(success => {
            this.breadcrumbsService.currentStep.entity = this.getPrefilledSubject();
            this.breadcrumbsService.currentStep.data.firstName = this.computeNameFromDicomTag(this.patient.patientName)[1];
            this.breadcrumbsService.currentStep.data.lastName = this.computeNameFromDicomTag(this.patient.patientName)[2];
            importStep.waitFor(this.breadcrumbsService.currentStep, false).subscribe(entity => {
                this.importDataService.contextBackup.subject = this.subjectToSubjectWithSubjectStudy((entity as PreclinicalSubject).subject);
            });
        });
    }

    private getPrefilledSubject(): PreclinicalSubject {
        let subjectStudy = new SubjectStudy();
        subjectStudy.study = this.study;
        subjectStudy.physicallyInvolved = false;
        let newPreclinicalSubject = new PreclinicalSubject();
        let newSubject = new Subject();
        let newAnimalSubject = new AnimalSubject();
        newSubject.imagedObjectCategory = ImagedObjectCategory.LIVING_ANIMAL;
        newSubject.birthDate = this.patient.patientBirthDate;
        newSubject.name = this.patient.patientName;
        if (this.patient.patientSex){
            newSubject.sex = this.patient.patientSex; 
        }
        newSubject.subjectStudyList = [subjectStudy];
        newPreclinicalSubject.subject = newSubject;
        newPreclinicalSubject.animalSubject = newAnimalSubject;
        return newPreclinicalSubject;
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
        this.router.navigate(['/preclinical-examination/create']).then(success => {
            this.breadcrumbsService.currentStep.entity = this.getPrefilledExam();
            currentStep.waitFor(this.breadcrumbsService.currentStep, false).subscribe(entity => {
                this.importDataService.contextBackup.examination = this.examToSubjectExam(entity as Examination);
            });
        });
    }


    private getPrefilledExam(): Examination {
        let newExam = new Examination();
        newExam.preclinical = true;
        newExam.hasStudyCenterData = true;
        newExam.study = new IdName(this.study.id, this.study.name);
        newExam.center = new IdName(this.center.id, this.center.name);
        newExam.subjectStudy = this.subject;
        newExam.subject = new Subject();
        newExam.subject.id = this.subject.id;
        newExam.subject.name = this.subject.name;
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
        return this.centerOptions.find(center => center.compatible) != undefined;
    }
    
    private get hasCompatibleEquipments(): boolean {
        return this.acquisitionEquipmentOptions.find(ae => ae.compatible) != undefined;
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
        if (this.animalSubject.id){
        	window.open('preclinical-subject/details/' + this.animalSubject.id , '_blank');
        }else{
            window.open('subject/details/' + this.subject.id, '_blank');
        }
    }

    private showExaminationDetails() {
        window.open('preclinical-examination/details/' + this.examination.id , '_blank');
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
        this.router.navigate(['imports/brukerfinish']);
    }

    private compareEntities(e1: Entity, e2: Entity) : boolean {
        return e1 && e2 && e1.id === e2.id;
    }

    ngOnDestroy() {
        for(let subscribtion of this.subscribtions) {
            subscribtion.unsubscribe();
        }
    }
}