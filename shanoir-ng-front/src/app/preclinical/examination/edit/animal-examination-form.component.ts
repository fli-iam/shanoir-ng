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

import { Component, ViewChild, ElementRef } from '@angular/core';
import { UntypedFormGroup, Validators } from '@angular/forms';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRoute } from '@angular/router';

import { TaskState } from 'src/app/async-tasks/task.model';
import { EntityService } from 'src/app/shared/components/entity/entity.abstract.service';
import { MassDownloadService } from 'src/app/shared/mass-download/mass-download.service';
import { Selection } from 'src/app/studies/study/tree.service';

import { environment } from '../../../../environments/environment';
import { ContrastAgent }    from '../../contrastAgent/shared/contrastAgent.model';
import { Examination } from '../../../examinations/shared/examination.model';
import { ExaminationAnesthetic }    from '../../anesthetics/examination_anesthetic/shared/examinationAnesthetic.model';
import { ExaminationAnestheticService }    from '../../anesthetics/examination_anesthetic/shared/examinationAnesthetic.service';
import { ExtraData }    from '../../extraData/extraData/shared/extradata.model';
import { BloodGasData }    from '../../extraData/bloodGasData/shared/bloodGasData.model';
import { BloodGasDataFile }    from '../../extraData/bloodGasData/shared/bloodGasDataFile.model';
import { PhysiologicalDataFile }    from '../../extraData/physiologicalData/shared/physiologicalDataFile.model';
import { ExtraDataService } from '../../extraData/extraData/shared/extradata.service';
import { CenterService } from '../../../centers/shared/center.service';
import { StudyService } from '../../../studies/shared/study.service';
import { StudyRightsService } from '../../../studies/shared/study-rights.service';
import { StudyUserRight } from '../../../studies/shared/study-user-right.enum';
import { IdName } from '../../../shared/models/id-name.model';
import { AnimalSubjectService } from '../../animalSubject/shared/animalSubject.service';
import * as PreclinicalUtils from '../../utils/preclinical.utils';
import * as AppUtils from '../../../utils/app.utils';
import { EntityComponent } from '../../../shared/components/entity/entity.component.abstract';
import { DatepickerComponent } from '../../../shared/date-picker/date-picker.component';
import { BreadcrumbsService } from '../../../breadcrumbs/breadcrumbs.service';
import { ExaminationService } from '../../../examinations/shared/examination.service';
import { AnimalExaminationService } from '../shared/animal-examination.service';
import { ExaminationNode } from '../../../tree/tree.model';
import { UnitOfMeasure } from "../../../enum/unitofmeasure.enum";
import { dateDisplay } from "../../../shared/./localLanguage/localDate.abstract";
import { Subject } from "../../../subjects/shared/subject.model";

@Component({
    selector: 'examination-preclinical-form',
    templateUrl: 'animal-examination-form.component.html',
    styleUrls: ['animal-examination.component.css'],
    standalone: false
})
export class AnimalExaminationFormComponent extends EntityComponent<Examination>{
    @ViewChild('input', { static: false }) private fileInput: ElementRef;

    urlupload: string;
    physioDataFile: PhysiologicalDataFile;
    bloodGasData: BloodGasData;
    examinationBloodGasData: BloodGasData = new BloodGasData();
    bloodGasDataFile: BloodGasDataFile;
    extraData: ExtraData;
    contrastAgent: ContrastAgent = new ContrastAgent();
    examAnesthetic: ExaminationAnesthetic = new ExaminationAnesthetic();
    examinationExtradatas: ExtraData[] = [];
    centers: IdName[] = [];
    studies: IdName[] = [];
    public subjects: Subject[];
    animalSubjectId: number;
    private inImport: boolean;
    private files: File[] = [];
    unit = UnitOfMeasure;
    defaultUnit = this.unit.KG;
    dateDisplay = dateDisplay;
    hasDownloadRight: boolean = false;
    downloadState: TaskState = new TaskState();
    noDatasets: boolean = false;
    hasAdministrateRight: boolean = false;
    hasImportRight: boolean = false;

    constructor(
        private route: ActivatedRoute,
        private examinationService: ExaminationService,
        private animalExaminationService: AnimalExaminationService,
        private examAnestheticService: ExaminationAnestheticService,
        private extradatasService: ExtraDataService,
        private animalSubjectService: AnimalSubjectService,
        private centerService: CenterService,
        private studyService: StudyService,
        private studyRightsService: StudyRightsService,
        public breadcrumbsService: BreadcrumbsService,
        private downloadService: MassDownloadService
        ) {
            super(route, 'preclinical-examination');
            this.inImport = breadcrumbsService.isImporting();
            this.manageSaveEntity();
        }

    get examination(): Examination { return this.entity; }
    set examination(examination: Examination) { this.entity = examination; }

    getService(): EntityService<Examination> {
        return this.examinationService;
    }

    protected getTreeSelection: () => Selection = () => {
        return Selection.fromExamination(this.examination);
    }

