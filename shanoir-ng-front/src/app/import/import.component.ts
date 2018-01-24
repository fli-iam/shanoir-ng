import { Component, OnInit, ViewChild } from '@angular/core';
import { FormGroup, FormBuilder, Validators, FormControl } from '@angular/forms';
import { Observable } from 'rxjs/Rx';

import { IdNameObject } from '../shared/models/id-name-object.model';
import { ImportService } from './import.service';
import { slideDown } from '../shared/animations/animations';
import { ImportJob, PatientDicom, SerieDicom, EquipmentDicom } from "./dicom-data.model";
import { ModalComponent } from '../shared/components/modal/modal.component';
import * as AppUtils from '../utils/app.utils';
import { Study } from '../studies/shared/study.model';
import { StudyService } from '../studies/shared/study.service';
import { StudyCard } from '../study-cards/shared/study-card.model';
import { ExaminationService } from '../examinations/shared/examination.service';
import { Subject } from '../subjects/shared/subject.model';
import { SubjectExamination } from '../examinations/shared/subject-examination.model';

declare var papaya: any;
// const mockImport: any = require('../../assets/mock-import.json');
// const mockStudy: any = require('../../assets/mock-study.json');

@Component({
    selector: 'import-modality',
    templateUrl: 'import.component.html',
    styleUrls: ['import.component.css'],
    animations: [slideDown]
})

export class ImportComponent implements OnInit {
    @ViewChild('papayaModal') papayaModal: ModalComponent;
    @ViewChild('studyModal') studyModal: ModalComponent;
    
    public importForm: FormGroup;
    private extensionError: Boolean;
    private dicomDirMissingError: Boolean;
    private studycardMissingError: Boolean;
    private studycardNotCompatibleError: Boolean;
    
    public archive: string;
    public modality: string;
    public subjectMode: "single" | "group" = "single";
    public patients: PatientDicom[];
    private patientDicom: PatientDicom;
    public subject: Subject;
    public studies: Study[];
    public study: Study;
    public studycards: StudyCard[];
    public studycard: StudyCard;
    private subjects: Subject[]; 
    public examinations: SubjectExamination[];
    private seriesSelected: boolean = false;
    private selectedSeries: PatientDicom;
    private detailedPatient: Object;
    public dsAcqOpened: boolean = false;
    private detailedSerie: Object;
    
    public tab_modality_open: boolean = true;
    public tab_upload_open: boolean = true;
    public tab_series_open: boolean = true;

    public pacsProgress: number = 0;
    public pacsStatus: string;
    public anonymProgress: number = 0;
    public anonymStatus: string;
    public niftiProgress: number = 0;
    public niftiStatus: string;
    public studyCardProgress: number = 0;
    public studyCardStatus: string;
    public createUser: boolean = false;


    //TODO: remove after json3 creation
    public examination: SubjectExamination;

    constructor(private fb: FormBuilder, private importService: ImportService,
        private studyService: StudyService, private examinationService: ExaminationService) {
    }

    ngOnInit(): void {
        this.buildForm();
        //TODO: clean json mock import after dev 
        //this.seriesSelected = true;
        // this.selectedSeries = mockImport;
        // this.validateSeriesSelected();
        //TODO: clean json mock study after dev
        //this.prepareStudyStudycard(mockStudy);
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
            'studycard': [this.studycard, Validators.required],
            'subjectMode': new FormControl(),
            'subjectName': new FormControl(),
            'subjectIdentifier': new FormControl(),
            'examination': new FormControl(),
            'modality': [{value: this.modality, disabled: true}, Validators.required],
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
        var params: object[] = [];
        let imagesList: string[] = [];
        let dicomImagesList: Array<string[]> = [];
        for (let image of serie.images) {
            imagesList.push(AppUtils.BACKEND_API_IMAGE_VIEWER_URL + image["path"]);
        }
        dicomImagesList.push(imagesList);
        params["images"] = dicomImagesList;
        params["kioskMode"] = true;
        papaya.Container.startPapaya();
        papaya.Container.addViewer("papaya", params);
    }
    
