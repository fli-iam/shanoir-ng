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

import { AcquisitionEquipment } from '../../acquisition-equipments/shared/acquisition-equipment.model';
import { Step } from '../../breadcrumbs/breadcrumbs.service';
import { Center } from '../../centers/shared/center.model';
import { Examination } from '../../examinations/shared/examination.model';
import { preventInitialChildAnimations, slideDown } from '../../shared/animations/animations';
import { IdName } from '../../shared/models/id-name.model';
import { ImagedObjectCategory } from '../../subjects/shared/imaged-object-category.enum';
import { SubjectStudy } from '../../subjects/shared/subject-study.model';
import { SimpleSubject, Subject } from '../../subjects/shared/subject.model';
import { AbstractClinicalContextComponent } from '../clinical-context/clinical-context.abstract.component';
import { EquipmentDicom, ImportJob, PatientDicom, SerieDicom, StudyDicom } from '../shared/dicom-data.model';


@Component({
    selector: 'clinical-context',
    templateUrl: '../clinical-context/clinical-context.component.html',
    styleUrls: ['../clinical-context/clinical-context.component.css', '../shared/import.step.css'],
    animations: [slideDown, preventInitialChildAnimations]
})
export class BasicClinicalContextComponent extends AbstractClinicalContextComponent implements OnDestroy {

    patient: PatientDicom;

    postConstructor() {
        this.patient = this.importDataService.patients[0];
        this.modality = this.getFirstSelectedSerie().modality.toString();
        this.useStudyCard = this.modality.toUpperCase() == "MR";
    }

    protected exitCondition(): boolean {
        return !this.importDataService.patients || !this.importDataService.patients[0];
    }

    getNextUrl(): string {
        return '/imports/upload';
    }

    importData(): Promise<any> {
        let importJob = this.buildImportJob();
        return this.importService.startImportJob(importJob);
    }

    protected buildImportJob(): ImportJob {
        let importJob = new ImportJob();
        let context = this.importDataService.contextData;
        importJob.patients = new Array<PatientDicom>();
        let simpleSubject: SimpleSubject = {
            id: context.subject.id,
            name: context.subject.name,
            identifier: context.subject.identifier,
            subjectStudyList: [context.subject.subjectStudy]
        };
        this.patient.subject = simpleSubject;
        let filteredPatient: PatientDicom = this.patient;
        filteredPatient.studies = this.patient.studies.map(study => {
            study.series = study.series.filter(serie => serie.selected);
            return study;
        });
        importJob.patients.push(filteredPatient);
        importJob.workFolder = this.importDataService.patientList.workFolder;
        importJob.fromDicomZip = true;
        importJob.examinationId = context.examination.id;
        importJob.studyId = context.study.id;
        importJob.studyCardId = context.studyCard ? context.studyCard.id : null;
        importJob.acquisitionEquipmentId = context.acquisitionEquipment.id;
        importJob.converterId = context.niftiConverter.id;
        importJob.subjectName = context.subject.name;
        importJob.studyName = context.study.name;
        importJob.anonymisationProfileToUse = context.study.profile.profileName;
        return importJob;
    }

    acqEqCompatible(acquisitionEquipment: AcquisitionEquipment): boolean | undefined {
        return this.equipmentsEquals(acquisitionEquipment, this.getFirstSelectedSerie()?.equipment);
    }

    centerCompatible(center: Center): boolean | undefined {
        return center.acquisitionEquipments && center.acquisitionEquipments.find(eq => this.acqEqCompatible(eq)) != undefined;
    }

    private equipmentsEquals(eq1: AcquisitionEquipment, eq2: EquipmentDicom): boolean {
        return eq1 && eq2 && eq2?.deviceSerialNumber && (eq1?.serialNumber == eq2?.deviceSerialNumber);
    }

    protected fillCreateSubjectStep(step: Step) {
        step.entity = this.getPrefilledSubject();
        step.data.firstName = this.computeNameFromDicomTag(this.patient.patientName)[1];
        step.data.lastName = this.computeNameFromDicomTag(this.patient.patientName)[2];
        step.data.patientName = this.patient.patientName;
        step.data.forceStudy = this.study;
        step.data.subjectNamePrefix = this.subjectNamePrefix;
    }

    private getPrefilledSubject(): Subject {
        let subjectStudy = new SubjectStudy();
        subjectStudy.study = this.study;
        subjectStudy.physicallyInvolved = false;
        let newSubject = new Subject();
        newSubject.birthDate = this.patient.patientBirthDate;
        if (this.patient.patientSex) {
            if (this.patient.patientSex == 'F' || this.patient.patientSex == 'M') {
                newSubject.sex = this.patient.patientSex;
            }
        }
        newSubject.subjectStudyList = [subjectStudy];
        newSubject.imagedObjectCategory = ImagedObjectCategory.LIVING_HUMAN_BEING;
        return newSubject;
    }

    protected fillCreateExaminationStep(step: Step) {
        step.entity = this.getPrefilledExam();
    }

    private getPrefilledExam(): Examination {
        let newExam = new Examination();
        newExam.preclinical = true;
        newExam.hasStudyCenterData = true;
        newExam.study = new IdName(this.study.id, this.study.name);
        if (this.center) {
            newExam.center = new IdName(this.center.id, this.center.name);
        }
        newExam.subjectStudy = this.subject;
        newExam.subject = new Subject();
        newExam.subject.id = this.subject.id;
        newExam.subject.name = this.subject.name;
        newExam.examinationDate = this.getFirstSelectedSerie().seriesDate;
        newExam.comment = this.getFirstSelectedStudy().studyDescription;
        return newExam;
    }

    protected fillCreateAcqEqStep(step: Step) {
        step.entity = this.getPrefilledAcqEqt();
    }

    private getPrefilledAcqEqt(): AcquisitionEquipment {
        let acqEpt = new AcquisitionEquipment();
        acqEpt.center = this.center;
        acqEpt.serialNumber = this.getFirstSelectedSerie().equipment.deviceSerialNumber;
        return acqEpt;
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

    protected getFirstSelectedSerie(): SerieDicom {
        if (!this.patient) return null;
        for (let study of this.patient.studies) {
            for (let serie of study.series) {
                if (serie.selected) return serie;
            }
        }
       return null;
    }

    protected getFirstSelectedStudy(): StudyDicom {
        if (!this.patient) return null;
        for (let study of this.patient.studies) {
            for (let serie of study.series) {
                if (serie.selected) return study;
            }
        }
       return null;
    }

    get importedCenterDataStr(): string {
        return this.patient?.studies[0]?.series[0]?.institution?.institutionName + " - "
                + this.patient?.studies[0]?.series[0]?.institution?.institutionAddress;
    }

    get importedEquipmentDataStr(): string {
        return this.patient?.studies[0]?.series[0]?.equipment?.manufacturer
                + '-' + this. patient?.studies[0]?.series[0]?.equipment?.manufacturerModelName
                + '-' + this.patient?.studies[0]?.series[0]?.equipment?.deviceSerialNumber;
    }

}