    initView(): Promise<void> {
        if (!this.examination.weightUnitOfMeasure)
            this.examination.weightUnitOfMeasure = this.defaultUnit;

        if (!this.examination.subject)
            this.examination.subject = new Subject();

        if (this.keycloakService.isUserAdmin()) {
            this.hasAdministrateRight = true;
            this.hasDownloadRight = true;
            this.hasImportRight = true;
            return Promise.resolve();
        } else {
            return this.studyRightsService.getMyRightsForStudy(this.examination.study.id).then(rights => {
                this.hasImportRight = rights.includes(StudyUserRight.CAN_IMPORT);
                this.hasAdministrateRight = rights.includes(StudyUserRight.CAN_ADMINISTRATE);
                this.hasDownloadRight = rights.includes(StudyUserRight.CAN_DOWNLOAD);
            });
        }
        return Promise.resolve();
    }

    initEdit(): Promise<void> {
        this.getCenters();
        this.getStudies();
        this.getSubjects();
        if(!this.examination.weightUnitOfMeasure){
            this.examination.weightUnitOfMeasure = this.defaultUnit;
        }
        if (!this.examination.subject) {
            this.examination.subject = new Subject();
        }
        //this.loadExaminationAnesthetic(this.id);
        if(this.examination && this.examination.subject && this.examination.subject.id){
            this.animalSubjectService
                .getAnimalSubject(this.examination.subject.id)
                .then(animalSubject => this.animalSubjectId = animalSubject.id);
        }
        return Promise.resolve();
    }

    initCreate(): Promise<void> {
        this.entity = new Examination();
        this.examination.weightUnitOfMeasure = this.defaultUnit;
        this.examination.preclinical = true;
        this.getCenters();
        this.getStudies();
        return Promise.resolve();
    }

    buildForm(): UntypedFormGroup {
        const numericRegex = /-?\d*\.?\d{1,2}/;
        const form: UntypedFormGroup = this.formBuilder.group({
            'study': [{value: this.examination.study, disabled: this.inImport}, Validators.required],
            'subject': [{value: this.examination.subject, disabled: this.inImport}],
            'center': [{value: this.examination.center, disabled: this.inImport}, Validators.required],
            'examinationDate': [this.examination.examinationDate, [Validators.required, DatepickerComponent.validator]],
            'comment': [this.examination.comment],
            'note': [this.examination.note],
            'subjectWeight': [this.examination.subjectWeight, [Validators.pattern(numericRegex)]],
            'weightUnitOfMeasure': [this.examination.weightUnitOfMeasure]
        });

        this.subscriptions.push(
            form.get('study').valueChanges.subscribe((study: IdName) => {
                this.examination.study = study;
                this.getSubjects();
            })
        );

        return form;
    }

    private getCenters(): void {
        this.centers = [];
        this.centerService
            .getCentersNames()
            .then(centers => {
                this.centers = centers;
            });
    }

    private getStudies(): void {
        this.studies = [];
        this.studyService
            .getStudiesNames()
            .then(studies => {
                this.studies = studies;
            });
    }

    private getSubjects(): void {
        if (!this.examination.study) return;
        this.studyService
            .findSubjectsByStudyIdPreclinical(this.examination.study.id, this.examination.preclinical)
            .then(subjects => this.subjects = subjects);
    }

    manageSaveEntity(): void {
        this.subscriptions.push(
            this.onSave.subscribe(response => {
                this.manageExaminationAnesthetic(response.id);
                //this.manageContrastAgent();
                this.addExtraDataToExamination(response.id);
            })
        );

    }

    public save(): Promise<Examination> {
        return super.save().then(result => {
            // Once the exam is saved, save associated files
            for (const file of this.files) {
                this.examinationService.postFile(file, this.entity.id);
            }
            return result;
        });
    }

    manageExaminationAnesthetic(examinationId : number) {
        if (this.examAnesthetic) {
            this.examAnesthetic.examinationId = examinationId;
            if (this.examAnesthetic  && this.examAnesthetic.internalId) {
                this.examAnestheticService.updateAnesthetic(examinationId, this.examAnesthetic);
            } else if (this.examAnesthetic.anesthetic ) {
                this.examAnestheticService.createAnesthetic(examinationId, this.examAnesthetic);
            }
        }
    }