    uploadArchive(event: any): void {
        let file:any = event.srcElement.files;
        let index:any = file[0].name.lastIndexOf(".");
        let strsubstring: any = file[0].name.substring(index, file[0].name.length);
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
            .subscribe((patientDicomList: ImportJob) => {
                let modalityOfDicomDir:any = patientDicomList.patients[0].studies[0].series[0].modality.toString();
                this.modality = modalityOfDicomDir;
                this.patients = patientDicomList.patients;
            }, (err: String) => {
                if (err.indexOf("DICOMDIR is missing") != -1) {
                    this.dicomDirMissingError = true;
                    this.extensionError = false;
                    this.archive = '';
                }
            });
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

    selectNode(nodeParams: PatientDicom) {
        this.selectedSeries = nodeParams;
        for (let study of nodeParams.studies) {
            for (let serie of study.series) {
                this.seriesSelected = false;
                if (serie.selected) {
                    this.seriesSelected = true;
                    break;
                } 
            }
        }
    }

    validateSeriesSelected () : void {
        this.findStudiesWithStudyCardsByUserAndEquipment(this.selectedSeries.studies[0].series[0].equipment);
    }

    // TODO: to be called later according to the new spec
    // this.importService.selectSeries(this.selectedSeries);

    findStudiesWithStudyCardsByUserAndEquipment(equipment: EquipmentDicom): void {
        this.studyService
            .findStudiesWithStudyCardsByUserAndEquipment(equipment)
            .then(studies => {
                this.prepareStudyStudycard(studies);
            })
            .catch((error) => {
                // TODO: display error
                console.log("error getting study and study card list by user and equipment!");
            });
    }

    prepareStudyStudycard(studies: Study[]) {
        this.studies = studies;
        let compatibleStudies: Study[] = [];
        for (let study of studies) {
            if (study.compatible) {
                compatibleStudies.push(study);
            }
        }
        if (compatibleStudies.length == 1) {
            // autoselect study
            this.study = compatibleStudies[0];
            this.studycards = this.study.studyCards;
        }
    }
    
    onSelectStudy(study: Study) {
        if (study.studyCards.length == 0) {
            this.studycardMissingError = true;
        } else {
            this.studycardMissingError = false;
            let compatibleStudycards: StudyCard[] = [];
            for (let studycard of study.studyCards) {
                if (studycard.compatible) {
                    compatibleStudycards.push(studycard);
                }
            }
            if (compatibleStudycards.length == 1) {
                // autoselect studycard
                this.studycard = compatibleStudycards[0];
            } 
            this.studycards = study.studyCards;
        }
    }

    onSelectStudycard(studycard: StudyCard) {
        if (studycard.compatible) {
            this.studycardNotCompatibleError = false;
            this.studyService
                .findSubjectsByStudyId(this.study.id)
                .then(subjects => this.subjects = subjects)
                .catch((error) => {
                    // TODO: display error
                    console.log("error getting subject list by study id!");
            });
        } else {
            this.studycardNotCompatibleError = true;
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
        }
    }

    closePopin() {
        this.studyModal.hide();
    }

    // startProgressTest() {
    //     this.pacsStatus = "";
    //     this.anonymStatus = "";
    //     this.niftiStatus = "";
    //     this.studyCardStatus = "";
    //     this.pacsProgress = 0;
    //     this.anonymProgress = 0;
    //     this.niftiProgress = 0;
    //     this.studyCardProgress = 0;
    //     let subscription1:any = Observable.timer(0, 10).subscribe(t => {
    //         this.pacsProgress = t * 0.005;
    //         if (this.pacsProgress >= 1) {
    //             this.pacsProgress = 1;
    //             this.pacsStatus = "ok";
    //             subscription1.unsubscribe();
    //             let subscription2 = Observable.timer(0, 10).subscribe(t => {
    //                 this.anonymProgress = t * 0.002;
    //                 if (this.anonymProgress >= 1) {
    //                     this.anonymProgress = 1;
    //                     this.anonymStatus = "ok";
    //                     subscription2.unsubscribe();
    //                     let subscription3 = Observable.timer(0, 10).subscribe(t => {
    //                         this.niftiProgress = t * 0.01;
    //                         if (this.niftiProgress >= 1) {
    //                             this.niftiProgress = 1;
    //                             this.niftiStatus = "ok";
    //                             subscription3.unsubscribe();
    //                             let subscription4 = Observable.timer(0, 10).subscribe(t => {
    //                                 this.studyCardProgress = t * 0.01;
    //                                 if (this.studyCardProgress >= 0.3) {
    //                                     this.studyCardStatus = "error";
    //                                     subscription4.unsubscribe();
    //                                 }
    //                             });
    //                         }
    //                     });
    //                 }
    //             });
    //         }
    //     });
    // }
}