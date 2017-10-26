import { Component, ViewChild } from '@angular/core';
import { FormGroup, FormBuilder, Validators, FormControl } from '@angular/forms';
import { Observable } from 'rxjs/Rx';

import { ImportService } from './import.service';
import { TreeNodeComponent } from '../shared/tree/tree.node.component';
import { slideDown } from '../shared/animations/animations';
import { PatientsDicom, PatientDicom, SerieDicom } from "./dicom.data.model";
import { ModalComponent } from '../shared/utils/modal.component';
import * as AppUtils from '../utils/app.utils';
declare var papaya: any;

@Component({
    selector: 'import-modality',
    templateUrl: 'import.component.html',
    styleUrls: ['import.component.css'],
    animations: [slideDown]
})

export class ImportComponent{
    @ViewChild('papayaModal') papayaModal: ModalComponent;
    private importForm: FormGroup;
    private patients: PatientDicom[];
    private patientDicom: PatientDicom;
    
    /* Form inputs */
    private modality: "IMR" | "PET";
    private subjectMode: "single" | "group" = "single";
    private subject;
    private archive;
    private serieSelected: boolean = false;
    
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
    
    private uploadProgress: number = 0;
    
    private tab_modality_open: boolean = true;
    private tab_upload_open: boolean = true;
    private tab_series_open: boolean = true;
    
    private subjects = [
        { id: 1, name: "ABVH3548" },
        { id: 2, name: "ABVH3548" },
        { id: 3, name: "ABVH6874" },
        { id: 4, name: "ABVH3548" },
        { id: 5, name: "ABVH9874" },
        { id: 6, name: "ABVH3548" }
    ];
    
    
    constructor(private fb: FormBuilder, private importService: ImportService) {
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
    
    // buildForm(): void {
        //     this.importForm = this.fb.group({
            //         // 'fu': [this.importForm.fu, [Validators.required, Validators.minLength(2), Validators.maxLength(50)]]
            //     });
            
            //     this.importForm.valueChanges
            //         .subscribe(data => this.onValueChanged(data));
            //     this.onValueChanged(); // (re)set validation messages now
            // }
            
            // onValueChanged(data?: any) {
                //     if (!this.importForm) { return; }
                //     const form = this.importForm;
                //     for (const field in this.formErrors) {
                    //         // clear previous error message (if any)
                    //         this.formErrors[field] = '';
    //         const control = form.get(field);
    //         if (control && control.dirty && !control.valid) {
        //             for (const key in control.errors) {
            //                 this.formErrors[field] += key;
            //             }
            //         }
            //     }
            // }
            
            // formErrors = {
                //     'fu': '',
                // };
                
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
        let formData: FormData = new FormData();
        formData.append('file', file[0], file[0].name);
        this.importService.uploadFile(formData)
            .then((patientDicomList: PatientsDicom) => {
                this.patients = patientDicomList.patients;
            });
        // TEST
        this.uploadProgress = 0;
        let subscription1 = Observable.timer(0, 10).subscribe(t => {
            this.uploadProgress = t * 0.005;
            if (this.uploadProgress >= 1) {
                this.uploadProgress = 1;
                subscription1.unsubscribe();
                this.archive = "file uploaded";
                this.tab_upload_open = false;
            }
        });
    }

    selectNode(nodeParams: any) {
        console.log(nodeParams);
    }

    isDicomSerieSelected(): void {
        console.log();
    }

    selectDicomSeries(): void {

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