    addExtraDataToExamination(examinationId: number) {
        if (!examinationId) return;
        // Set the upload URL model
        if (this.physioDataFile) {
            this.physioDataFile.examinationId = examinationId;
            //Create physio data
            if (this.physioDataFile?.id) {
            	this.extradatasService.updateExtradata(PreclinicalUtils.PRECLINICAL_PHYSIO_DATA, this.physioDataFile.id, this.physioDataFile    )
                .then(physioData => {
                	if (this.physioDataFile.physiologicalDataFile){
                    this.extradatasService.postFile(this.physioDataFile.physiologicalDataFile, physioData)
                    	.then(() => {
                    		this.examinationExtradatas.push(physioData);
                    	});
                    }
                    //Add extra data to array
                    this.examinationExtradatas.push(physioData);
                });
            } else {
            	this.extradatasService.createExtraData(PreclinicalUtils.PRECLINICAL_PHYSIO_DATA, this.physioDataFile)
                .then(physioData => {
                if (this.physioDataFile.physiologicalDataFile){
                    this.extradatasService.postFile(this.physioDataFile.physiologicalDataFile, physioData)
                    	.then(() => {
                    		this.examinationExtradatas.push(physioData);
                    	});
                    }
                    //Add extra data to array
                    this.examinationExtradatas.push(physioData);
                });
            }
        }
        if (this.bloodGasData) {
            this.bloodGasData.examinationId = examinationId;
            //Create blood gas data
             if (this.examinationBloodGasData && this.examinationBloodGasData.id) {
            	this.extradatasService.updateExtradata(PreclinicalUtils.PRECLINICAL_BLOODGAS_DATA, this.examinationBloodGasData.id, this.bloodGasData)
                	.then(bloodGasData => {
                	if (this.bloodGasDataFile.bloodGasDataFile){
                    	this.extradatasService.postFile(this.bloodGasDataFile.bloodGasDataFile, bloodGasData)
                    		.then(() => {
                    			this.examinationExtradatas.push(bloodGasData);
                    		});
                    }
                    this.examinationExtradatas.push(bloodGasData);
                });
            } else {
            	this.extradatasService.createExtraData(PreclinicalUtils.PRECLINICAL_BLOODGAS_DATA, this.bloodGasData)
                	.then(bloodGasData => {
                	if (this.bloodGasDataFile.bloodGasDataFile){
                    	this.extradatasService.postFile(this.bloodGasDataFile.bloodGasDataFile, bloodGasData)
                    		.then(() => {
                    			this.examinationExtradatas.push(bloodGasData);
                    		});
                    }
                    this.examinationExtradatas.push(bloodGasData);
                });
            }
        }
    }

    onUploadExtraData(event) {
        this.extraData = event;
        this.extraData.extradatatype = "Extra data"
        this.examinationExtradatas.push(this.extraData);
        //this.examinationExtradatas = null;
        this.form.markAsDirty();
        this.form.updateValueAndValidity();
    }

    onUploadPhysiologicalData(event) {
        this.physioDataFile = event;
        this.form.markAsDirty();
        this.form.updateValueAndValidity();
    }

    onUploadBloodGasData(event) {
        this.bloodGasDataFile = event;
        this.bloodGasData = new BloodGasData();
        this.bloodGasData.filename =  this.bloodGasDataFile.filename;
        this.bloodGasData.extradatatype = "Blood gas data"
        this.form.markAsDirty();
        this.form.updateValueAndValidity();
    }

    onExamAnestheticChange(event) {
        this.examAnesthetic = event;
        this.form.markAsDirty();
        this.form.updateValueAndValidity();
    }

    onAgentChange(event) {
        this.contrastAgent = event;
        this.form.markAsDirty();
        this.form.updateValueAndValidity();
    }

    public exportBruker() {
        this.animalExaminationService.getBrukerArchive(this.examination.id)
            .then(response => {this.downloadIntoBrowser(response);});;
    }

    private downloadIntoBrowser(response: HttpResponse<Blob>){
        if (response.status == 200) {
            AppUtils.browserDownloadFile(response.body, this.getFilename(response));
        } else {
            this.consoleService.log('warn', 'Error: No bruker archive found for examination n°' + this.examination.id);
        }
    }

    private getFilename(response: HttpResponse<any>): string {
        const prefix = 'attachment;filename=';
        const contentDispHeader: string = response.headers.get('Content-Disposition');
        return contentDispHeader.slice(contentDispHeader.indexOf(prefix) + prefix.length, contentDispHeader.length);
    }

    // Extra data file management
    public setFile() {
        this.fileInput.nativeElement.click();
    }

    getFileName(element: string): string {
        return element.split('\\').pop().split('/').pop();
    }

    public deleteFile(file: any) {
        this.examination.extraDataFilePathList = this.examination.extraDataFilePathList.filter(fileToKeep => fileToKeep != file);
        this.files = this.files.filter(fileToKeep => fileToKeep.name != file);
    }

    public attachNewFile(event: any) {
        const newFile = event.target.files[0];
        this.examination.extraDataFilePathList.push(newFile.name);
        this.files.push(newFile);
    }

    onExaminationNodeInit(node: ExaminationNode) {
        node.open();
        this.breadcrumbsService.currentStep.data.examinationNode = node;
    }

    getUnit(key: string) {
        return UnitOfMeasure.getLabelByKey(key);
    }

    downloadAll() {
        this.downloadService.downloadAllByExaminationId(this.examination?.id,this.downloadState);
    }

    openViewer() {
	    window.open(environment.viewerUrl + '/viewer?StudyInstanceUIDs=1.4.9.12.34.1.8527.' + this.entity.id, '_blank');
    }

    openSegmentationViewer() {
        window.open(environment.viewerUrl + '/segmentation?StudyInstanceUIDs=1.4.9.12.34.1.8527.' + this.entity.id, '_blank');
    }

    public async hasDeleteRight(): Promise<boolean> {
         return this.keycloakService.isUserAdmin() || this.hasAdministrateRight;
    }
}
