import { Injectable } from '@angular/core';
import { AcquisitionEquipment } from '../acquisition-equipments/shared/acquisition-equipment.model';
import { SubjectExamination } from '../examinations/shared/subject-examination.model';
import { NiftiConverter } from '../niftiConverters/nifti.converter.model';
import { StudyCenter } from '../studies/shared/study-center.model';
import { Study } from '../studies/shared/study.model';
import { SubjectWithSubjectStudy } from '../subjects/shared/subject.with.subject-study.model';
import { ImportJob, PatientDicom } from './dicom-data.model';

export class ContextData {
    public study: Study;
    public center: StudyCenter;
    public acquisitionEquipment: AcquisitionEquipment;
    public subject: SubjectWithSubjectStudy;
    public examination: SubjectExamination;
    public niftiConverter: NiftiConverter;

    constructor (contextdata: ContextData) {
        this.study = contextdata ? contextdata.study : new Study();
        this.center = contextdata ? contextdata.center: new StudyCenter();
        this.acquisitionEquipment = contextdata ? contextdata.acquisitionEquipment: new AcquisitionEquipment();
        this.niftiConverter = contextdata ? contextdata.niftiConverter: new NiftiConverter();
        this.subject = contextdata ? contextdata.subject: new SubjectWithSubjectStudy();
        this.examination = contextdata ? contextdata.examination: new SubjectExamination();
    }
}

@Injectable()
export class ImportDataService {

    private _inMemoryExtracted: any;      // 1. upload
    private _archiveUploaded: ImportJob;  // 1. upload
    private _patients: PatientDicom[];    // 2. series
    private _contextData: ContextData;    // 3. context
    public contextBackup: ContextData;

    public reset() {
        this._inMemoryExtracted = undefined;
        this._archiveUploaded = undefined;
        this._patients = undefined;
        this._contextData = undefined;
        this.contextBackup = undefined;
    }

    public get inMemoryExtracted(): any {
        return this._inMemoryExtracted;
    }

    public set inMemoryExtracted(extracted: any) {
        this._inMemoryExtracted = extracted;
        this.patients = undefined;
    }

    public get archiveUploaded(): ImportJob {
        return this._archiveUploaded;
    }

    public set archiveUploaded(job: ImportJob) {
        this._archiveUploaded = job;
        this.patients = undefined;
    }

    public get patients(): PatientDicom[] {
        return this._patients;
    }

    public set patients(patients: PatientDicom[]) {
        this._patients = patients;
        this.contextData = undefined;
        this.contextBackup = undefined;
    }

    public get contextData(): ContextData {
        return this._contextData;
    }

    public set contextData(context: ContextData) {
        this._contextData = context;
    }
}  