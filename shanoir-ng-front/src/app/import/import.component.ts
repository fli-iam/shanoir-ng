import { Component, OnInit, ViewChild } from '@angular/core';
import { FormGroup, FormBuilder, Validators, FormControl } from '@angular/forms';
import { Observable } from 'rxjs/Rx';

import { IdNameObject } from '../shared/models/id-name-object.model';
import { ImportService } from './import.service';
import { slideDown } from '../shared/animations/animations';
import { PatientsDicom, PatientDicom, SerieDicom } from "./dicom-data.model";
import { ModalComponent } from '../shared/components/modal/modal.component';
import * as AppUtils from '../utils/app.utils';
import { Study } from '../studies/shared/study.model';
import { StudyService } from '../studies/shared/study.service';
import { Examination } from '../examinations/shared/examination.model';
import { StudyCard } from '../study-cards/shared/study-card.model';
import { ExaminationService } from '../examinations/shared/examination.service';
import { Subject } from '../subjects/shared/subject.model';

declare var papaya: any;

@Component({
    selector: 'import-modality',
    templateUrl: 'import.component.html',
    styleUrls: ['import.component.css'],
    animations: [slideDown]
})

export class ImportComponent implements OnInit {
    @ViewChild('papayaModal') papayaModal: ModalComponent;
    private patients: PatientDicom[];
    private patientDicom: PatientDicom;
    
    /* Form inputs */
    private modality: "IMR" | "PET";
    private subjectMode: "single" | "group" = "single";
    private extensionError: Boolean;
    private dicomDirMissingError: Boolean;
    private subject: Subject;
    private archive;
    private selectedSeries: SerieDicom[] = [];
    
    /* Display variables */
    private step: number = 1;
    private detailedSerie: Object;
    private detailedPatient: Object;
    private dsAcqOpened: boolean = false;
    private createUser: boolean = false;
    
    private pacsProgress: number = 0;
    private pacsStatus: string;
    private anonymProgress: number = 0;
    private anonymStatus: string;
    private niftiProgress: number = 0;
    private niftiStatus: string;
    private studyCardProgress: number = 0;
    private studyCardStatus: string;

    private tab_modality_open: boolean = true;
    private tab_upload_open: boolean = true;
    private tab_series_open: boolean = true;
    
    public importForm: FormGroup;
    private studies: Study[];
    private study: Study;
    private studycards: IdNameObject[];
    private subjects: Subject[]; 
    private examinations: Examination[];
    
    constructor(private fb: FormBuilder, private importService: ImportService,
        private studyService: StudyService, private examinationService: ExaminationService) {
    }

    ngOnInit(): void {
        this.findStudiesWithStudyCardsByUserId();
        this.buildForm();
    }

    findStudiesWithStudyCardsByUserId(): void {
        this.studyService
            .findStudiesWithStudyCardsByUserId()
            .then(studies => {
                this.studies = studies;
            })
            .catch((error) => {
                // TODO: display error
                console.log("error getting study list by user!");
            });
    }
    
    closeEditSubject(subject: any) {
        // Add the subject to the select box and select it
        console.log(subject);
        if (subject) {
            subject.name = subject.lastName;
            subject.selected = true;
            this.subjects.push(subject);
        }
        this.createUser = false;
    }
    
    buildForm(): void {
        this.importForm = this.fb.group({
            'study': [this.study, Validators.required],
            'fu': new FormControl(),
            'studycard': new FormControl(),
            'subjectMode': new FormControl(),
            'subjectName': new FormControl(),
            'subjectIdentifier': new FormControl(),
            'examination': new FormControl(),
            'modality': [this.modality, Validators.required],
            'subject': [this.subject, Validators.required]
        });
    
        this.importForm.valueChanges
            .subscribe(data => this.onValueChanged(data));
        this.onValueChanged(); // (re)set validation messages now
    }
        
    onValueChanged(data?: any) {
        if (!this.importForm) { return; }
        const form = this.importForm;
        for (const field in this.formErrors) {
                // clear previous error message (if any)
                this.formErrors[field] = '';
            const control = form.get(field);
            if (control && control.dirty && !control.valid) {
                for (const key in control.errors) {
                        this.formErrors[field] += key;
                }
            }
        }
    }
    
    formErrors = {
        'fu': '',
        'study': '',
        'studycard': '',
        'subjectMode': '',
        'subjectName': '',
        'subjectIdentifier': '',
        'examination': '',
        'modality': '',
        'subject': ''
    };
            
