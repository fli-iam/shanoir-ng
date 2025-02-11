/**rabbitMqCon
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

import { Step } from '../../breadcrumbs/breadcrumbs.service';
import { Examination } from '../../examinations/shared/examination.model';
import { AnimalSubject } from '../../preclinical/animalSubject/shared/animalSubject.model';
import { AnimalSubjectService } from '../../preclinical/animalSubject/shared/animalSubject.service';
import { PreclinicalSubject } from '../../preclinical/animalSubject/shared/preclinicalSubject.model';
import { preventInitialChildAnimations, slideDown } from '../../shared/animations/animations';
import { IdName } from '../../shared/models/id-name.model';
import { ImagedObjectCategory } from '../../subjects/shared/imaged-object-category.enum';
import { SubjectStudy } from '../../subjects/shared/subject-study.model';
import { Subject } from '../../subjects/shared/subject.model';
import { SubjectWithSubjectStudy } from '../../subjects/shared/subject.with.subject-study.model';
import { ServiceLocator } from '../../utils/locator.service';
import { AbstractClinicalContextComponent } from '../clinical-context/clinical-context.abstract.component';
import { ImportJob, PatientDicom, SerieDicom, StudyDicom } from '../shared/dicom-data.model';
import {UnitOfMeasure} from "../../enum/unitofmeasure.enum";


@Component({
    selector: 'pre-clinical-context',
    templateUrl: '../clinical-context/clinical-context.component.html',
    styleUrls: ['../clinical-context/clinical-context.component.css', '../shared/import.step.css'],
    animations: [slideDown, preventInitialChildAnimations],
    standalone: false
})
export class PreClinicalContextComponent extends AbstractClinicalContextComponent implements OnDestroy {

    private animalSubject: AnimalSubject = new AnimalSubject();
    private animalSubjectService: AnimalSubjectService = ServiceLocator.injector.get(AnimalSubjectService);
    patient: PatientDicom;
    editSubjectStudy: boolean = false;

    postConstructor() {
        this.patient = this.importDataService.patients[0];
        this.useStudyCard = true;
    }

    protected exitCondition(): boolean {
        return !this.importDataService.patients || !this.importDataService.patients[0];
    }

    protected getSubjectList(studyId: number): Promise<SubjectWithSubjectStudy[]> {
        this.openSubjectStudy = false;
        if (!studyId) {
            return Promise.resolve([]);
        } else {
            return this.studyService.findSubjectsByStudyIdPreclinical(studyId, true);
        }
    }

    getNextUrl(): string {
        return '/imports/bruker';
    }

    importData(timestamp: number): Promise<any> {
        let context = this.importDataService.contextData;
        let contextImportJob = this.importDataService.archiveUploaded;
        let importJob = new ImportJob();
        importJob.patients = new Array<PatientDicom>();
        // this.patient.subject = new IdName(this.context.subject.id, this.context.subject.name);
        this.patient.subject = Subject.makeSubject(
                context.subject.id,
                context.subject.name,
                context.subject.identifier,
                context.subject.subjectStudy);
        importJob.patients.push(this.patient);
        importJob.workFolder = contextImportJob.workFolder;
        importJob.fromDicomZip = true;
        importJob.subjectName = context.subject.name;
        importJob.studyName = context.study.name;
        importJob.examinationId = context.examination.id;
        importJob.studyInstanceUID = context.examination.studyInstanceUID;
        importJob.studyId = context.study.id;
        importJob.acquisitionEquipmentId = context.acquisitionEquipment.id;
        importJob.archive = contextImportJob.archive;
        importJob.timestamp = timestamp;
        importJob.anonymisationProfileToUse = context.study.profile?.profileName;
        return this.importService.startImportJob(importJob);
    }

    public onSelectSubject(): Promise<any> {
        return super.onSelectSubject().then(() =>  {
            if (this.subject) {
                return this.animalSubjectService
                    .getAnimalSubject(this.subject.id)
                    .then(animalSubject => this.animalSubject = animalSubject);
            }
            this.onContextChange();
        });
    }

    public showSubjectDetails() {
        window.open('preclinical-subject/details/' + this.animalSubject.id , '_blank');
    }

    protected getCreateSubjectRoute(): string {
        return '/preclinical-subject/create';
    }

    protected getCreateExamRoute(): string {
        return '/preclinical-examination/create';
    }

    protected fillCreateSubjectStep(step: Step) {
        step.entity = this.getPrefilledSubject();
        step.data.firstName = this.computeNameFromDicomTag(this.patient.patientName)[1];
        step.data.lastName = this.computeNameFromDicomTag(this.patient.patientName)[2];
        step.data.patientName = this.patient.patientName;
        step.data.forceStudy = this.study;
        step.data.subjectNamePrefix = this.subjectNamePrefix;
    }

    private getPrefilledSubject(): Subject | PreclinicalSubject {
        let subjectStudy = new SubjectStudy();
        subjectStudy.study = this.study;
        subjectStudy.physicallyInvolved = false;
        let newSubject = new Subject();
        newSubject.birthDate = this.patient?.patientBirthDate ? new Date(this.patient.patientBirthDate) : null;
        if (this.patient.patientSex) {
            if (this.patient.patientSex == 'F' || this.patient.patientSex == 'M') {
                newSubject.sex = this.patient.patientSex;
            }
        }
        newSubject.subjectStudyList = [subjectStudy];
        let newPreclinicalSubject = new PreclinicalSubject();
        let newAnimalSubject = new AnimalSubject();
        newSubject.imagedObjectCategory = ImagedObjectCategory.LIVING_ANIMAL;
        newSubject.name = this.patient.patientName;
        newPreclinicalSubject.animalSubject = newAnimalSubject;
        newPreclinicalSubject.subject = newSubject;
        return newPreclinicalSubject;
    }

    protected fillCreateExaminationStep(step: Step): void {
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
        newExam.examinationDate = this.getFirstSelectedSerie()?.seriesDate ? new Date(this.getFirstSelectedSerie()?.seriesDate) : null;
        newExam.comment = this.getFirstSelectedStudy().studyDescription;
        newExam.weightUnitOfMeasure = UnitOfMeasure.KG;
        return newExam;
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
        for (let study of this.patient.studies) {
            for (let serie of study.series) {
                if (serie.selected) return serie;
            }
        }
       return null;
    }

    protected getFirstSelectedStudy(): StudyDicom {
        for (let study of this.patient.studies) {
            for (let serie of study.series) {
                if (serie.selected) return study;
            }
        }
       return null;
    }

    public showExaminationDetails() {
        window.open('preclinical-examination/details/' + this.examination.id , '_blank');
    }

    get valid(): boolean {
        let context = this.getContext();
        return (
            context.study
            && (!context.useStudyCard || context.studyCard)
            && !!context.center
            && !!context.acquisitionEquipment
            && !!context.subject
            && !!context.examination
        );
    }
}
