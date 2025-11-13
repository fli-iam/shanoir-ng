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

import { Component, ViewChild, ElementRef} from '@angular/core';
import { UntypedFormGroup, Validators, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { NgClass } from '@angular/common';

import { EntityService } from 'src/app/shared/components/entity/entity.abstract.service';
import { Selection } from 'src/app/studies/study/tree.service';

import { ContrastAgent }    from '../../contrastAgent/shared/contrastAgent.model';
import { ContrastAgentService } from '../../contrastAgent/shared/contrastAgent.service';
import { Examination } from '../../../examinations/shared/examination.model';
import { ExaminationAnesthetic }    from '../../anesthetics/examination_anesthetic/shared/examinationAnesthetic.model';
import { ExaminationAnestheticService }    from '../../anesthetics/examination_anesthetic/shared/examinationAnesthetic.service';
import { ExtraData }    from '../../extraData/extraData/shared/extradata.model';
import { BloodGasData }    from '../../extraData/bloodGasData/shared/bloodGasData.model';
import { BloodGasDataFile }    from '../../extraData/bloodGasData/shared/bloodGasDataFile.model';
import { PhysiologicalData }    from '../../extraData/physiologicalData/shared/physiologicalData.model';
import { PhysiologicalDataFile }    from '../../extraData/physiologicalData/shared/physiologicalDataFile.model';
import { ExtraDataService } from '../../extraData/extraData/shared/extradata.service';
import { CenterService } from '../../../centers/shared/center.service';
import { StudyService } from '../../../studies/shared/study.service';
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
import {dateDisplay} from "../../../shared/./localLanguage/localDate.abstract";
import {Subject} from "../../../subjects/shared/subject.model";
import { FormFooterComponent } from '../../../shared/components/form-footer/form-footer.component';
import { SelectBoxComponent } from '../../../shared/select/select.component';
import { ExaminationAnestheticFormComponent } from '../../anesthetics/examination_anesthetic/edit/examinationAnesthetic-form.component';
import { PhysiologicalDataFormComponent } from '../../extraData/physiologicalData/add/physiologicalData-form.component';
import { BloodGasDataFormComponent } from '../../extraData/bloodGasData/add/bloodGasData-form.component';
import { ExaminationNodeComponent } from '../../../examinations/tree/examination-node.component';
import { LocalDateFormatPipe } from '../../../shared/localLanguage/localDateFormat.pipe';

@Component({
    selector: 'examination-preclinical-form',
    templateUrl: 'animal-examination-form.component.html',
    styleUrls: ['animal-examination.component.css'],
    imports: [FormsModule, ReactiveFormsModule, NgClass, FormFooterComponent, RouterLink, SelectBoxComponent, DatepickerComponent, ExaminationAnestheticFormComponent, PhysiologicalDataFormComponent, BloodGasDataFormComponent, ExaminationNodeComponent, LocalDateFormatPipe]
})
export class AnimalExaminationFormComponent extends EntityComponent<Examination>{

    @ViewChild('input', { static: false }) private fileInput: ElementRef;

    urlupload: string;
    physioData: PhysiologicalData;
    examinationPhysioData: PhysiologicalData = new PhysiologicalData();
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

    constructor(
        private route: ActivatedRoute,
        private examinationService: ExaminationService,
        private animalExaminationService: AnimalExaminationService,
        private examAnestheticService: ExaminationAnestheticService,
        private extradatasService: ExtraDataService,
        private contrastAgentsService: ContrastAgentService,
        private animalSubjectService: AnimalSubjectService,
        private centerService: CenterService,
        private studyService: StudyService,
        public breadcrumbsService: BreadcrumbsService)
    {

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
        if(!this.examination.weightUnitOfMeasure){
            this.examination.weightUnitOfMeasure = this.defaultUnit;
        }
        if (!this.examination.subject) {
            this.examination.subject = new Subject();
        }
        //this.loadExaminationAnesthetic();
        if(this.examination && this.examination.subject && this.examination.subject.id ){
            this.animalSubjectService
                .getAnimalSubject(this.examination.subject.id)
                .then(animalSubject => this.animalSubjectId = animalSubject.id);
        }
        return Promise.resolve();
    }


    initEdit(): Promise<void> {
        this.getCenters();
        this.getStudies();
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
            form.get('study').valueChanges.subscribe(() => {
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


    public getSubjects(): void {
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
                this.addExtraDataToExamination(response.id, false);
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

    addExtraDataToExamination(examinationId: number, isUpdate: boolean) {
        if (!examinationId) { return; }
        //Set the upload URL model
        if (this.physioData) {
            this.physioData.examinationId = examinationId;
            //Create physio data
            if (isUpdate && this.examinationPhysioData && this.examinationPhysioData.id){
            	this.extradatasService.updateExtradata(PreclinicalUtils.PRECLINICAL_PHYSIO_DATA,this.examinationPhysioData.id, this.physioData)
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
            }else{
            	this.extradatasService.createExtraData(PreclinicalUtils.PRECLINICAL_PHYSIO_DATA, this.physioData)
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
             if (isUpdate && this.examinationBloodGasData && this.examinationBloodGasData.id){
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
            }else{
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
    }

    onUploadPhysiologicalData(event) {
        this.physioDataFile = event;
        this.physioData = new PhysiologicalData();
        this.physioData.filename =  this.physioDataFile.filename;
        this.physioData.extradatatype = "Physiological data";
        this.physioData.has_heart_rate = this.physioDataFile.has_heart_rate;
        this.physioData.has_respiratory_rate = this.physioDataFile.has_respiratory_rate;
        this.physioData.has_sao2 = this.physioDataFile.has_sao2;
        this.physioData.has_temperature = this.physioDataFile.has_temperature;

    }

    onUploadBloodGasData(event) {
        this.bloodGasDataFile = event;
        this.bloodGasData = new BloodGasData();
        this.bloodGasData.filename =  this.bloodGasDataFile.filename;
        this.bloodGasData.extradatatype = "Blood gas data"
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

    onExamAnestheticChange(event) {
        this.examAnesthetic = event;
    }

    onAgentChange(event) {
        this.contrastAgent = event;
    }

    public async hasEditRight(): Promise<boolean> {
        return false;
    }

    public async hasDeleteRight(): Promise<boolean> {
        return false;
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
}
