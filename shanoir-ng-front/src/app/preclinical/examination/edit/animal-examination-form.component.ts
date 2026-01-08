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

import { Component } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { ExaminationComponent } from 'src/app/examinations/examination/examination.component';
import { MassDownloadService } from 'src/app/shared/mass-download/mass-download.service';

import { BreadcrumbsService } from '../../../breadcrumbs/breadcrumbs.service';
import { CenterService } from '../../../centers/shared/center.service';
import { Examination } from '../../../examinations/shared/examination.model';
import { ExaminationService } from '../../../examinations/shared/examination.service';
import { StudyRightsService } from '../../../studies/shared/study-rights.service';
import { StudyService } from '../../../studies/shared/study.service';
import { ExaminationAnesthetic } from '../../anesthetics/examination_anesthetic/shared/examinationAnesthetic.model';
import { ExaminationAnestheticService } from '../../anesthetics/examination_anesthetic/shared/examinationAnesthetic.service';
import { AnimalSubjectService } from '../../animalSubject/shared/animalSubject.service';
import { ContrastAgent } from '../../contrastAgent/shared/contrastAgent.model';
import { BloodGasDataFile } from '../../extraData/bloodGasData/shared/bloodGasDataFile.model';
import { ExtraData } from '../../extraData/extraData/shared/extradata.model';
import { ExtraDataService } from '../../extraData/extraData/shared/extradata.service';
import { PhysiologicalDataFile } from '../../extraData/physiologicalData/shared/physiologicalDataFile.model';
import * as PreclinicalUtils from '../../utils/preclinical.utils';
import { AnimalExaminationService } from '../shared/animal-examination.service';


@Component({
    selector: 'examination-preclinical-form',
    templateUrl: 'animal-examination-form.component.html',
    styleUrls: ['animal-examination.component.css'],
    standalone: false
})
export class AnimalExaminationFormComponent extends ExaminationComponent {

    physioDataFile: PhysiologicalDataFile;
    bloodGasDataFile: BloodGasDataFile;
    extraData: ExtraData;
    contrastAgent: ContrastAgent = new ContrastAgent();
    examAnesthetic: ExaminationAnesthetic = new ExaminationAnesthetic();
    animalSubjectId: number;
    examinationExtradatas: ExtraData[] = [];

    constructor(
            route: ActivatedRoute,
            examinationService: ExaminationService,
            centerService: CenterService,
            studyService: StudyService,
            studyRightsService: StudyRightsService,
            breadcrumbsService: BreadcrumbsService,
            downloadService: MassDownloadService,
            private animalExaminationService: AnimalExaminationService,
            private examAnestheticService: ExaminationAnestheticService,
            private extradatasService: ExtraDataService,
            private animalSubjectService: AnimalSubjectService) {
        super(route, examinationService, centerService, studyService, studyRightsService, breadcrumbsService, downloadService);
        this.manageSaveEntity();
    }

    protected getRoutingName(): string {
        return 'preclinical-examination';
    }

    initEdit(): Promise<void> {
        super.initEdit();
        if(this.examination && this.examination.subject && this.examination.subject.id){
            this.animalSubjectService
                .getAnimalSubject(this.examination.subject.id)
                .then(animalSubject => this.animalSubjectId = animalSubject.id);
        }
        return Promise.resolve();
    }

    initCreate(): Promise<void> {
        super.initCreate();
        this.examination.preclinical = true;
        return Promise.resolve();
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
            if (this.physioDataFile?.id) {
            	this.extradatasService.updateExtradata(PreclinicalUtils.PRECLINICAL_PHYSIO_DATA, this.physioDataFile.id, this.physioDataFile    )
                .then(() => {
                	if (this.physioDataFile.physiologicalDataFile){
                    this.extradatasService.postFile(this.physioDataFile.physiologicalDataFile, this.physioDataFile)
                    	.then(() => {
                    		this.examinationExtradatas.push(this.physioDataFile);
                    	});
                    }
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
                });
            }
        }
        if (this.bloodGasDataFile) {
            this.bloodGasDataFile.examinationId = examinationId;
            //Create blood gas data
            if (this.bloodGasDataFile.id) {
            	this.extradatasService.updateExtradata(PreclinicalUtils.PRECLINICAL_BLOODGAS_DATA, this.bloodGasDataFile.id, this.bloodGasDataFile)
                	.then(() => {
                        if (this.bloodGasDataFile.bloodGasDataFile){
                            this.extradatasService.postFile(this.bloodGasDataFile.bloodGasDataFile, this.bloodGasDataFile)
                                .then(() => {
                                    this.examinationExtradatas.push(this.bloodGasDataFile);
                                });
                        }
                    });
            } else {
            	this.extradatasService.createExtraData(PreclinicalUtils.PRECLINICAL_BLOODGAS_DATA, this.bloodGasDataFile)
                	.then(bloodGasData => {
                        if (this.bloodGasDataFile.bloodGasDataFile){
                            this.extradatasService.postFile(this.bloodGasDataFile.bloodGasDataFile, bloodGasData)
                                .then(() => {
                                    this.examinationExtradatas.push(bloodGasData);
                                });
                        }
                    });
            }
        }
    }

    onUploadExtraData(event) {
        this.extraData = event;
        this.extraData.extraDataType = "Extra data";
        this.examinationExtradatas.push(this.extraData);
        this.form.markAsDirty();
        this.form.updateValueAndValidity();
    }

    onUploadPhysiologicalData(event) {
        const fileChanged: boolean = this.physioDataFile?.physiologicalDataFile?.name != event?.physiologicalDataFile?.name;
        this.physioDataFile = event;
        this.physioDataFile.extraDataType = "Physiological data";
        if (fileChanged) {
            this.examination.extraDataFilePathList.push(this.physioDataFile.physiologicalDataFile.name);
            this.files.push(this.physioDataFile.physiologicalDataFile);
        }
        this.form.markAsDirty();
        this.form.updateValueAndValidity();
    }

    onUploadBloodGasData(event) {
        this.bloodGasDataFile = event;
        this.bloodGasDataFile.extraDataType = "Blood gas data";
        this.examination.extraDataFilePathList.push(this.bloodGasDataFile.bloodGasDataFile.name);
        this.files.push(this.bloodGasDataFile.bloodGasDataFile);
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
}
