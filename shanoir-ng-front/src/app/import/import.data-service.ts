import { Injectable } from '@angular/core';
import { AcquisitionEquipment } from '../acquisition-equipments/shared/acquisition-equipment.model';
import { Center } from '../centers/shared/center.model';
import { SubjectExamination } from '../examinations/shared/subject-examination.model';
import { NiftiConverter } from '../niftiConverters/nifti.converter.model';
import { Study } from '../studies/shared/study.model';
import { SubjectWithSubjectStudy } from '../subjects/shared/subject.with.subject-study.model';
import { ImportJob, PatientDicom } from './dicom-data.model';

export class ContextData {
    
    constructor (
        public study: Study,
        public center: Center, 
        public acquisitionEquipment: AcquisitionEquipment,
        public subject: SubjectWithSubjectStudy,
        public examination: SubjectExamination,
        public niftiConverter: NiftiConverter
    ) {}
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