    initPapaya(serie: SerieDicom) {
        var params = [];
        let imagesList: string[] = [];
        let dicomImagesList: Array<string[]> = [];
        for (let image of serie.images) {
            imagesList.push(AppUtils.BACKEND_API_IMAGE_VIEWER_URL + image);
        }
        dicomImagesList.push(imagesList);
        params["images"] = dicomImagesList;
        params["kioskMode"] = true;
        papaya.Container.startPapaya();
        papaya.Container.addViewer("papaya", params);
    }
    
    showSerieDetails(nodeParams: any) {
        if (nodeParams && this.detailedSerie && nodeParams.id == this.detailedSerie["id"]) {
            this.detailedSerie = null;
        } else {
            this.detailedSerie = nodeParams;
        }
    }

    showPatientDetails(nodeParams: any) {
        if (nodeParams && this.detailedPatient && nodeParams.id == this.detailedPatient["id"]) {
            this.detailedPatient = null;
        } else {
            this.detailedPatient = nodeParams;
        }
    }

    uploadArchive(event: any): void {
        let file = event.srcElement.files;
        let index = file[0].name.lastIndexOf(".");
        let strsubstring = file[0].name.substring(index, file[0].name.length);
        if (strsubstring != '.zip') {
            this.extensionError = true;
            this.dicomDirMissingError = false;
            this.archive = '';
        } else {
            this.extensionError = false;
            this.dicomDirMissingError = false;
            this.archive = "file uploaded";
        }
        let formData: FormData = new FormData();
        formData.append('file', file[0], file[0].name);
        this.importService.uploadFile(formData)
            .subscribe((patientDicomList: PatientsDicom) => {
                this.patients = patientDicomList.patients;
            }, (err: String) => {
                if (err.indexOf("DICOMDIR is missing") != -1) {
                    this.dicomDirMissingError = true;
                    this.extensionError = false;
                    this.archive = '';
                }
            });
    }

    selectNode(nodeParams: any) {
        console.log("nodeParams: ", nodeParams);
        if (nodeParams.seriesInstanceUID) {
            this.selectedSeries.push(nodeParams);
        } else if (nodeParams.studyInstanceUID) {
            this.selectedSeries.push(nodeParams.series);
        }
        console.log("final selected series: ", this.selectedSeries);
    }

    selectDicomSeries(): void {
        this.importService.selectSeries(this.selectedSeries);
    }

    onSelectStudy(study: Study) {
        if (study) {
            this.studycards = study.studyCards;
            this.studyService
                .findSubjectsByStudyId(study.id)
                .then(subjects => this.subjects = subjects)
                .catch((error) => {
                    // TODO: display error
                    console.log("error getting subject list by study id!");
                });
            this.buildForm();
        }
    }

    onSelectSubject(subject: Subject) {
        if (subject) {
            this.subject = subject;
            this.examinationService
                .findExaminationsBySubjectId(subject.id)
                .then(examinations => this.examinations = examinations)
                .catch((error) => {
                    // TODO: display error
                    console.log("error getting examination list by subject id!");
                });
            this.buildForm();
        }
    }

    startProgressTest() {
        this.pacsStatus = "";
        this.anonymStatus = "";
        this.niftiStatus = "";
        this.studyCardStatus = "";
        this.pacsProgress = 0;
        this.anonymProgress = 0;
        this.niftiProgress = 0;
        this.studyCardProgress = 0;
        let subscription1 = Observable.timer(0, 10).subscribe(t => {
            this.pacsProgress = t * 0.005;
            if (this.pacsProgress >= 1) {
                this.pacsProgress = 1;
                this.pacsStatus = "ok";
                subscription1.unsubscribe();
                let subscription2 = Observable.timer(0, 10).subscribe(t => {
                    this.anonymProgress = t * 0.002;
                    if (this.anonymProgress >= 1) {
                        this.anonymProgress = 1;
                        this.anonymStatus = "ok";
                        subscription2.unsubscribe();
                        let subscription3 = Observable.timer(0, 10).subscribe(t => {
                            this.niftiProgress = t * 0.01;
                            if (this.niftiProgress >= 1) {
                                this.niftiProgress = 1;
                                this.niftiStatus = "ok";
                                subscription3.unsubscribe();
                                let subscription4 = Observable.timer(0, 10).subscribe(t => {
                                    this.studyCardProgress = t * 0.01;
                                    if (this.studyCardProgress >= 0.3) {
                                        this.studyCardStatus = "error";
                                        subscription4.unsubscribe();
                                    }
                                });
                            }
                        });
                    }
                });
            }
        });
    }
}